from __future__ import annotations

import re
from datetime import datetime, timedelta, timezone

from firebase_admin import auth as firebase_auth
from firebase_admin import firestore

from password_reset import _aware, _fail, _text


def _ref(mobile: str):
    return firestore.client().collection("vendor_mobile_otps").document(mobile)


def _digits(mobile: str) -> str:
    digits = re.sub(r"\D", "", _text(mobile))
    if digits.startswith("91") and len(digits) == 12:
        digits = digits[2:]
    return digits


def _valid_mobile(mobile: str) -> bool:
    return re.fullmatch(r"[6-9][0-9]{9}", mobile) is not None


def _row_verified(mobile: str) -> dict:
    if not _valid_mobile(mobile):
        return _fail("Enter a valid 10-digit Indian mobile number.")
    row = _ref(mobile).get()
    if not row.exists:
        return _fail("Verify this mobile number first.")
    data = row.to_dict() or {}
    expires_at = _aware(data.get("expires_at"))
    if not data.get("verified") or expires_at is None or expires_at <= datetime.now(timezone.utc):
        _ref(mobile).delete()
        return _fail("Verify this mobile number first.")
    return {"ok": True, "message": "Mobile number verified."}


def mark_verified(mobile: str, id_token: str) -> dict:
    from accounts import _taken

    mobile = _digits(mobile)
    id_token = _text(id_token)
    if not _valid_mobile(mobile):
        return _fail("Enter a valid 10-digit Indian mobile number.")
    if id_token == "":
        return _fail("Verify this mobile number first.")
    if _taken("mobile", mobile):
        return _fail("The mobile has already been taken.")
    try:
        decoded = firebase_auth.verify_id_token(id_token)
    except Exception:
        return _fail("Could not verify this mobile number. Request a new OTP.")
    phone = _text(decoded.get("phone_number"))
    provider = _text(((decoded.get("firebase") or {}).get("sign_in_provider")))
    if provider != "phone" or phone != "+91" + mobile:
        return _fail("This code was sent to a different mobile number.")

    now = datetime.now(timezone.utc)
    _ref(mobile).set(
        {
            "mobile": mobile,
            "verified": True,
            "verified_at": now,
            "expires_at": now + timedelta(minutes=10),
            "uid": _text(decoded.get("uid")),
        }
    )
    return {"ok": True, "message": "Mobile number verified."}


def ready(mobile: str, otp: str = "") -> dict:
    return _row_verified(_digits(mobile))


def confirm(mobile: str, id_token: str) -> dict:
    return mark_verified(mobile, id_token)


def consume(mobile: str, otp: str = "") -> dict:
    mobile = _digits(mobile)
    checked = _row_verified(mobile)
    if not checked.get("ok"):
        return checked
    _ref(mobile).delete()
    return {"ok": True, "message": "Mobile number verified."}


def _public(result: dict) -> tuple[dict, int]:
    status = 200 if result.get("ok") else 422
    return {
        "success": bool(result.get("ok")),
        "message": result.get("message") or "Could not verify this mobile number.",
    }, status


def confirm_mobile_otp(body: dict) -> tuple[dict, int]:
    token = body.get("firebase_token")
    if _text(token) == "":
        token = body.get("id_token")
    return _public(mark_verified(_text(body.get("mobile")), _text(token)))
