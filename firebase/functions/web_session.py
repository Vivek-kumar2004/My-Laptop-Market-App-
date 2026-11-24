from __future__ import annotations

import html
import secrets
from datetime import datetime, timedelta, timezone

from firebase_admin import firestore
from flask import Response

from datetime import date

from accounts import register
from auth import _days_left, acknowledge_warning, login
from email_verify import confirm as confirm_email_otp
from email_verify import send as send_email_otp
from phone_verify import confirm as confirm_mobile_otp
from password_reset import forgot_password, reset_password
from subscription import SHOW_VENDOR_PAYMENT
from feature_flags import SHOW_VENDOR_OTP
from vendor_auth_ui import (
    auth_document,
    forgot_main,
    login_main,
    login_scripts,
    mobile_firebase_scripts,
    register_footer,
    register_main,
    register_scripts,
)

COOKIE = "mlm_vendor"
MAX_AGE = 120 * 60
ASSET = "https://storage.googleapis.com/mylaptopmarket-app-web/mylaptop/assets"
# Live-site webfonts omit CORS, so the dashboard on Cloud Functions shows every icon as the same box.
FA = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"
VENDOR = "/api/vendor"
LOGO = "https://storage.googleapis.com/mylaptopmarket-app-web/images/mylaptoplogo.png"


def _firebase_config() -> dict:
    import json
    import os

    path = os.path.join(os.path.dirname(__file__), "firebase_web.json")
    if not os.path.exists(path):
        return {}
    try:
        with open(path, encoding="utf-8") as handle:
            raw = json.load(handle)
    except Exception:
        return {}
    if not isinstance(raw, dict):
        return {}
    return {
        "apiKey": _text(raw.get("apiKey")),
        "authDomain": _text(raw.get("authDomain")),
        "projectId": _text(raw.get("projectId")),
    }


def _mobile_auth_script() -> str:
    import json

    payload = json.dumps(_firebase_config())
    return """
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-auth-compat.js"></script>
<script>
(function() {
  var config = __CONFIG__;
  var note = document.getElementById("mobileNote");
  var send = document.getElementById("sendMobileOtp");
  var verify = document.getElementById("verifyMobileOtp");
  function say(text) { if (note) note.textContent = text; }
  function smsFailText(error) {
    var code = error && error.code ? String(error.code) : "";
    var blob = (code + " " + (error && error.message ? String(error.message) : "")).toUpperCase();
    if (blob.indexOf("TOO_MANY") >= 0 || blob.indexOf("TOO-MANY") >= 0) {
      return "Firebase has paused SMS for this number. Use a different mobile number.";
    }
    if (blob.indexOf("QUOTA") >= 0) return "SMS limit reached for now. Try again later.";
    if (blob.indexOf("CAPTCHA") >= 0 || blob.indexOf("INVALID-APP-CREDENTIAL") >= 0 || blob.indexOf("MISSING-CLIENT") >= 0) {
      return "The check failed. Close this and tap Send SMS again.";
    }
    if (blob.indexOf("OPERATION_NOT_ALLOWED") >= 0 || blob.indexOf("OPERATION-NOT-ALLOWED") >= 0) {
      return "SMS is blocked for this number right now.";
    }
    if (blob.indexOf("NETWORK") >= 0) return "Network problem. Check internet and tap Send SMS again.";
    if (code.indexOf("auth/") === 0) return "Could not send the SMS OTP (" + code.slice(5) + ").";
    return "Could not send the SMS OTP.";
  }
  if (!config.apiKey || !send || !verify) {
    say("SMS verification is not ready.");
    return;
  }
  firebase.initializeApp(config);
  var confirmation = null;
  var verifier = null;
  send.onclick = function() {
    var mobile = (document.querySelector("[name=mobile]").value || "").replace(/\\D/g, "");
    if (!/^[6-9][0-9]{9}$/.test(mobile)) {
      say("Enter a valid 10-digit Indian mobile number.");
      return;
    }
    say("Sending...");
    if (!verifier) {
      verifier = new firebase.auth.RecaptchaVerifier("recaptcha", {size: "normal"});
    }
    firebase.auth().signInWithPhoneNumber("+91" + mobile, verifier).then(function(result) {
      confirmation = result;
      say("OTP sent to your mobile number.");
    }).catch(function(error) {
      say(smsFailText(error));
      if (verifier && verifier.clear) verifier.clear();
      verifier = null;
    });
  };
  verify.onclick = function() {
    var mobile = (document.querySelector("[name=mobile]").value || "").replace(/\\D/g, "");
    var code = (document.querySelector("[name=mobile_otp]").value || "").replace(/\\D/g, "");
    if (!confirmation) {
      say("Send an OTP to this mobile number first.");
      return;
    }
    say("Checking...");
    confirmation.confirm(code).then(function(result) {
      return result.user.getIdToken();
    }).then(function(token) {
      return fetch("mobile-verify", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({mobile: mobile, firebase_token: token})
      });
    }).then(function(res) { return res.json(); }).then(function(data) {
      firebase.auth().signOut();
      say((data && data.message) || "Mobile number verified.");
    }).catch(function() {
      say("Incorrect OTP.");
    });
  };
})();
</script>
""".replace("__CONFIG__", payload)


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _aware(value):
    if isinstance(value, datetime):
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)
        return value
    return None


def _public(data: dict) -> dict:
    return {
        "id": int(data.get("vendor_id") or 0),
        "shop_name": data.get("shop_name"),
        "username": data.get("username"),
    }


def vendor_session(req) -> dict | None:
    token = _text(req.cookies.get(COOKIE) if req.cookies else "")
    if token == "":
        return None
    ref = firestore.client().collection("vendor_sessions").document(token)
    doc = ref.get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}
    expires = _aware(data.get("expires_at"))
    if expires is None or expires <= datetime.now(timezone.utc):
        ref.delete()
        return None
    if not _text(data.get("username")) or not data.get("vendor_id"):
        return None
    data["_token"] = token
    return data


def _request_path() -> str:
    try:
        from flask import request

        path = request.path or ""
        host = (request.host or "").split(":")[0].lower()
    except RuntimeError:
        return ""
    if path.startswith("/mylaptop/"):
        path = path[len("/mylaptop") :]
    if host in {"mylaptopmarket.com", "www.mylaptopmarket.com"} and path.startswith("/api/"):
        path = path[len("/api") :]
    if not path.startswith("/"):
        path = "/" + path
    return path.rstrip("/") or "/"


def _session_vendor() -> tuple[dict | None, dict]:
    try:
        from flask import request

        session = vendor_session(request)
    except RuntimeError:
        return None, {}
    if session is None:
        return None, {}
    doc = firestore.client().collection("vendors").document(str(session.get("vendor_id") or 0)).get()
    vendor = doc.to_dict() or {} if doc.exists else {}
    return session, vendor


def _logo_src(vendor: dict) -> str:
    raw = _text(vendor.get("logo_url")) or _text(vendor.get("logo"))
    if raw.startswith("http://") or raw.startswith("https://"):
        return raw
    if raw:
        return f"{ASSET}/uploads/vendors/{raw.split('/')[-1]}"
    return LOGO


def _unread_count(vendor_id: int) -> int:
    unread = 0
    for doc in firestore.client().collection("messages").where("receiver_id", "==", vendor_id).stream():
        row = doc.to_dict() or {}
        if row.get("is_read") not in (1, True, "1"):
            unread += 1
    return unread


def _nav(href: str, icon: str, label: str, current: str, badge: str = "") -> str:
    active = " active" if current.rstrip("/").endswith("/" + href.strip("/").split("/")[-1]) else ""
    mark = f'<span class="badge badge-danger right">{badge}</span>' if badge else ""
    return (
        f'<li class="nav-item"><a href="{href}" class="nav-link{active}">'
        f'<i class="nav-icon fas {icon}"></i><p>{label}{mark}</p></a></li>'
    )


def _sidebar(vendor: dict, current: str, unread: int) -> str:
    shop = html.escape(_text(vendor.get("shop_name")) or "Vendor")
    logo = html.escape(_logo_src(vendor))
    messages = _nav(f"{VENDOR}/messages", "fa-comments", "Messages", current, str(unread) if unread else "")
    return f"""
<aside class="main-sidebar sidebar-dark-primary elevation-4">
<a href="{VENDOR}/dashboard" class="brand-link d-flex align-items-center" style="gap:10px; overflow:hidden;">
<img src="{logo}" alt="" onerror="this.onerror=null;this.src='{LOGO}';" style="float:none !important; opacity:1; width:42px; height:42px; min-width:42px; object-fit:cover; border-radius:50%; background:#fff;">
<span class="brand-text font-weight-light" style="max-width:140px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="{shop}">{shop}</span>
</a>
<div class="sidebar"><nav class="mt-2"><ul class="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu" data-accordion="false">
{_nav(f"{VENDOR}/dashboard", "fa-tachometer-alt", "Dashboard", current)}
{_nav(f"{VENDOR}/profile", "fa-id-badge", "My Profile / Logo", current)}
{_nav(f"{VENDOR}/seller-verification", "fa-user-check", "Seller Verification", current)}
<li class="nav-item"><a href="#" class="nav-link"><i class="nav-icon fas fa-store"></i><p>Product Management (B2B)<i class="right fas fa-angle-left"></i></p></a>
<ul class="nav nav-treeview">
<li class="nav-item"><a href="{VENDOR}/add_product" class="nav-link{' active' if current.endswith('/add_product') else ''}"><i class="far fa-circle nav-icon"></i><p>Add Product</p></a></li>
<li class="nav-item"><a href="{VENDOR}/product_list" class="nav-link{' active' if current.endswith('/product_list') else ''}"><i class="far fa-circle nav-icon"></i><p>Product List</p></a></li>
</ul></li>
{_nav(f"{VENDOR}/invoices", "fa-file-invoice-dollar", "My Invoices", current)}
{_nav(f"{VENDOR}/requirements", "fa-clipboard-list", "Requirements", current)}
{messages}
{_nav(f"{VENDOR}/search_products", "fa-search", "Search Products", current)}
{_nav(f"{VENDOR}/logout", "fa-sign-out-alt", "Logout", current)}
</ul></nav></div></aside>"""


def _scripts() -> str:
    return f"""
<script src="{ASSET}/plugins/jquery/jquery.min.js"></script>
<script src="{ASSET}/plugins/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="{ASSET}/dist/js/adminlte.min.js"></script>"""


def _guest_page(
    title: str,
    body: str,
    status: int,
    width: str = "420px",
    *,
    document_html: str | None = None,
) -> Response:
    if document_html is not None:
        return Response(document_html, status=status, mimetype="text/html")
    # Legacy AdminLTE guest shell (unused by redesigned auth pages).
    document = f"""<!DOCTYPE html>
<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(title)}</title>
<link rel="stylesheet" href="{FA}" crossorigin="anonymous">
<link rel="stylesheet" href="{ASSET}/dist/css/adminlte.min.css">
</head>
<body class="hold-transition login-page"><div class="login-box" style="width:{html.escape(width)}; max-width:calc(100% - 24px);">
<div class="login-logo"><a href="https://mylaptopmarket.com"><img src="{LOGO}" alt="My Laptop Market" style="max-height:100px; width:auto;"></a>
<p class="mt-2"><b>Vendor</b> {html.escape(title.replace("Vendor ", ""))}</p></div>
<div class="card"><div class="card-body login-card-body">{body}</div></div>
</div>{_scripts()}</body></html>"""
    return Response(document, status=status, mimetype="text/html")


def _vendor_panel(title: str, body: str, status: int, bare: bool, extra_scripts: str = "") -> Response:
    session, vendor = _session_vendor()
    current = _request_path()
    vendor_id = int((session or {}).get("vendor_id") or 0)
    unread = _unread_count(vendor_id) if vendor_id else 0
    shop = html.escape(_text(vendor.get("shop_name")) or _text((session or {}).get("shop_name")) or "Vendor Panel")
    inner = body if bare else f'<div class="card"><div class="card-body">{body}</div></div>'
    document = f"""<!DOCTYPE html>
<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{shop}</title>
<link rel="stylesheet" href="{FA}" crossorigin="anonymous">
<link rel="stylesheet" href="{ASSET}/dist/css/adminlte.min.css">
<style>
.content-wrapper form:not(.adminlte-form) label {{ display:block; margin-top:12px; }}
.content-wrapper form:not(.adminlte-form) input:not([type=checkbox]):not([type=radio]):not([type=file]),
.content-wrapper form:not(.adminlte-form) select,
.content-wrapper form:not(.adminlte-form) textarea {{ width:100%; max-width:640px; margin:6px 0; padding:8px; }}
.content-wrapper form:not(.adminlte-form) textarea {{ min-height:90px; }}
.content-wrapper form:not(.adminlte-form) button {{ display:inline-block; }}
</style></head>
<body class="hold-transition sidebar-mini"><div class="wrapper">
<nav class="main-header navbar navbar-expand navbar-white navbar-light"><ul class="navbar-nav"><li class="nav-item">
<a class="nav-link" data-widget="pushmenu" href="#"><i class="fas fa-bars"></i></a></li></ul></nav>
{_warning_banner()}
{_sidebar(vendor, current, unread)}
<div class="content-wrapper"><section class="content p-3">{inner}</section></div>
</div>{_scripts()}{extra_scripts}</body></html>"""
    return Response(document, status=status, mimetype="text/html")


def _page(
    title: str,
    body: str,
    status: int = 200,
    wide: bool = False,
    width: str | None = None,
    extra_scripts: str = "",
    document_html: str | None = None,
) -> Response:
    path = _request_path()
    if document_html is not None or title in {"Vendor Login", "Vendor Register", "Forgot Password"} or path in {
        "/vendor/login",
        "/vendor/register",
        "/vendor/forgot-password",
    }:
        return _guest_page(title, body, status, width or "420px", document_html=document_html)
    if path.startswith("/vendor/"):
        return _vendor_panel(
            title,
            body,
            status,
            bare=title in {
                "Vendor Dashboard",
                "My Profile",
                "Seller Verification",
                "Add Product",
                "Edit Product",
                "Product List",
                "My Invoices",
                "Requirements",
                "Post Requirement",
                "Messages",
                "Search Products",
            },
            extra_scripts=extra_scripts,
        )
    document = f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(title)}</title>
<style>
body {{ font-family: sans-serif; background:#f4f6f9; margin:0; }}
.box {{ max-width:{ width or ("720px" if wide else "380px") }; margin:48px auto; background:#fff; padding:24px; border-radius:8px; }}
.stats {{ display:grid; grid-template-columns:repeat(auto-fit,minmax(140px,1fr)); gap:10px; }}
.stat {{ background:#f4f6f9; border-radius:8px; padding:12px; }}
.stat b {{ font-size:28px; display:block; }}
label {{ display:block; margin-top:12px; }}
input, select, textarea, button, a.btn {{ display:block; width:100%; box-sizing:border-box; margin:8px 0; padding:10px; }}
textarea {{ min-height:90px; }}
img {{ max-height:72px; }}
button, a.btn {{ background:#007bff; color:#fff; border:0; text-align:center; text-decoration:none; }}
a.pay {{ background:#ffc107; color:#111; }}
.error {{ color:#842029; background:#f8d7da; padding:8px; }}
.ok {{ color:#0f5132; background:#d1e7dd; padding:8px; }}
.warn {{ color:#664d03; background:#fff3cd; padding:12px; margin-bottom:16px; }}
.warn button {{ background:#ffc107; color:#111; }}
</style>
</head>
<body><div class="box">{_warning_banner()}{body}</div></body>
</html>"""
    return Response(document, status=status, mimetype="text/html")


def _warning_banner() -> str:
    try:
        from flask import request

        session = vendor_session(request)
    except RuntimeError:
        return ""
    if session is None:
        return ""
    doc = firestore.client().collection("vendors").document(str(session.get("vendor_id") or 0)).get()
    warning = _text((doc.to_dict() or {}).get("admin_warning")) if doc.exists else ""
    if warning == "":
        return ""
    return (
        '<div class="alert alert-warning warn" style="margin:12px 16px 0;">'
        '<strong>Warning from My Laptop Market</strong>'
        f'<div style="margin-top:6px; white-space:pre-wrap">{html.escape(warning)}</div>'
        '<form action="/api/vendor/warning/ack" method="post" style="margin-top:10px;">'
        '<button type="submit" class="btn btn-sm btn-warning">I understand</button></form></div>'
    )


def acknowledge(req) -> Response:
    session = vendor_session(req)
    if session is None:
        response = Response("", status=302)
        response.headers["Location"] = "/api/vendor/login"
        return response
    acknowledge_warning(int(session.get("vendor_id") or 0))
    from urllib.parse import urlparse

    parsed = urlparse(req.headers.get("Referer") or "")
    if parsed.path.startswith("/api/vendor/") and "warning/ack" not in parsed.path:
        target = parsed.path
        if parsed.query:
            target += "?" + parsed.query
    else:
        target = "/api/vendor/dashboard"
    response = Response("", status=302)
    response.headers["Location"] = target
    return response


def _login_body(error: str = "", vendor_id: int = 0, success: str = "") -> str:
    pay = ""
    if SHOW_VENDOR_PAYMENT and vendor_id:
        pay = (
            f'<a class="pay-link" href="/api/subscription/checkout?vendor_id={vendor_id}&source=web">'
            "Pay / Renew Subscription</a>"
        )
    return login_main(error=error, vendor_id=vendor_id, success=success, pay_html=pay)


_FIREBASE_LOGIN_JS = """
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-auth-compat.js"></script>
<script>
var config = __CONFIG__;
function showNote(message, ok) {
  var note = document.getElementById("sign-note");
  if (!note) return;
  note.innerHTML = "";
  if (!message) return;
  var box = document.createElement("div");
  box.className = ok ? "ok" : "error";
  box.textContent = message;
  note.appendChild(box);
}
function finishSignIn(data) {
  if (data && data.success && data.redirect) {
    window.location.href = data.redirect;
    return;
  }
  showNote((data && data.message) || "Could not sign in.", false);
}
function postJson(path, body) {
  return fetch(path, {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    credentials: "same-origin",
    body: JSON.stringify(body)
  }).then(function(response) { return response.json(); });
}
function postToken(token) {
  return postJson("firebase-login", {id_token: token}).then(finishSignIn).catch(function() {
    showNote("Could not sign in.", false);
  });
}
if (config && config.apiKey) {
  firebase.initializeApp(config);
  firebase.auth().getRedirectResult().then(function(result) {
    if (result && result.user) return result.user.getIdToken().then(postToken);
  }).catch(function() {});
  var googleButton = document.getElementById("google-sign-in");
  if (googleButton) {
    googleButton.addEventListener("click", function() {
      var provider = new firebase.auth.GoogleAuthProvider();
      provider.setCustomParameters({prompt: "select_account"});
      firebase.auth().signInWithPopup(provider).then(function(result) {
        return result.user.getIdToken().then(postToken);
      }).catch(function(error) {
        var code = error && error.code ? String(error.code) : "";
        if (code.indexOf("popup") >= 0) {
          firebase.auth().signInWithRedirect(provider);
          return;
        }
        if (code.indexOf("cancelled") >= 0 || code.indexOf("closed") >= 0) {
          showNote("Google sign-in was cancelled.", false);
          return;
        }
        showNote("Google sign-in could not start. Try again.", false);
      });
    });
  }
}
</script>
"""


def _firebase_login_script() -> str:
    import json

    config = _firebase_config()
    if not config.get("apiKey"):
        return ""
    return _FIREBASE_LOGIN_JS.replace("__CONFIG__", json.dumps(config))


def google_auth_page() -> Response:
    page = """<!DOCTYPE html>
<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="refresh" content="0;url=https://mylaptopmarket-app.firebaseapp.com/google-auth.html">
<title>Google sign-in</title></head>
<body style="font-family:sans-serif;padding:24px;color:#0767d8;">
<p>Opening Google…</p>
<script>window.location.replace("https://mylaptopmarket-app.firebaseapp.com/google-auth.html");</script>
</body></html>"""
    return Response(page, status=200, mimetype="text/html")


def login_page(error: str = "", vendor_id: int = 0, success: str = "") -> Response:
    main = _login_body(error, vendor_id, success)
    document = auth_document(
        "Vendor Login",
        main,
        back_href="https://mylaptopmarket.com",
        back_label="Back to home",
        scripts=login_scripts(_firebase_login_script()),
    )
    return _page("Vendor Login", main, document_html=document)


def _field(req, name: str) -> str:
    if req.form and name in req.form:
        return _text(req.form.get(name))
    body = req.get_json(silent=True) or {}
    return _text(body.get(name))


def attach_vendor_cookie(response: Response, vendor: dict) -> Response:
    token = secrets.token_urlsafe(32)
    firestore.client().collection("vendor_sessions").document(token).set(
        {
            "vendor_id": int(vendor.get("id") or 0),
            "username": vendor.get("username"),
            "shop_name": vendor.get("shop_name"),
            "expires_at": datetime.now(timezone.utc) + timedelta(seconds=MAX_AGE),
        }
    )
    response.set_cookie(COOKIE, token, max_age=MAX_AGE, httponly=True, secure=True, samesite="None", path="/")
    return response


def login_auth(req) -> Response:
    username = _field(req, "username")
    password = _field(req, "password")
    result = login(username, password)
    if not result.get("success"):
        vendor = result.get("vendor") or {}
        vendor_id = int(result.get("vendor_id") or 0)
        message = result.get("message") or "Invalid username or password."
        if result.get("code") == "pending_verification" and vendor.get("subscription_start"):
            message = "Aapka account inactive hai."
        show_id = vendor_id if result.get("code") in {"expired", "pending_verification"} else 0
        return login_page(message, show_id)
    vendor = result.get("vendor") or {}
    response = Response("", status=302)
    response.headers["Location"] = "dashboard"
    return attach_vendor_cookie(response, vendor)


def _count(value) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return 0


def dashboard(req) -> Response:
    session = vendor_session(req)
    if session is None:
        response = Response("", status=302)
        response.headers["Location"] = "login"
        return response
    vendor_id = int(session.get("vendor_id") or 0)
    client = firestore.client()
    vendor_doc = client.collection("vendors").document(str(vendor_id)).get()
    vendor = vendor_doc.to_dict() or {} if vendor_doc.exists else {}
    products = []
    active = inactive = 0
    for doc in client.collection("products").where("vendor_id", "==", vendor_id).stream():
        row = doc.to_dict() or {}
        products.append(row)
        if _count(row.get("status")) == 1:
            active += 1
        else:
            inactive += 1
    products.sort(key=lambda row: int(row.get("id") or 0), reverse=True)
    low_rows = [row for row in products if _count(row.get("stock")) <= 5]
    messages = []
    unread = 0
    for doc in client.collection("messages").where("receiver_id", "==", vendor_id).stream():
        row = doc.to_dict() or {}
        messages.append(row)
        if row.get("is_read") not in (1, True, "1"):
            unread += 1
    messages.sort(key=lambda row: _text(row.get("created_at")), reverse=True)
    invoices = sum(1 for _ in client.collection("invoices").where("vendor_id", "==", vendor_id).stream())
    shop = html.escape(_text(vendor.get("shop_name")) or _text(session.get("shop_name")) or _text(session.get("username")))
    logo = html.escape(_logo_src(vendor))
    trusted = _count(vendor.get("gst_verified")) == 1 and _text(vendor.get("shop_photo")) != ""
    badge_bg = "#ECFDF5" if trusted else "#FFF7ED"
    badge_color = "#166534" if trusted else "#9A3412"
    badge_border = "#86EFAC" if trusted else "#FDBA74"
    badge_icon = "fa-check-circle" if trusted else "fa-clock"
    badge = "Verified Vendor" if trusted else "Pending Verification"
    status_badge = '<span class="badge badge-success">Active</span>' if _count(vendor.get("status")) == 1 else '<span class="badge badge-danger">Inactive</span>'
    end = _text(vendor.get("subscription_end"))[:10]
    days = _days_left(vendor, date.today()) if end else None
    expiring = ""
    subscription = ""
    if SHOW_VENDOR_PAYMENT and end:
        try:
            till = date.fromisoformat(end).strftime("%d %b %Y")
        except ValueError:
            till = end
        if days is not None and days <= 5:
            note = f'<p class="text-danger mb-3"><i class="fas fa-exclamation-triangle"></i> Aapki subscription sirf {days} din me expire hone wali hai!</p>'
            card = "card-warning"
            expiring = f'<div class="alert alert-warning alert-dismissible"><button type="button" class="close" data-dismiss="alert">&times;</button><i class="fas fa-exclamation-triangle"></i> Subscription expires in <strong>{days} day(s)</strong>. <a href="/api/subscription/checkout?vendor_id={vendor_id}&source=web" class="alert-link">Renew now</a></div>'
        else:
            note = f'<p class="text-success mb-3"><i class="fas fa-check-circle"></i> Active ({days} din baaki hain)</p>'
            card = "card-success"
        subscription = f"""
<div class="col-md-6"><div class="card {card} card-outline"><div class="card-body">
<h5>Subscription Status</h5><hr>
<p class="mb-1">Valid Till: <strong>{html.escape(till)}</strong></p>
{note}
<a href="/api/subscription/checkout?vendor_id={vendor_id}&source=web" class="btn btn-primary btn-sm"><i class="fas fa-sync-alt"></i> Renew Subscription</a>
</div></div></div>"""
    if unread:
        expiring += f'<div class="alert alert-primary alert-dismissible"><button type="button" class="close" data-dismiss="alert">&times;</button><i class="fas fa-comment-dots"></i> You have <strong>{unread}</strong> unread message(s). <a href="{VENDOR}/messages" class="alert-link">Open inbox</a></div>'
    message_rows = []
    for row in messages[:6]:
        sender_id = str(row.get("sender_id") or "")
        sender_doc = client.collection("vendors").document(sender_id).get() if sender_id else None
        sender = sender_doc.to_dict() or {} if sender_doc is not None and sender_doc.exists else {}
        sender_name = html.escape(_text(sender.get("shop_name")) or _text(sender.get("username")) or "Vendor")
        text = html.escape(_text(row.get("message"))[:50])
        read = '<span class="badge badge-danger">Unread</span>' if row.get("is_read") not in (1, True, "1") else '<span class="badge badge-success">Read</span>'
        message_rows.append(f"<tr><td><strong>{sender_name}</strong></td><td><small>{text}</small></td><td>{read}</td></tr>")
    if not message_rows:
        message_rows.append('<tr><td colspan="3" class="text-center text-muted py-3">No messages yet</td></tr>')
    low_html = []
    for row in low_rows[:8]:
        low_html.append(
            "<tr><td><span class=\"badge badge-info\">B2B</span></td>"
            f"<td>{html.escape(_text(row.get('product_name')) or 'Product')}</td>"
            f"<td><span class=\"badge badge-danger\">{_count(row.get('stock'))}</span></td></tr>"
        )
    if not low_html:
        low_html.append('<tr><td colspan="3" class="text-center text-muted py-3">All stocks look healthy</td></tr>')
    recent_html = []
    for row in products[:6]:
        try:
            price = f"₹{int(float(row.get('price') or 0)):,}"
        except (TypeError, ValueError):
            price = "—"
        state = '<span class="badge badge-success">Active</span>' if _count(row.get("status")) == 1 else '<span class="badge badge-secondary">Inactive</span>'
        recent_html.append(
            f"<tr><td>{html.escape(_text(row.get('product_name'))[:28])}</td><td>{price}</td><td>{_count(row.get('stock'))}</td><td>{state}</td></tr>"
        )
    if not recent_html:
        recent_html.append(f'<tr><td colspan="4" class="text-center text-muted py-3">No products yet — <a href="{VENDOR}/add_product">add one</a></td></tr>')
    body = f"""
<link rel="stylesheet" href="{ASSET}/css/home-orbit.css?v=20260919c">
<link rel="stylesheet" href="{ASSET}/css/vendor-orbit.css?v=20260919c">
<div class="d-flex flex-wrap align-items-center justify-content-between mb-3">
<div>
<h3 class="mb-1 d-flex align-items-center flex-wrap" style="gap:12px;"><span>Welcome</span>
<span class="d-inline-flex align-items-center" style="gap:8px;"><img src="{logo}" alt="" style="width:44px;height:44px;border-radius:50%;object-fit:cover;" onerror="this.onerror=null;this.src='{LOGO}';"><span>{shop}</span></span>
<span>👨‍💼</span></h3>
<p class="text-muted mb-2">Your business snapshot — products, leads, messages & subscription</p>
<a href="{VENDOR}/seller-verification" class="badge" style="background:{badge_bg};color:{badge_color};border:1px solid {badge_border};font-size:13px;padding:6px 10px;"><i class="fas {badge_icon}"></i> {badge}</a>
</div>
<div class="mt-2 mt-md-0">
<a href="{VENDOR}/add_product" class="btn btn-primary btn-sm mr-1 mb-1"><i class="fas fa-plus"></i> Add B2B Product</a>
<a href="{VENDOR}/messages" class="btn btn-info btn-sm mb-1"><i class="fas fa-comments"></i> Messages{f' <span class="badge badge-light">{unread}</span>' if unread else ''}</a>
</div></div>
<section class="home-orbit vendor-orbit" id="homeOrbit">
<div class="home-orbit-heading"><h2 class="vendor-orbit-title">LIVE CATALOG</h2></div>
<p class="home-orbit-cap" id="orbitCap" hidden></p>
<div class="home-orbit-stage" id="homeOrbitStage" aria-label="3D product orbit">
<div class="orbit-scene"><div class="orbit-rig" id="orbitRig">
<div class="orbit-laptop" id="orbitLaptop"><img src="{ASSET}/images/orbit-laptop.png?v=2" alt="" onerror="this.onerror=null;this.src='{ASSET}/images/no-product.svg'"></div>
<div class="orbit-ring orbit-ring-top" id="orbitRingTop"></div>
<div class="orbit-ring orbit-ring-bottom" id="orbitRingBottom"></div>
</div></div></div>
<div class="home-orbit-actions"><p class="orbit-hint">Drag to spin · hover to pause · click a product for details</p>
<a class="orbit-browse-btn" id="orbitBrowse" href="https://mylaptopmarket.com/products.html" target="_blank" rel="noopener">Browse all products <i class="fas fa-arrow-right"></i></a>
</div></section>
{expiring}
<div class="row mb-3">{subscription}
<div class="col-md-6"><div class="card card-outline card-secondary"><div class="card-body">
<h5>Profile Snapshot</h5><hr>
<div class="mb-2 d-flex align-items-center" style="gap:8px;"><img src="{logo}" alt="" style="width:48px;height:48px;border-radius:50%;object-fit:cover;"><strong>{shop}</strong></div>
<p class="mb-1"><strong>Username:</strong> {html.escape(_text(vendor.get("username")) or "—")}</p>
<p class="mb-1"><strong>Location:</strong> {html.escape(_text(vendor.get("location")) or "—")}</p>
<p class="mb-2"><strong>Status:</strong> {status_badge}</p>
<a href="{VENDOR}/profile" class="btn btn-sm btn-outline-primary"><i class="fas fa-image"></i> Update Logo / Profile</a>
</div></div></div></div>
<div class="row">
<div class="col-lg-3 col-6"><div class="small-box bg-info"><div class="inner"><h3>{active}</h3><p>B2B Active Products</p></div><div class="icon"><i class="fas fa-box-open"></i></div><a href="{VENDOR}/product_list" class="small-box-footer">View list <i class="fas fa-arrow-circle-right"></i></a></div></div>
<div class="col-lg-3 col-6"><div class="small-box bg-secondary"><div class="inner"><h3>{inactive}</h3><p>B2B Inactive Products</p></div><div class="icon"><i class="fas fa-box"></i></div><a href="{VENDOR}/product_list" class="small-box-footer">Manage <i class="fas fa-arrow-circle-right"></i></a></div></div>
<div class="col-lg-3 col-6"><div class="small-box bg-primary"><div class="inner"><h3>{unread}</h3><p>Unread Messages</p></div><div class="icon"><i class="fas fa-comments"></i></div><a href="{VENDOR}/messages" class="small-box-footer">Inbox <i class="fas fa-arrow-circle-right"></i></a></div></div>
<div class="col-lg-3 col-6"><div class="small-box bg-danger"><div class="inner"><h3>{len(low_rows)}</h3><p>Low Stock Items</p></div><div class="icon"><i class="fas fa-exclamation-circle"></i></div><a href="#low-stock" class="small-box-footer">Check stock <i class="fas fa-arrow-circle-right"></i></a></div></div>
<div class="col-lg-3 col-6"><div class="small-box bg-dark"><div class="inner"><h3>{invoices}</h3><p>My Invoices</p></div><div class="icon"><i class="fas fa-file-invoice-dollar"></i></div><a href="{VENDOR}/invoices" class="small-box-footer">View invoices <i class="fas fa-arrow-circle-right"></i></a></div></div>
</div>
<div class="card card-outline card-primary mb-3"><div class="card-header"><h3 class="card-title"><i class="fas fa-bolt"></i> Quick Actions</h3></div>
<div class="card-body">
<a href="{VENDOR}/add_product" class="btn btn-outline-primary btn-sm mr-1 mb-1"><i class="fas fa-plus"></i> Add B2B Product</a>
<a href="{VENDOR}/product_list" class="btn btn-outline-secondary btn-sm mr-1 mb-1"><i class="fas fa-list"></i> B2B Product List</a>
<a href="{VENDOR}/messages" class="btn btn-outline-info btn-sm mr-1 mb-1"><i class="fas fa-comments"></i> Messages</a>
<a href="{VENDOR}/search_products" class="btn btn-outline-dark btn-sm mr-1 mb-1"><i class="fas fa-search"></i> Search Products</a>
<a href="{VENDOR}/invoices" class="btn btn-outline-danger btn-sm mb-1"><i class="fas fa-file-invoice"></i> Invoices</a>
</div></div>
<div class="row">
<div class="col-12"><div class="card"><div class="card-header"><h3 class="card-title"><i class="fas fa-comments text-primary"></i> Recent Messages</h3><div class="card-tools"><a href="{VENDOR}/messages" class="btn btn-tool btn-sm">Open inbox</a></div></div>
<div class="card-body p-0"><table class="table table-sm table-striped mb-0"><thead><tr><th>From</th><th>Message</th><th>Read</th></tr></thead><tbody>{''.join(message_rows)}</tbody></table></div></div></div>
</div>
<div class="row" id="low-stock">
<div class="col-lg-6"><div class="card"><div class="card-header"><h3 class="card-title"><i class="fas fa-exclamation-triangle text-danger"></i> Low Stock Alert (≤ 5)</h3></div>
<div class="card-body p-0"><table class="table table-sm mb-0"><thead><tr><th>Type</th><th>Product</th><th>Stock</th></tr></thead><tbody>{''.join(low_html)}</tbody></table></div></div></div>
<div class="col-lg-6"><div class="card"><div class="card-header"><h3 class="card-title"><i class="fas fa-boxes text-info"></i> Recent B2B Products</h3><div class="card-tools"><a href="{VENDOR}/product_list" class="btn btn-tool btn-sm">View all</a></div></div>
<div class="card-body p-0"><table class="table table-sm table-striped mb-0"><thead><tr><th>Product</th><th>Price</th><th>Stock</th><th>Status</th></tr></thead><tbody>{''.join(recent_html)}</tbody></table></div></div></div>
</div>
<script src="{ASSET}/js/vendor-orbit.js"></script>
"""
    return _page("Vendor Dashboard", body, wide=True)


def logout(req) -> Response:
    session = vendor_session(req)
    if session is not None:
        firestore.client().collection("vendor_sessions").document(session["_token"]).delete()
    response = Response("", status=302)
    response.headers["Location"] = "login"
    response.set_cookie(COOKIE, "", max_age=0, httponly=True, secure=True, samesite="None", path="/")
    return response


def phone_auth_page(mobile: str) -> Response:
    import json
    import re

    digits = re.sub(r"\D", "", mobile or "")
    if digits.startswith("91") and len(digits) == 12:
        digits = digits[2:]
    config = _firebase_config()
    if not re.fullmatch(r"[6-9][0-9]{9}", digits) or not config.get("apiKey"):
        return Response("<p>SMS verification is not ready.</p>", status=200, mimetype="text/html")
    page = """<!DOCTYPE html>
<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Verify mobile</title></head>
<body style="font-family:sans-serif;padding:16px;">
<p>Tick the check. The SMS OTP will be sent to your mobile.</p>
<div id="recaptcha"></div>
<p id="note"></p>
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-auth-compat.js"></script>
<script>
var config = __CONFIG__;
var mobile = __MOBILE__;
firebase.initializeApp(config);
var confirmation = null;
function tell(payload) {
  if (window.Android) {
    if (payload.type === "sent") Android.onSent();
    else if (payload.type === "token") Android.onToken(payload.token || "");
    else Android.onError(payload.message || "Could not verify this mobile number.");
  }
  if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.mlm) {
    window.webkit.messageHandlers.mlm.postMessage(payload);
  }
}
function fail(message) {
  document.getElementById("note").textContent = message;
  tell({type: "error", message: message});
}
function smsFailText(error) {
  var code = error && error.code ? String(error.code) : "";
  var blob = (code + " " + (error && error.message ? String(error.message) : "")).toUpperCase();
  if (blob.indexOf("TOO_MANY") >= 0 || blob.indexOf("TOO-MANY") >= 0) {
    return "Firebase has paused SMS for this number. Use a different mobile number.";
  }
  if (blob.indexOf("QUOTA") >= 0) return "SMS limit reached for now. Try again later.";
  if (blob.indexOf("CAPTCHA") >= 0 || blob.indexOf("INVALID-APP-CREDENTIAL") >= 0 || blob.indexOf("MISSING-CLIENT") >= 0) {
    return "The check failed. Close this and tap Send SMS again.";
  }
  if (blob.indexOf("OPERATION_NOT_ALLOWED") >= 0 || blob.indexOf("OPERATION-NOT-ALLOWED") >= 0) {
    return "SMS is blocked for this number right now.";
  }
  if (blob.indexOf("NETWORK") >= 0) return "Network problem. Check internet and tap Send SMS again.";
  if (code.indexOf("auth/") === 0) return "Could not send the SMS OTP (" + code.slice(5) + ").";
  return "Could not send the SMS OTP.";
}
function mountCheck() {
  var box = document.getElementById("recaptcha");
  if (box) box.innerHTML = "";
  verifier = new firebase.auth.RecaptchaVerifier("recaptcha", {
    size: "normal",
    callback: function() {
      firebase.auth().signInWithPhoneNumber("+91" + mobile, verifier).then(function(result) {
        confirmation = result;
        document.getElementById("note").textContent = "OTP sent to your mobile number.";
        tell({type: "sent"});
      }).catch(function(error) {
        fail(smsFailText(error));
        try { verifier.clear(); } catch (e) {}
        mountCheck();
      });
    },
    "error-callback": function() {
      fail("The check could not load. Close this and tap Send SMS again.");
    }
  });
  verifier.render().catch(function() {
    fail("The check could not load. Close this and tap Send SMS again.");
  });
}
var verifier = null;
mountCheck();
window.confirmCode = function(code) {
  if (!confirmation) { fail("Send an OTP to this mobile number first."); return; }
  confirmation.confirm(code).then(function(result) {
    return result.user.getIdToken();
  }).then(function(token) {
    firebase.auth().signOut();
    tell({type: "token", token: token});
  }).catch(function() { fail("Incorrect OTP."); });
};
</script>
</body></html>
"""
    page = page.replace("__CONFIG__", json.dumps(config)).replace("__MOBILE__", json.dumps(digits))
    return Response(page, status=200, mimetype="text/html")


def register_page(error: str = "", values: dict | None = None, success: str = "") -> Response:
    values = values or {}
    main = register_main(error=error, values=values, success=success, otp_enabled=SHOW_VENDOR_OTP)
    scripts = ""
    if SHOW_VENDOR_OTP:
        scripts += mobile_firebase_scripts(_firebase_config())
    scripts += register_scripts(_firebase_config(), otp_enabled=SHOW_VENDOR_OTP)
    document = auth_document(
        "Vendor Register",
        main,
        back_href="login",
        back_label="Back to login",
        step=(1, 3),
        footer=register_footer(),
        scripts=scripts,
    )
    return _page("Vendor Register", main, width="460px", document_html=document)


def _register_values(req) -> dict:
    return {
        name: _field(req, name)
        for name in (
            "shop_name",
            "username",
            "city",
            "state",
            "mobile",
            "email",
            "password",
            "email_otp",
            "mobile_otp",
            "gstin",
        )
    }


def _json_message(ok: bool, message: str, status: int = 200) -> Response:
    import json as _json_mod

    body = _json_mod.dumps({"success": ok, "ok": ok, "message": message})
    return Response(body, status=status if ok else 422, mimetype="application/json")


def register_send_otp(req) -> Response:
    if req.is_json:
        data = req.get_json(silent=True) or {}
        result = send_email_otp(_text(data.get("email")))
        return _json_message(bool(result.get("ok")), result.get("message") or ("OTP sent." if result.get("ok") else "Could not send the OTP."))
    values = _register_values(req)
    result = send_email_otp(values.get("email") or "")
    if result.get("ok"):
        return register_page(values=values, success=result.get("message") or "OTP sent.")
    return register_page(error=result.get("message") or "Could not send the OTP.", values=values)


def register_verify_otp(req) -> Response:
    if req.is_json:
        data = req.get_json(silent=True) or {}
        result = confirm_email_otp(_text(data.get("email")), _text(data.get("email_otp") or data.get("otp")))
        return _json_message(
            bool(result.get("ok")),
            result.get("message") or ("Email verified." if result.get("ok") else "Could not verify this email."),
        )
    values = _register_values(req)
    result = confirm_email_otp(values.get("email") or "", values.get("email_otp") or "")
    if result.get("ok"):
        return register_page(values=values, success=result.get("message") or "Email verified.")
    return register_page(error=result.get("message") or "Could not verify this email.", values=values)


def register_send_mobile_otp(req) -> Response:
    values = _register_values(req)
    return register_page(
        error="Use Send SMS, complete the check, then enter the code and tap Verify.",
        values=values,
    )


def register_verify_mobile_otp(req) -> Response:
    if req.is_json:
        import json as _json_mod

        data = req.get_json(silent=True) or {}
        result = confirm_mobile_otp(data.get("mobile") or "", data.get("firebase_token") or "")
        body = _json_mod.dumps({"success": bool(result.get("ok")), "message": result.get("message") or ""})
        return Response(body, status=200 if result.get("ok") else 422, mimetype="application/json")
    values = _register_values(req)
    result = confirm_mobile_otp(values.get("mobile") or "", values.get("firebase_token") or "")
    if result.get("ok"):
        return register_page(values=values, success=result.get("message") or "Mobile number verified.")
    return register_page(error=result.get("message") or "Could not verify this mobile number.", values=values)


def register_store(req) -> Response:
    body = _register_values(req)
    result = register(body)
    if not result.get("success"):
        return register_page(result.get("message") or "Could not register.", body)
    response = Response("", status=302)
    if SHOW_VENDOR_PAYMENT:
        response.headers["Location"] = f"/api/subscription/checkout?vendor_id={int(result.get('vendor_id') or 0)}&source=web"
    else:
        response.headers["Location"] = "login"
    return response


def forgot_page(email: str = "", error: str = "", success: str = "") -> Response:
    main = forgot_main(email=email, error=error, success=success)
    document = auth_document(
        "Forgot Password",
        main,
        back_href="login",
        back_label="Back to login",
    )
    return _page("Forgot Password", main, document_html=document)


def forgot_send(req) -> Response:
    email = _field(req, "email")
    result, _status = forgot_password({"email": email})
    if result.get("success"):
        return forgot_page(email, success=result.get("message") or "OTP sent.")
    return forgot_page(email, error=result.get("message") or "Could not send the OTP.")


def reset_store(req) -> Response:
    email = _field(req, "email")
    otp = _field(req, "otp")
    password = _field(req, "password")
    confirm = _field(req, "password_confirmation")
    if confirm != password:
        return forgot_page(email, error="The password confirmation does not match.")
    result, _status = reset_password({"email": email, "otp": otp, "password": password})
    if result.get("success"):
        response = Response("", status=302)
        response.headers["Location"] = "login?reset=1"
        return response
    return forgot_page(email, error=result.get("message") or "Could not update the password.")


def auth_status(req) -> dict:
    session = vendor_session(req)
    return {
        "status": True,
        "authenticated": session is not None,
        "vendor": _public(session) if session is not None else None,
    }
