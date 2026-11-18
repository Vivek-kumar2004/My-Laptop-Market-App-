from __future__ import annotations

import html
from datetime import date
from urllib.parse import quote

from firebase_admin import firestore
from flask import Response

from accounts import STATES, profile_update, shop_trust
from auth import _days_left
from subscription import SHOW_VENDOR_PAYMENT
from web_session import _page, vendor_session

SITE = "https://mylaptopmarket.com/mylaptop"
LOGO_LIMIT = 2 * 1024 * 1024


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _redirect(location: str) -> Response:
    response = Response("", status=302)
    response.headers["Location"] = location
    return response


def _login() -> Response:
    return _redirect("login")


def _vendor(vendor_id: int) -> dict | None:
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}
    data["_ref"] = doc.reference
    return data


def _picked(req, vendor: dict, name: str, fallback: str = "") -> str:
    if req.form and name in req.form:
        return _text(req.form.get(name))
    return _text(vendor.get(name)) or fallback


def _states(selected: str) -> str:
    options = ['<option value="">Select state</option>']
    for name in STATES:
        mark = " selected" if name == selected else ""
        options.append(f'<option value="{html.escape(name)}"{mark}>{html.escape(name)}</option>')
    return "".join(options)


def _logo_src(vendor: dict) -> str:
    logo = _text(vendor.get("logo_url")) or _text(vendor.get("logo"))
    if logo == "":
        return ""
    if logo.startswith("http"):
        return logo
    return f"{SITE}/assets/uploads/vendors/{logo.split('/')[-1]}"


def _form(req, vendor: dict, notice: str = "", error: str = "") -> str:
    shop = _picked(req, vendor, "shop_name")
    mobile = _picked(req, vendor, "mobile")
    city = _picked(req, vendor, "city", _text(vendor.get("location")))
    state = _picked(req, vendor, "state")
    logo = _logo_src(vendor) or "https://storage.googleapis.com/mylaptopmarket-app-web/images/mylaptoplogo.png"
    shop_name = html.escape(shop or "Vendor")
    trusted = int(vendor.get("gst_verified") or 0) == 1 and _text(vendor.get("shop_photo")) != ""
    stamp = ""
    if trusted:
        stamp = (
            '<div class="mt-2"><span style="display:inline-flex;align-items:center;gap:4px;'
            'background:#FFF7D6;color:#92400E;border:1px solid #E8B931;border-radius:999px;'
            'padding:2px 10px;font-size:12px;font-weight:700;">'
            '<i class="fas fa-check-circle"></i> Verified</span></div>'
        )
    alert = ""
    if notice:
        alert += f'<div class="alert alert-success">{html.escape(notice)}</div>'
    if error:
        alert += f'<div class="alert alert-danger">{html.escape(error)}</div>'
    remove = ""
    if _text(vendor.get("logo")):
        remove = """
<div class="form-group">
<div class="custom-control custom-checkbox">
<input type="checkbox" class="custom-control-input" id="removeLogo" name="remove_logo" value="1">
<label class="custom-control-label text-danger" for="removeLogo">Remove current logo</label>
</div>
</div>"""
    return f"""
<div class="d-flex align-items-center justify-content-between mb-3">
<div>
<h3 class="mb-1">My Profile / Shop Branding</h3>
<p class="text-muted mb-0">Upload your shop logo — it will appear in the sidebar and wherever your name shows.</p>
</div>
<a href="/api/vendor/dashboard" class="btn btn-secondary btn-sm"><i class="fas fa-arrow-left"></i> Dashboard</a>
</div>
{alert}
<div class="row">
<div class="col-lg-4">
<div class="card card-outline card-primary">
<div class="card-header"><h3 class="card-title">Preview</h3></div>
<div class="card-body text-center py-4">
<span class="d-inline-flex align-items-center flex-column" style="gap:10px;">
<img src="{html.escape(logo)}" alt="{shop_name}" class="elevation-2" style="width:96px;height:96px;object-fit:cover;border-radius:50%;background:#fff;">
<h4 class="mb-0">{shop_name}</h4>
</span>
{stamp}
<a href="/api/vendor/seller-verification" class="btn btn-outline-primary btn-sm mt-3">Seller Verification</a>
<p class="text-muted mt-3 mb-0" style="font-size:13px;">Sidebar and welcome areas use this branding.</p>
</div>
</div>
</div>
<div class="col-lg-8">
<div class="card card-primary">
<div class="card-header"><h3 class="card-title">Update Shop Details</h3></div>
<form class="adminlte-form" action="/api/vendor/profile" method="post" enctype="multipart/form-data">
<div class="card-body">
<div class="form-group">
<label>Shop Name</label>
<input type="text" name="shop_name" class="form-control" value="{html.escape(shop)}" required>
</div>
<div class="row">
<div class="col-md-6"><div class="form-group"><label>Mobile</label>
<input type="text" name="mobile" class="form-control" maxlength="10" value="{html.escape(mobile)}"></div></div>
<div class="col-md-6"><div class="form-group"><label>City</label>
<input type="text" name="city" class="form-control" value="{html.escape(city)}"></div></div>
<div class="col-md-6"><div class="form-group"><label>State</label>
<select name="state" class="form-control">{_states(state)}</select></div></div>
</div>
<div class="form-group">
<label>Shop Logo</label>
<div class="custom-file">
<input type="file" name="logo" class="custom-file-input" id="logoInput" accept="image/*">
<label class="custom-file-label" for="logoInput">Choose image (JPG/PNG, max 2MB)</label>
</div>
<small class="text-muted">Recommended: square logo, at least 200×200px</small>
</div>
{remove}
</div>
<div class="card-footer">
<button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Save Changes</button>
</div>
</form>
</div>
</div>
</div>
<script>
document.getElementById("logoInput").addEventListener("change", function (event) {{
  var label = document.querySelector(".custom-file-label");
  if (label && event.target.files[0]) label.textContent = event.target.files[0].name;
}});
</script>
"""


def _too_big(upload) -> bool:
    if upload is None or not upload.filename:
        return False
    upload.stream.seek(0, 2)
    size = upload.stream.tell()
    upload.stream.seek(0)
    return size > LOGO_LIMIT


def profile_page(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor = _vendor(int(session.get("vendor_id") or 0))
    if vendor is None:
        return _login()
    return _page(
        "My Profile",
        _form(req, vendor, _text(req.args.get("notice")), _text(req.args.get("error"))),
        wide=True,
    )


def profile_store(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    vendor = _vendor(vendor_id)
    if vendor is None:
        return _login()
    if _text(req.form.get("shop_name") if req.form else "") == "":
        return _page("My Profile", _form(req, vendor, error="The shop name field is required."), wide=True)
    logo = req.files.get("logo") if req.files else None
    if _too_big(logo):
        return _page("My Profile", _form(req, vendor, error="Logo must be 2 MB or smaller."), wide=True)
    result = profile_update(req, vendor_id)
    if not result.get("success"):
        return _page("My Profile", _form(req, vendor, error=_text(result.get("message")) or "Could not save profile."), wide=True)
    token = _text(session.get("_token"))
    shop = _text((result.get("vendor") or {}).get("shop_name"))
    if token and shop:
        firestore.client().collection("vendor_sessions").document(token).update({"shop_name": shop})
    return _redirect("profile?notice=" + quote("Profile updated successfully."))


def _photo_src(vendor: dict) -> str:
    photo = _text(vendor.get("shop_photo"))
    if photo == "":
        return ""
    if photo.startswith("http"):
        return photo
    return f"{SITE}/assets/uploads/vendors/{photo.split('/')[-1]}"


def _registry_box(vendor: dict, gstin: str) -> str:
    details = vendor.get("gst_details") if isinstance(vendor.get("gst_details"), dict) else None
    stored = _text(vendor.get("gstin"))
    if not details or stored == "" or gstin.upper() != stored.upper():
        return ""
    active = _text(details.get("status")).lower() == "active"
    background = "#F0FDF4" if active else "#FFF7ED"
    border = "#86EFAC" if active else "#FDBA74"
    rows = []
    labels = (
        ("Trade name", "trade_name"),
        ("Legal name", "legal_name"),
        ("Constitution", "constitution"),
        ("Taxpayer type", "taxpayer_type"),
        ("Registered on", "registration_date"),
        ("State jurisdiction", "state_jurisdiction"),
        ("Centre jurisdiction", "central_jurisdiction"),
        ("Address", "address"),
    )
    for label, key in labels:
        value = _text(details.get(key))
        if value:
            rows.append(
                f'<div class="mb-1"><div class="text-muted" style="font-size:12px;">{label}</div>'
                f"<div>{html.escape(value)}</div></div>"
            )
    status = html.escape(_text(details.get("status")))
    return f"""
<div class="p-3 mb-3" style="border-radius:12px;background:{background};border:1px solid {border};">
<div class="d-flex justify-content-between mb-2"><strong>GST registry</strong><strong>{status}</strong></div>
{''.join(rows)}
</div>"""


def verification_form(req, vendor: dict, notice: str = "", error: str = "") -> str:
    vendor_id = int(vendor.get("id") or 0)
    gst_done = int(vendor.get("gst_verified") or 0) == 1
    photo = _photo_src(vendor)
    photo_done = photo != ""
    trusted = gst_done and photo_done
    heading = "You're a verified seller" if trusted else "Verification pending"
    icon = "fa-check-circle text-success" if trusted else "fa-user-check text-warning"
    stamp = ""
    if trusted:
        stamp = (
            '<div class="mt-2"><span style="display:inline-flex;align-items:center;gap:4px;'
            'background:#FFF7D6;color:#92400E;border:1px solid #E8B931;border-radius:999px;'
            'padding:2px 10px;font-size:12px;font-weight:700;">'
            '<i class="fas fa-check-circle"></i> Verified</span></div>'
        )
    gst_icon = "fa-check-circle text-success" if gst_done else "fa-circle text-warning"
    photo_icon = "fa-check-circle text-success" if photo_done else "fa-circle text-warning"
    gst_tone = "text-success" if gst_done else "text-warning"
    photo_tone = "text-success" if photo_done else "text-warning"
    gstin = _picked(req, vendor, "gstin")
    end = _text(vendor.get("subscription_end"))[:10]
    active = int(vendor.get("status") or 0) == 1
    if active:
        extra = ""
        if end:
            try:
                till = date.fromisoformat(end).strftime("%d %b %Y")
            except ValueError:
                till = end
            days = _days_left(vendor, date.today())
            extra = f" till {html.escape(till)}"
            if days > 0:
                extra += f" · {days} days left"
        subscription = f'<p class="mb-2">Subscription active{extra}.</p>'
        pay_label = "Renew Subscription"
        pay_class = "btn-primary"
    else:
        subscription = '<p class="mb-2">Products live karne ke liye ₹1 / 30 din ki subscription alag se chahiye.</p>'
        pay_label = "Pay / Activate Subscription"
        pay_class = "btn-warning"
    alert = ""
    if notice:
        alert += f'<div class="alert alert-success">{html.escape(notice)}</div>'
    if error:
        alert += f'<div class="alert alert-danger">{html.escape(error)}</div>'
    picture = ""
    if photo:
        picture = (
            f'<img src="{html.escape(photo)}" alt="Shop photo" class="img-fluid mb-3" '
            'style="max-height:180px;border-radius:12px;object-fit:cover;">'
        )
    return f"""
<div class="d-flex align-items-center justify-content-between mb-3">
<h3 class="mb-0">Seller Verification</h3>
<a href="/api/vendor/dashboard" class="btn btn-secondary btn-sm"><i class="fas fa-arrow-left"></i> Dashboard</a>
</div>
{alert}
<div class="row">
<div class="col-lg-8">
<div class="card"><div class="card-body text-center">
<i class="fas {icon}" style="font-size:48px;"></i>
<h4 class="mt-3 mb-2">{heading}</h4>
<p class="text-muted mb-2">Verified tab hota hai jab active GSTIN aur shop photo dono save ho jayein.</p>
{stamp}
</div></div>
<div class="card"><div class="card-body">
<div class="d-flex justify-content-between align-items-center mb-2">
<span><i class="fas {gst_icon}"></i> Active GSTIN</span>
<strong class="{gst_tone}">{"Done" if gst_done else "Required"}</strong>
</div>
<div class="d-flex justify-content-between align-items-center">
<span><i class="fas {photo_icon}"></i> Shop photo</span>
<strong class="{photo_tone}">{"Done" if photo_done else "Required"}</strong>
</div>
</div></div>
<div class="card card-primary">
<div class="card-header"><h3 class="card-title">GST and shop photo</h3></div>
<form class="adminlte-form" action="/api/vendor/seller-verification" method="post" enctype="multipart/form-data">
<div class="card-body">
<div class="form-group">
<label>GSTIN</label>
<input type="text" name="gstin" class="form-control" maxlength="15" value="{html.escape(gstin)}" style="text-transform:uppercase;" placeholder="15-character GSTIN">
<small class="text-muted">We check this number on the GST registry. An active GSTIN plus a shop photo adds the verified stamp.</small>
</div>
{_registry_box(vendor, gstin)}
<div class="form-group">
<label>Shop photo</label>
<input type="file" name="shop_photo" class="form-control" accept="image/*">
<small class="text-muted">JPG, PNG, WEBP, or HEIC, up to 5 MB.</small>
</div>
{picture}
</div>
<div class="card-footer">
<button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Save GST and shop photo</button>
</div>
</form>
</div>
</div>
<div class="col-lg-4">
<div class="card">
<div class="card-header"><h3 class="card-title">{"Subscription" if SHOW_VENDOR_PAYMENT else "Status"}</h3></div>
<div class="card-body">
{subscription if SHOW_VENDOR_PAYMENT else ""}
{f'<a href="/api/subscription/checkout?vendor_id={vendor_id}&source=web" class="btn btn-block {pay_class} mb-2">{pay_label}</a>' if SHOW_VENDOR_PAYMENT else ""}
<a href="/api/vendor/seller-verification" class="btn btn-outline-secondary btn-block">Refresh Status</a>
</div>
</div>
</div>
</div>
"""


def verification_page(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor = _vendor(int(session.get("vendor_id") or 0))
    if vendor is None:
        return _login()
    return _page(
        "Seller Verification",
        verification_form(req, vendor, _text(req.args.get("notice")), _text(req.args.get("error"))),
        wide=True,
    )


def verification_store(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    vendor = _vendor(vendor_id)
    if vendor is None:
        return _login()
    result = shop_trust(req, vendor_id)
    if not result.get("success"):
        return _page(
            "Seller Verification",
            verification_form(req, vendor, error=_text(result.get("message")) or "Could not save."),
            wide=True,
        )
    return _redirect("seller-verification?notice=" + quote(_text(result.get("message")) or "Saved."))
