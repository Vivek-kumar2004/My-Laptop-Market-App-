import json
import re
import secrets
from datetime import date, datetime, timedelta, timezone

import bcrypt
from firebase_admin import auth as firebase_auth
from firebase_admin import firestore

SITE = "https://mylaptopmarket.com/mylaptop"
DEFAULT_LOGO = "https://storage.googleapis.com/mylaptopmarket-app-web/images/mylaptoplogo.png"

GST_KEYS = (
    "legal_name",
    "trade_name",
    "status",
    "constitution",
    "taxpayer_type",
    "registration_date",
    "state_jurisdiction",
    "central_jurisdiction",
    "address",
)


def _text(value) -> str:
    if value is None:
        return ""
    return str(value)


def _file_url(raw) -> str | None:
    text = _text(raw).strip()
    if not text:
        return None
    if text.startswith("http://") or text.startswith("https://"):
        return text.split("?")[0]
    name = text.rstrip("/").split("/")[-1]
    if not name:
        return None
    return f"{SITE}/assets/uploads/vendors/{name}"


def _expired(vendor: dict, today: date) -> bool:
    end = _text(vendor.get("subscription_end")).strip()
    if not end:
        return False
    try:
        end_date = date.fromisoformat(end[:10])
    except ValueError:
        return False
    return end_date < today


def _days_left(vendor: dict, today: date) -> int:
    end = _text(vendor.get("subscription_end")).strip()
    if not end:
        return 0
    try:
        end_date = date.fromisoformat(end[:10])
    except ValueError:
        return 0
    return max(0, (end_date - today).days)


def _gst_details(vendor: dict):
    raw = vendor.get("gst_details")
    if isinstance(raw, str) and raw.strip():
        try:
            raw = json.loads(raw)
        except json.JSONDecodeError:
            raw = None
    if not isinstance(raw, dict):
        return None
    details = {key: _text(raw.get(key)) for key in GST_KEYS}
    if not any(details.values()):
        return None
    return details


def profile_incomplete(vendor: dict) -> bool:
    flag = vendor.get("profile_complete")
    if flag is False:
        return True
    if flag is True:
        return False
    shop = _text(vendor.get("shop_name")).strip()
    mobile = re.sub(r"\D", "", _text(vendor.get("mobile")))
    if len(mobile) >= 12 and mobile.startswith("91"):
        mobile = mobile[-10:]
    return shop == "" or not re.fullmatch(r"[6-9]\d{9}", mobile)


def vendor_payload(vendor: dict) -> dict:
    logo = vendor.get("logo")
    photo = _text(vendor.get("shop_photo")).strip()
    trusted = int(vendor.get("gst_verified") or 0) == 1 and photo != ""
    incomplete = profile_incomplete(vendor)
    return {
        "id": int(vendor.get("id") or 0),
        "username": _text(vendor.get("username")),
        "shop_name": vendor.get("shop_name"),
        "email": _text(vendor.get("email")),
        "mobile": _text(vendor.get("mobile")),
        "location": _text(vendor.get("location")),
        "state": _text(vendor.get("state")),
        "status": int(vendor.get("status") or 0),
        "verified": int(vendor.get("status") or 0) == 1,
        "profile_complete": not incomplete,
        "subscription_start": vendor.get("subscription_start"),
        "subscription_end": vendor.get("subscription_end"),
        "logo": logo,
        "logo_url": _file_url(logo) or DEFAULT_LOGO,
        "gstin": _text(vendor.get("gstin")),
        "gst_verified": int(vendor.get("gst_verified") or 0) == 1,
        "gst_details": _gst_details(vendor),
        "shop_photo_url": _file_url(photo) if photo else None,
        "shop_trusted": trusted,
        "admin_warning": _text(vendor.get("admin_warning")),
    }


def _password_matches(password: str, stored: str) -> bool:
    if not stored:
        return False
    hashed = stored.replace("$2y$", "$2b$", 1).encode()
    try:
        return bcrypt.checkpw(password.encode(), hashed)
    except ValueError:
        return False


def _find_vendor(username: str):
    needle = _text(username).strip()
    if needle == "":
        return None
    client = firestore.client()
    matches = client.collection("vendors").where("username", "==", needle).limit(1).stream()
    for doc in matches:
        data = doc.to_dict() or {}
        data["_ref"] = doc.reference
        return data
    if "@" in needle:
        return _find_vendor_by_email(needle)
    digits = re.sub(r"\D", "", needle)
    if len(digits) >= 12 and digits.startswith("91"):
        digits = digits[-10:]
    if re.fullmatch(r"[6-9]\d{9}", digits):
        for doc in client.collection("vendors").where("mobile", "==", digits).limit(1).stream():
            data = doc.to_dict() or {}
            data["_ref"] = doc.reference
            return data
        for doc in client.collection("vendors").stream():
            data = doc.to_dict() or {}
            mobile = re.sub(r"\D", "", _text(data.get("mobile")))
            if len(mobile) >= 12 and mobile.startswith("91"):
                mobile = mobile[-10:]
            if mobile == digits:
                data["_ref"] = doc.reference
                return data
    return None


def _deactivate(vendor: dict) -> None:
    client = firestore.client()
    vendor_id = int(vendor.get("id") or 0)
    batch = client.batch()
    batch.update(vendor["_ref"], {"status": 0})
    for doc in client.collection("products").where("vendor_id", "==", vendor_id).stream():
        batch.update(doc.reference, {"status": 0})
    batch.commit()
    vendor["status"] = 0


def _finish_login(vendor: dict) -> dict:
    today = date.today()
    if _expired(vendor, today):
        _deactivate(vendor)
        return {
            "success": False,
            "code": "expired",
            "message": "Aapki subscription expire ho gayi hai. Please renew karein.",
            "vendor_id": int(vendor.get("id") or 0),
            "vendor": vendor_payload(vendor),
        }

    if int(vendor.get("status") or 0) != 1:
        return {
            "success": False,
            "code": "pending_verification",
            "message": "Aapka account pending approval hai. Please subscription activate karein.",
            "vendor_id": int(vendor.get("id") or 0),
            "vendor": vendor_payload(vendor),
        }

    return {"success": True, "vendor": vendor_payload(vendor)}


def _find_vendor_by_email(email: str):
    email = _text(email).strip().lower()
    if email == "":
        return None
    client = firestore.client()
    matches = client.collection("vendors").where("email", "==", email).limit(1).stream()
    for doc in matches:
        data = doc.to_dict() or {}
        data["_ref"] = doc.reference
        return data
    for doc in client.collection("vendors").stream():
        data = doc.to_dict() or {}
        if _text(data.get("email")).strip().lower() == email:
            data["_ref"] = doc.reference
            return data
    return None


def _vendor_by_id(vendor_id: int):
    if vendor_id <= 0:
        return None
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}
    data["_ref"] = doc.reference
    return data


def _link_firebase_uid(vendor: dict, uid: str) -> None:
    uid = _text(uid).strip()
    if uid == "" or _text(vendor.get("firebase_uid")) == uid:
        return
    try:
        vendor["_ref"].update({"firebase_uid": uid})
    except Exception:
        return
    vendor["firebase_uid"] = uid


def login_with_firebase(id_token: str, defer_create: bool = False) -> dict:
    id_token = _text(id_token).strip()
    if id_token == "":
        return {
            "success": False,
            "code": "invalid_credentials",
            "message": "Sign in again.",
        }
    try:
        decoded = firebase_auth.verify_id_token(id_token)
    except Exception:
        return {
            "success": False,
            "code": "invalid_credentials",
            "message": "This sign-in could not be verified. Try again.",
        }
    provider = _text(((decoded.get("firebase") or {}).get("sign_in_provider")))
    if provider != "google.com":
        return {
            "success": False,
            "code": "invalid_credentials",
            "message": "Use Google to sign in.",
        }
    email = _text(decoded.get("email")).strip().lower()
    if "@" not in email:
        return {
            "success": False,
            "code": "invalid_credentials",
            "message": "This Google account has no email address.",
        }
    uid = _text(decoded.get("uid"))
    display_name = _text(decoded.get("name"))
    vendor = _find_vendor_by_email(email)
    if vendor is None and defer_create:
        from accounts import hold_google_signup

        token = hold_google_signup(email, uid, display_name)
        return {
            "success": True,
            "code": "needs_profile",
            "redirect": "complete-profile",
            "message": "Finish a few business details to continue.",
            "vendor_id": 0,
            "signup_token": token,
            "vendor": {
                "id": 0,
                "email": email,
                "shop_name": "",
                "username": "",
                "profile_complete": False,
            },
        }
    if vendor is None:
        from accounts import create_google_vendor

        created = create_google_vendor(email, uid, display_name)
        if not created.get("success"):
            return {
                "success": False,
                "code": "register_failed",
                "message": created.get("message") or "Could not create your account with Google.",
            }
        vendor = created.get("vendor_row") or {}
        return {
            "success": True,
            "code": "needs_profile",
            "redirect": "complete-profile",
            "message": "Finish a few business details to continue.",
            "vendor_id": int(vendor.get("id") or 0),
            "vendor": vendor_payload(vendor),
        }
    _link_firebase_uid(vendor, uid)
    if profile_incomplete(vendor):
        return {
            "success": True,
            "code": "needs_profile",
            "redirect": "complete-profile",
            "message": "Finish a few business details to continue.",
            "vendor_id": int(vendor.get("id") or 0),
            "vendor": vendor_payload(vendor),
        }
    return _finish_login(vendor)


def issue_app_handoff(vendor_id: int = 0, signup_token: str = "") -> str:
    code = secrets.token_urlsafe(24)
    payload = {"expires_at": datetime.now(timezone.utc) + timedelta(minutes=2)}
    if int(vendor_id or 0) > 0:
        payload["vendor_id"] = int(vendor_id)
    if signup_token:
        payload["signup_token"] = signup_token
    firestore.client().collection("vendor_auth_codes").document(code).set(payload)
    return code


def consume_app_handoff(code: str) -> dict:
    code = _text(code).strip()
    if code == "" or len(code) > 200:
        return {"success": False, "message": "Sign in with Google again."}
    ref = firestore.client().collection("vendor_auth_codes").document(code)
    snap = ref.get()
    data = snap.to_dict() if snap.exists else None
    if snap.exists:
        ref.delete()
    if not data:
        return {"success": False, "message": "This Google sign-in expired. Try again."}
    expires = data.get("expires_at")
    if hasattr(expires, "timestamp"):
        if getattr(expires, "tzinfo", None) is None:
            expires = expires.replace(tzinfo=timezone.utc)
        if expires <= datetime.now(timezone.utc):
            return {"success": False, "message": "This Google sign-in expired. Try again."}
    signup_token = _text(data.get("signup_token"))
    if signup_token:
        from accounts import load_google_signup

        signup = load_google_signup(signup_token)
        if signup is None:
            return {"success": False, "message": "This Google sign-in expired. Try again."}
        return {
            "success": True,
            "code": "needs_profile",
            "signup_token": signup_token,
            "redirect": "complete-profile",
            "message": "Finish a few business details to continue.",
            "vendor_id": 0,
            "vendor": {
                "id": 0,
                "email": _text(signup.get("email")),
                "shop_name": "",
                "username": "",
                "profile_complete": False,
            },
        }
    vendor = _vendor_by_id(int(data.get("vendor_id") or 0))
    if vendor is None:
        return {"success": False, "message": "Sign in with Google again."}
    if profile_incomplete(vendor):
        return {
            "success": True,
            "code": "needs_profile",
            "redirect": "complete-profile",
            "message": "Finish a few business details to continue.",
            "vendor_id": int(vendor.get("id") or 0),
            "vendor": vendor_payload(vendor),
        }
    return _finish_login(vendor)


def login(username: str, password: str) -> dict:
    username = (username or "").strip()
    password = password or ""
    if username == "" or password == "":
        return {
            "success": False,
            "code": "invalid_credentials",
            "message": "Invalid username or password.",
        }

    vendor = _find_vendor(username)
    if vendor is None or not _password_matches(password, _text(vendor.get("password"))):
        return {
            "success": False,
            "code": "invalid_credentials",
            "message": "Invalid username or password.",
        }

    return _finish_login(vendor)


def acknowledge_warning(vendor_id: int) -> tuple[dict, int]:
    ref = firestore.client().collection("vendors").document(str(vendor_id))
    if not ref.get().exists:
        return {"success": False, "message": "Vendor not found."}, 422
    ref.update({"admin_warning": ""})
    return {"success": True, "message": "Warning acknowledged."}, 200


def vendor_status(vendor_id: str) -> tuple[dict, int]:
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return {"success": False, "message": "Vendor not found"}, 200
    vendor = doc.to_dict() or {}
    today = date.today()
    verified = int(vendor.get("status") or 0) == 1
    if verified:
        message = "Seller verified. Subscription active."
    elif not _text(vendor.get("subscription_start")).strip():
        message = "Pending verification. Activate subscription to go live."
    else:
        message = "Subscription expired. Renew to get verified again."
    return {
        "success": True,
        "vendor": vendor_payload(vendor),
        "verified": verified,
        "days_left": _days_left(vendor, today),
        "message": message,
    }, 200
