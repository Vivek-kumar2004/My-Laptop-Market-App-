import re

from firebase_admin import firestore


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _shop(vendor_id: int) -> str:
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return "A vendor"
    row = doc.to_dict() or {}
    name = _text(row.get("shop_name")) or _text(row.get("username"))
    return name or "A vendor"


def _others(vendor_id: int) -> list[int]:
    ids = []
    for doc in firestore.client().collection("vendors").stream():
        row = doc.to_dict() or {}
        other = int(row.get("id") or 0)
        if other != vendor_id and int(row.get("status") or 0) == 1:
            ids.append(other)
    return ids


def product_posted(vendor_id: int, product_name: str, product_id: int, status: int) -> None:
    try:
        if int(status) != 1:
            return
        from push_apns import notify

        name = _text(product_name) or "New product"
        notify(
            _others(vendor_id),
            "New product",
            f"{_shop(vendor_id)} listed {name}",
            "product",
            {"target_id": str(product_id), "target_name": name},
        )
    except Exception:
        return


def message_sent(sender_id: int, receiver_id: int, text: str) -> None:
    try:
        from push_apns import notify

        sender = _shop(sender_id)
        preview = re.sub(r"\s+", " ", _text(text))
        if len(preview) > 80:
            preview = preview[:77] + "..."
        body = f"{sender}: {preview}" if preview else f"{sender} sent you a message"
        notify(
            [receiver_id],
            "New message",
            body,
            "message",
            {"target_id": str(sender_id), "target_name": sender},
        )
    except Exception:
        return
