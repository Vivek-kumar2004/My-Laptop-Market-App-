import json
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


def vendor_payload(vendor: dict) -> dict:
    logo = vendor.get("logo")
    photo = _text(vendor.get("shop_photo")).strip()
    trusted = int(vendor.get("gst_verified") or 0) == 1 and photo != ""
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
    client = firestore.client()
    matches = client.collection("vendors").where("username", "==", username).limit(1).stream()
    for doc in matches:
        data = doc.to_dict() or {}
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


def login_with_firebase(id_token: str) -> dict:
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
    vendor = _find_vendor_by_email(email)
    if vendor is None:
        return {
            "success": False,
            "code": "no_vendor",
            "message": "No vendor account uses this email. Register first, then sign in with the same email.",
        }
    _link_firebase_uid(vendor, _text(decoded.get("uid")))
    return _finish_login(vendor)


def issue_app_handoff(vendor_id: int) -> str:
    code = secrets.token_urlsafe(24)
    firestore.client().collection("vendor_auth_codes").document(code).set(
        {
            "vendor_id": int(vendor_id),
            "expires_at": datetime.now(timezone.utc) + timedelta(minutes=2),
        }
    )
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
    vendor = _vendor_by_id(int(data.get("vendor_id") or 0))
    if vendor is None:
        return {"success": False, "message": "Sign in with Google again."}
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
