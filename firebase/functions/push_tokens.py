import hashlib

from firebase_admin import firestore


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _body(req) -> dict:
    body = req.get_json(silent=True)
    if isinstance(body, dict):
        return body
    if req.form:
        return req.form.to_dict()
    return {}


def _environment(value) -> str:
    environment = _text(value).lower() or "sandbox"
    if environment not in {"sandbox", "production"}:
        return "sandbox"
    return environment


def _doc_id(token: str) -> str:
    return hashlib.sha256(token.encode()).hexdigest()


def _save(collection: str, token: str, owner_field: str, owner_id: int, platform: str, environment: str) -> None:
    firestore.client().collection(collection).document(_doc_id(token)).set(
        {
            "token": token,
            owner_field: owner_id,
            "platform": platform or "ios",
            "environment": environment,
        }
    )


def _delete(collection: str, token: str) -> None:
    if token == "":
        return
    firestore.client().collection(collection).document(_doc_id(token)).delete()


def save_vendor(req) -> tuple[dict, int]:
    body = _body(req)
    raw_vendor = body.get("vendor_id")
    token = _text(body.get("token"))
    platform = _text(body.get("platform")) or "ios"
    if raw_vendor is None or _text(raw_vendor) == "":
        return {"success": False, "message": "The vendor id field is required."}, 422
    try:
        vendor_id = int(raw_vendor)
    except (TypeError, ValueError):
        return {"success": False, "message": "The vendor id must be an integer."}, 422
    if token == "" or len(token) > 255 or len(platform) > 20:
        return {"success": False, "message": "The token field is required."}, 422
    if not firestore.client().collection("vendors").document(str(vendor_id)).get().exists:
        return {"success": False, "message": "Vendor not found."}, 422
    _save("vendor_push_tokens", token, "vendor_id", vendor_id, platform, _environment(body.get("environment")))
    return {"success": True, "message": "Push token saved."}, 200


def delete_vendor(req) -> tuple[dict, int]:
    _delete("vendor_push_tokens", _text(_body(req).get("token")))
    return {"success": True, "message": "Push token removed."}, 200


def save_admin(req, admin: dict) -> tuple[dict, int]:
    body = _body(req)
    token = _text(body.get("token"))
    platform = _text(body.get("platform")) or "ios"
    if token == "" or len(token) > 255 or len(platform) > 20:
        return {"success": False, "message": "The token field is required."}, 422
    _save(
        "admin_push_tokens",
        token,
        "admin_id",
        int(admin.get("admin_id") or 0),
        platform,
        _environment(body.get("environment")),
    )
    return {"success": True, "message": "Push token saved."}, 200


def delete_admin(req) -> tuple[dict, int]:
    _delete("admin_push_tokens", _text(_body(req).get("token")))
    return {"success": True, "message": "Push token removed."}, 200
