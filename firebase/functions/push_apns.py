from __future__ import annotations

import base64
import json
import os
import time

import httpx
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.asymmetric.utils import decode_dss_signature
from firebase_admin import firestore

VENDOR_BUNDLE = "com.mylaptopmarket.app"
GST_ALERT = "Plz update your GSTIN to become a verified seller"
_jwt_cache = {"token": "", "exp": 0}


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _b64url(raw: bytes) -> str:
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode()


def _pem() -> str:
    encoded = _text(os.environ.get("APNS_KEY_B64"))
    if encoded:
        try:
            return base64.b64decode(encoded).decode()
        except Exception:
            return ""
    return os.environ.get("APNS_KEY") or ""


def _ready() -> str:
    if _text(os.environ.get("APNS_KEY_ID")) == "" or _text(os.environ.get("APNS_TEAM_ID")) == "":
        return "Apple key missing."
    if _pem() == "":
        return "Apple key missing."
    return ""


def _jwt() -> str:
    now = int(time.time())
    if _jwt_cache["token"] and _jwt_cache["exp"] > now + 60:
        return _jwt_cache["token"]
    key = serialization.load_pem_private_key(_pem().encode(), password=None)
    header = _b64url(json.dumps({"alg": "ES256", "kid": _text(os.environ.get("APNS_KEY_ID"))}, separators=(",", ":")).encode())
    claims = _b64url(json.dumps({"iss": _text(os.environ.get("APNS_TEAM_ID")), "iat": now}, separators=(",", ":")).encode())
    signing = f"{header}.{claims}".encode()
    der = key.sign(signing, ec.ECDSA(hashes.SHA256()))
    r_int, s_int = decode_dss_signature(der)
    token = f"{header}.{claims}.{_b64url(r_int.to_bytes(32, 'big') + s_int.to_bytes(32, 'big'))}"
    _jwt_cache["token"] = token
    _jwt_cache["exp"] = now + 2400
    return token


def _post(client: httpx.Client, url: str, bundle: str, payload: str) -> tuple[int, str]:
    response = client.post(
        url,
        headers={
            "authorization": "bearer " + _jwt(),
            "apns-topic": bundle,
            "apns-push-type": "alert",
            "apns-priority": "10",
            "content-type": "application/json",
        },
        content=payload,
    )
    return response.status_code, response.text


def _payload(title: str, body: str, push_type: str, extra: dict | None = None) -> str:
    return json.dumps(
        {
            "aps": {"alert": {"title": title, "body": body}, "sound": "default", "badge": 1},
            "type": push_type,
            **(extra or {}),
        }
    )


def _dispatch(collection: str, owner_field: str, wanted: set | None, bundle: str, payload: str) -> dict:
    sent = failed = removed = 0
    client = firestore.client()
    with httpx.Client(http2=True, timeout=15) as http:
        for doc in client.collection(collection).stream():
            row = doc.to_dict() or {}
            try:
                owner_id = int(row.get(owner_field) or 0)
            except (TypeError, ValueError):
                continue
            token = _text(row.get("token"))
            if token == "" or (wanted is not None and owner_id not in wanted):
                continue
            preferred = "production" if _text(row.get("environment")) == "production" else "sandbox"
            hosts = (
                [("production", "https://api.push.apple.com"), ("sandbox", "https://api.sandbox.push.apple.com")]
                if preferred == "production"
                else [("sandbox", "https://api.sandbox.push.apple.com"), ("production", "https://api.push.apple.com")]
            )
            last_status = 0
            last_body = ""
            delivered = False
            for env, base in hosts:
                try:
                    last_status, last_body = _post(http, f"{base}/3/device/{token}", bundle, payload)
                except Exception:
                    last_status = 0
                    last_body = ""
                    continue
                if 200 <= last_status < 300:
                    if _text(row.get("environment")) != env:
                        doc.reference.update({"environment": env})
                    sent += 1
                    delivered = True
                    break
            if delivered:
                continue
            reason = ""
            try:
                reason = json.loads(last_body or "{}").get("reason") or ""
            except json.JSONDecodeError:
                reason = ""
            if last_status == 410 or reason in {"Unregistered", "ExpiredProviderToken", "BadDeviceToken"}:
                doc.reference.delete()
                removed += 1
            else:
                failed += 1
    return {"sent": sent, "failed": failed, "removed": removed, "error": ""}


def notify(vendor_ids, title: str, body: str, push_type: str, extra: dict | None = None) -> dict:
    missing = _ready()
    if missing:
        return {"sent": 0, "failed": 0, "removed": 0, "error": missing}
    bundle = _text(os.environ.get("APNS_VENDOR_BUNDLE_ID")) or VENDOR_BUNDLE
    return _dispatch(
        "vendor_push_tokens",
        "vendor_id",
        {int(item) for item in vendor_ids},
        bundle,
        _payload(title, body, push_type, extra),
    )


def notify_admins(title: str, body: str, push_type: str) -> None:
    if _ready():
        return
    bundle = _text(os.environ.get("APNS_BUNDLE_ID")) or "com.mylaptopmarket.admin"
    try:
        _dispatch("admin_push_tokens", "admin_id", None, bundle, _payload(title, body, push_type))
    except Exception:
        return


def notify_active(title: str, body: str, push_type: str) -> None:
    if body == "" or _ready():
        return
    ids = []
    for doc in firestore.client().collection("vendors").stream():
        row = doc.to_dict() or {}
        if int(row.get("status") or 0) == 1:
            ids.append(int(row.get("id") or 0))
    try:
        notify(ids, title, body, push_type)
    except Exception:
        return


def push_test() -> tuple[dict, int]:
    missing = _ready()
    if missing:
        return {"success": False, "message": missing}, 500
    bundle = _text(os.environ.get("APNS_BUNDLE_ID")) or "com.mylaptopmarket.admin"
    try:
        result = _dispatch(
            "admin_push_tokens",
            "admin_id",
            None,
            bundle,
            _payload("MLM Admin test", "Push notifications are working on this iPhone.", "inquiry"),
        )
    except Exception:
        return {"success": False, "message": "Could not send the test."}, 500
    sent = int(result["sent"])
    failed = int(result["failed"])
    removed = int(result["removed"])
    message = "Test notification sent." if sent or failed or removed else "No admin phones are registered yet."
    return {"success": True, "message": message, "sent": sent, "failed": failed, "removed": removed}, 200


def gst_alert() -> tuple[dict, int]:
    ids = []
    for doc in firestore.client().collection("vendors").stream():
        row = doc.to_dict() or {}
        gstin = _text(row.get("gstin"))
        if int(row.get("gst_verified") or 0) == 1 and gstin != "":
            continue
        ids.append(int(row.get("id") or 0))
    try:
        result = notify(ids, "My Laptop Market", GST_ALERT, "announcement")
    except Exception:
        return {"success": False, "message": "Could not send the alert."}, 500
    if result.get("error"):
        return {"success": False, "message": result["error"]}, 500
    sent = int(result["sent"])
    message = f"Alert sent to {sent} phones." if sent else "No vendor phones are registered yet."
    return {
        "success": True,
        "message": message,
        "sent": sent,
        "failed": int(result["failed"]),
        "removed": int(result["removed"]),
    }, 200
