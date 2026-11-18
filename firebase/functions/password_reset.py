from __future__ import annotations

import os
import re
import smtplib
from datetime import datetime, timedelta, timezone
from email.message import EmailMessage

import bcrypt
from firebase_admin import firestore

from auth import _password_matches


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _fail(message: str) -> dict:
    return {"ok": False, "message": message}


def _valid_email(email: str) -> bool:
    return re.fullmatch(r"[^@\s]+@[^@\s]+\.[^@\s]+", email) is not None


def _vendor_by_email(email: str):
    target = email.lower()
    for doc in firestore.client().collection("vendors").stream():
        data = doc.to_dict() or {}
        if _text(data.get("email")).lower() == target:
            data["_ref"] = doc.reference
            return data
    return None


def _otp_ref(vendor_id: int):
    return firestore.client().collection("vendor_password_otps").document(str(vendor_id))


def _aware(value):
    if isinstance(value, datetime):
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)
        return value
    return None


def _smtp():
    host = _text(os.environ.get("MAIL_HOST"))
    user = _text(os.environ.get("MAIL_USERNAME"))
    password = os.environ.get("MAIL_PASSWORD") or ""
    if host == "" or user == "" or password.strip() == "":
        return None
    try:
        port = int(os.environ.get("MAIL_PORT") or "587")
    except ValueError:
        port = 587
    sender = _text(os.environ.get("MAIL_FROM_ADDRESS")) or user
    return host, port, user, password, sender


def _safe_mail_error(exc: Exception) -> str:
    text = re.sub(r"//[^/\s:]+:[^@\s]+@", "//***@", str(exc))
    text = re.sub(r"\s+", " ", text).strip()
    if text == "":
        return "Check the server mail settings."
    if len(text) > 180:
        return text[:180] + "..."
    return text


def _send_mail(vendor_email: str, otp: str) -> str | None:
    settings = _smtp()
    if settings is None:
        return "Email is not set up on the server. Add SMTP mail settings, then try again."
    host, port, user, password, sender = settings
    message = EmailMessage()
    message["Subject"] = "My Laptop Market password reset code"
    message["From"] = sender
    message["To"] = vendor_email
    message.set_content(
        f"Your My Laptop Market password reset code is {otp}.\n\n"
        "It expires in 10 minutes. If you did not ask for this, ignore this email."
    )
    try:
        if port == 465:
            with smtplib.SMTP_SSL(host, port, timeout=20) as smtp:
                smtp.login(user, password)
                smtp.send_message(message)
        else:
            with smtplib.SMTP(host, port, timeout=20) as smtp:
                smtp.ehlo()
                smtp.starttls()
                smtp.ehlo()
                smtp.login(user, password)
                smtp.send_message(message)
    except Exception as exc:
        return "Could not send the OTP email. " + _safe_mail_error(exc)
    return None


def clear(vendor_id: int) -> None:
    _otp_ref(vendor_id).delete()


def send(email: str) -> dict:
    email = _text(email).lower()
    vendor = _vendor_by_email(email)
    if vendor is None:
        return _fail("No account is registered with this email.")

    vendor_id = int(vendor.get("id") or 0)
    existing = _otp_ref(vendor_id).get()
    if existing.exists:
        sent_at = _aware((existing.to_dict() or {}).get("sent_at"))
        if sent_at and sent_at > datetime.now(timezone.utc) - timedelta(seconds=60):
            return _fail("OTP already sent. Wait a minute before requesting another.")

    if _smtp() is None:
        return _fail("Email is not set up on the server. Add SMTP mail settings, then try again.")

    otp = f"{int.from_bytes(os.urandom(3), 'big') % 1000000:06d}"
    mail_error = _send_mail(_text(vendor.get("email")) or email, otp)
    if mail_error:
        return _fail(mail_error)

    now = datetime.now(timezone.utc)
    hashed = bcrypt.hashpw(otp.encode(), bcrypt.gensalt()).decode()
    _otp_ref(vendor_id).set(
        {
            "vendor_id": vendor_id,
            "otp_hash": hashed,
            "expires_at": now + timedelta(minutes=10),
            "sent_at": now,
            "attempts": 0,
        }
    )
    return {"ok": True, "message": "OTP sent to " + email}


def reset(email: str, otp: str, password: str) -> dict:
    email = _text(email).lower()
    otp = re.sub(r"\D", "", otp or "")
    password = password or ""
    if len(otp) != 6:
        return _fail("Enter the 6-digit OTP from your email.")
    if len(password) < 6:
        return _fail("Password must be at least 6 characters.")

    vendor = _vendor_by_email(email)
    row_ref = _otp_ref(int(vendor.get("id") or 0)) if vendor else None
    row = row_ref.get() if row_ref is not None else None
    if vendor is None or row is None or not row.exists:
        return _fail("Request a new OTP first.")

    data = row.to_dict() or {}
    expires_at = _aware(data.get("expires_at"))
    if expires_at is None or expires_at <= datetime.now(timezone.utc):
        row_ref.delete()
        return _fail("This OTP has expired. Request a new one.")
    if int(data.get("attempts") or 0) >= 5:
        row_ref.delete()
        return _fail("Too many wrong attempts. Request a new OTP.")
    if not _password_matches(otp, _text(data.get("otp_hash"))):
        row_ref.update({"attempts": int(data.get("attempts") or 0) + 1})
        return _fail("Incorrect OTP.")

    hashed = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
    vendor["_ref"].update({"password": hashed, "password_plain": password})
    row_ref.delete()
    return {"ok": True, "message": "Password updated. You can log in now."}


def forgot_password(body: dict) -> tuple[dict, int]:
    email = _text(body.get("email"))
    if email == "":
        return {"success": False, "message": "The email field is required."}, 200
    if not _valid_email(email):
        return {"success": False, "message": "The email field must be a valid email address."}, 200
    result = send(email)
    status = 200 if result["ok"] else 422
    return {"success": result["ok"], "message": result["message"]}, status


def reset_password(body: dict) -> tuple[dict, int]:
    email = _text(body.get("email"))
    otp = body.get("otp")
    password = body.get("password") or ""
    if email == "":
        return {"success": False, "message": "The email field is required."}, 200
    if not _valid_email(email):
        return {"success": False, "message": "The email field must be a valid email address."}, 200
    if _text(otp) == "":
        return {"success": False, "message": "The otp field is required."}, 200
    if len(password) < 6:
        return {"success": False, "message": "The password field must be at least 6 characters."}, 200
    result = reset(email, _text(otp), password)
    status = 200 if result["ok"] else 422
    return {"success": result["ok"], "message": result["message"]}, status
