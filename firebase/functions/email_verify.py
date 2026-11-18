from __future__ import annotations

import os
import re
from datetime import datetime, timedelta, timezone
from email.message import EmailMessage

import bcrypt
from firebase_admin import firestore

from password_reset import _aware, _fail, _safe_mail_error, _smtp, _text, _valid_email


def _ref(email: str):
    return firestore.client().collection("vendor_email_otps").document(email.lower())


def _send(address: str, otp: str) -> str | None:
    settings = _smtp()
    if settings is None:
        return "Email is not set up on the server. Add SMTP mail settings, then try again."
    host, port, user, password, sender = settings
    message = EmailMessage()
    message["Subject"] = "My Laptop Market email verification code"
    message["From"] = sender
    message["To"] = address
    message.set_content(
        f"Your My Laptop Market email verification code is {otp}.\n\n"
        "Enter this code in the app to finish creating your vendor account. "
        "It expires in 10 minutes. If you did not ask for this, ignore this email."
    )
    try:
        import smtplib

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


def send(email: str) -> dict:
    from accounts import _taken

    email = _text(email).lower()
    if email == "":
        return _fail("Enter your email address.")
    if not _valid_email(email):
        return _fail("Enter a valid email address.")
    if _taken("email", email):
        return _fail("The email has already been taken.")

    row_ref = _ref(email)
    existing = row_ref.get()
    if existing.exists:
        sent_at = _aware((existing.to_dict() or {}).get("sent_at"))
        if sent_at and sent_at > datetime.now(timezone.utc) - timedelta(seconds=60):
            return _fail("OTP already sent. Wait a minute before requesting another.")

    if _smtp() is None:
        return _fail("Email is not set up on the server. Add SMTP mail settings, then try again.")

    otp = f"{int.from_bytes(os.urandom(3), 'big') % 1000000:06d}"
    mail_error = _send(email, otp)
    if mail_error:
        return _fail(mail_error)

    now = datetime.now(timezone.utc)
    hashed = bcrypt.hashpw(otp.encode(), bcrypt.gensalt()).decode()
    row_ref.set(
        {
            "email": email,
            "otp_hash": hashed,
            "expires_at": now + timedelta(minutes=10),
            "sent_at": now,
            "attempts": 0,
        }
    )
    return {"ok": True, "message": "OTP sent to " + email}


def _check(email: str, otp: str) -> dict:
    from auth import _password_matches

    email = _text(email).lower()
    otp = re.sub(r"\D", "", otp or "")
    if len(otp) != 6:
        return _fail("Enter the 6-digit OTP sent to your email.")

    row_ref = _ref(email)
    row = row_ref.get()
    if not row.exists:
        return _fail("Send an OTP to this email first.")

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
    return {"ok": True, "ref": row_ref, "email": email}


def ready(email: str, otp: str) -> dict:
    checked = _check(email, otp)
    if not checked.get("ok"):
        return {"ok": False, "message": checked.get("message") or "Enter the OTP sent to your email."}
    return {"ok": True, "message": "Email verified."}


def confirm(email: str, otp: str) -> dict:
    checked = _check(email, otp)
    if not checked.get("ok"):
        return checked
    checked["ref"].update({"verified": True, "verified_at": datetime.now(timezone.utc)})
    return {"ok": True, "message": "Email verified."}


def consume(email: str, otp: str) -> dict:
    checked = _check(email, otp)
    if not checked.get("ok"):
        return checked
    checked["ref"].delete()
    return {"ok": True, "message": "Email verified."}


def _public(result: dict) -> tuple[dict, int]:
    status = 200 if result.get("ok") else 422
    return {"success": bool(result.get("ok")), "message": result.get("message") or "Could not verify this email."}, status


def send_email_otp(body: dict) -> tuple[dict, int]:
    return _public(send(_text(body.get("email"))))


def confirm_email_otp(body: dict) -> tuple[dict, int]:
    code = body.get("email_otp")
    if _text(code) == "":
        code = body.get("otp")
    return _public(confirm(_text(body.get("email")), _text(code)))
