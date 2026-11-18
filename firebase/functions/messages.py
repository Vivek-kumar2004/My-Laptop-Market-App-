from datetime import datetime, timezone

from firebase_admin import firestore


def _text(value) -> str:
    if value is None:
        return ""
    return str(value)


def _stamp(value) -> str:
    text = _text(value).strip().replace("Z", "").replace("T", " ")
    return text[:19]


def _iso_z(value) -> str:
    stamp = _stamp(value)
    if not stamp:
        return ""
    return stamp.replace(" ", "T") + ".000000Z"


def _iso_offset(value) -> str:
    stamp = _stamp(value)
    if not stamp:
        return ""
    return stamp.replace(" ", "T") + "+00:00"


def _now_stamp() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")


def _message_row(data: dict) -> dict:
    return {
        "id": int(data.get("id") or 0),
        "sender_id": int(data.get("sender_id") or 0),
        "receiver_id": int(data.get("receiver_id") or 0),
        "message": _text(data.get("message")),
        "is_read": int(data.get("is_read") or 0),
        "created_at": _iso_z(data.get("created_at")),
        "updated_at": _iso_z(data.get("updated_at") or data.get("created_at")),
    }


def _vendor(client, vendor_id: int):
    doc = client.collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    return doc.to_dict() or {}


def conversations(vendor_id: int) -> dict:
    client = firestore.client()
    rows = []
    for doc in client.collection("messages").where("sender_id", "==", vendor_id).stream():
        rows.append(doc.to_dict() or {})
    for doc in client.collection("messages").where("receiver_id", "==", vendor_id).stream():
        rows.append(doc.to_dict() or {})

    latest = {}
    unread = {}
    for row in rows:
        sender = int(row.get("sender_id") or 0)
        receiver = int(row.get("receiver_id") or 0)
        contact_id = receiver if sender == vendor_id else sender
        stamp = _stamp(row.get("created_at"))
        current = latest.get(contact_id)
        if current is None or stamp > _stamp(current.get("created_at")):
            latest[contact_id] = row
        if sender == contact_id and receiver == vendor_id and int(row.get("is_read") or 0) == 0:
            unread[contact_id] = unread.get(contact_id, 0) + 1

    items = []
    for contact_id, last in latest.items():
        vendor = _vendor(client, contact_id)
        if not vendor:
            continue
        items.append(
            {
                "contact_id": int(vendor.get("id") or contact_id),
                "contact_name": vendor.get("shop_name") or vendor.get("username") or "",
                "last_message": _text(last.get("message")),
                "last_message_time": _iso_offset(last.get("created_at")),
                "unread_count": unread.get(contact_id, 0),
            }
        )
    items.sort(key=lambda row: row["last_message_time"], reverse=True)
    return {"success": True, "conversations": items}


def _shop(client, vendor_id: int, cache: dict) -> str:
    if vendor_id in cache:
        return cache[vendor_id]
    vendor = _vendor(client, vendor_id)
    name = ""
    if vendor:
        name = _text(vendor.get("shop_name") or vendor.get("username")).strip()
    cache[vendor_id] = name or f"Vendor {vendor_id}"
    return cache[vendor_id]


def admin_threads() -> list[dict]:
    """Read-only list of vendor-to-vendor chats. Does not mark messages read."""
    client = firestore.client()
    threads: dict[tuple[int, int], dict] = {}
    for doc in client.collection("messages").stream():
        row = doc.to_dict() or {}
        sender = int(row.get("sender_id") or 0)
        receiver = int(row.get("receiver_id") or 0)
        if sender <= 0 or receiver <= 0 or sender == receiver:
            continue
        key = (min(sender, receiver), max(sender, receiver))
        slot = threads.setdefault(
            key,
            {"left_id": key[0], "right_id": key[1], "stamp": "", "preview": "", "count": 0},
        )
        slot["count"] += 1
        stamp = _stamp(row.get("created_at"))
        if stamp >= slot["stamp"]:
            slot["stamp"] = stamp
            slot["preview"] = _text(row.get("message")).strip()
    names: dict[int, str] = {}
    items = []
    for slot in threads.values():
        slot["left_name"] = _shop(client, slot["left_id"], names)
        slot["right_name"] = _shop(client, slot["right_id"], names)
        items.append(slot)
    items.sort(key=lambda row: row["stamp"], reverse=True)
    return items


def admin_thread(left_id: int, right_id: int) -> list[dict]:
    """Read-only transcript. Does not mark messages read."""
    client = firestore.client()
    low, high = (left_id, right_id) if left_id <= right_id else (right_id, left_id)
    rows = []
    for sender, receiver in ((low, high), (high, low)):
        for doc in client.collection("messages").where("sender_id", "==", sender).stream():
            data = doc.to_dict() or {}
            if int(data.get("receiver_id") or 0) != receiver:
                continue
            rows.append(data)
    rows.sort(key=lambda row: (_stamp(row.get("created_at")), int(row.get("id") or 0)))
    names: dict[int, str] = {}
    transcript = []
    for row in rows:
        sender = int(row.get("sender_id") or 0)
        transcript.append(
            {
                "sender_id": sender,
                "sender_name": _shop(client, sender, names),
                "message": _text(row.get("message")).strip(),
                "created_at": _stamp(row.get("created_at")),
            }
        )
    return transcript


def chat(vendor_id: int, contact_id: int) -> dict:
    client = firestore.client()
    batch = client.batch()
    pending = 0
    rows = []
    query = client.collection("messages").where("receiver_id", "==", vendor_id).stream()
    for doc in query:
        data = doc.to_dict() or {}
        if int(data.get("sender_id") or 0) != contact_id:
            continue
        if int(data.get("is_read") or 0) == 0:
            batch.update(doc.reference, {"is_read": 1, "updated_at": _now_stamp()})
            pending += 1
            data["is_read"] = 1
        rows.append(data)
    if pending:
        batch.commit()

    for doc in client.collection("messages").where("sender_id", "==", vendor_id).stream():
        data = doc.to_dict() or {}
        if int(data.get("receiver_id") or 0) == contact_id:
            rows.append(data)

    rows.sort(key=lambda row: (_stamp(row.get("created_at")), int(row.get("id") or 0)))
    return {"success": True, "messages": [_message_row(row) for row in rows]}


def _next_id() -> int:
    client = firestore.client()
    ref = client.collection("settings").document("counters")
    transaction = client.transaction()

    @firestore.transactional
    def allocate(transaction):
        snap = ref.get(transaction=transaction)
        current = int((snap.to_dict() or {}).get("messages") or 0)
        nxt = current + 1
        transaction.set(ref, {"messages": nxt}, merge=True)
        return nxt

    return allocate(transaction)


def send_message(sender_id, receiver_id, text) -> tuple[dict, int]:
    try:
        sender_id = int(sender_id)
        receiver_id = int(receiver_id)
    except (TypeError, ValueError):
        return {"success": False, "message": "Cannot message yourself."}, 422
    text = _text(text).strip()
    if sender_id == receiver_id:
        return {"success": False, "message": "Cannot message yourself."}, 422
    if text == "":
        return {"success": False, "message": "Message is required."}, 422

    client = firestore.client()
    if _vendor(client, sender_id) is None or _vendor(client, receiver_id) is None:
        return {"success": False, "message": "Vendor not found."}, 422

    now = _now_stamp()
    message_id = _next_id()
    row = {
        "id": message_id,
        "sender_id": sender_id,
        "receiver_id": receiver_id,
        "message": text,
        "is_read": 0,
        "created_at": now,
        "updated_at": now,
    }
    client.collection("messages").document(str(message_id)).set(row)
    from alerts import message_sent

    message_sent(sender_id, receiver_id, text)
    return {"success": True, "message": _message_row(row)}, 200


def contacts(vendor_id: int) -> dict:
    client = firestore.client()
    items = []
    for doc in client.collection("vendors").stream():
        vendor = doc.to_dict() or {}
        if int(vendor.get("id") or 0) == vendor_id:
            continue
        if int(vendor.get("status") or 0) != 1:
            continue
        items.append(
            {
                "id": int(vendor.get("id") or 0),
                "username": _text(vendor.get("username")),
                "shop_name": vendor.get("shop_name"),
            }
        )
    items.sort(key=lambda row: _text(row.get("shop_name")).lower())
    return {"success": True, "contacts": items}
