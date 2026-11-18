from __future__ import annotations

from datetime import datetime, timezone

from firebase_admin import firestore

from auth import _file_url
from cluster import group_key, make_title_key, resolve
from products_write import CATEGORIES


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _field(req, name: str) -> str:
    if req.form and name in req.form:
        return _text(req.form.get(name))
    body = req.get_json(silent=True) or {}
    if name in body and body.get(name) is not None:
        return _text(body.get(name))
    return _text(req.args.get(name))


def _vendor_id(req) -> int:
    raw = _field(req, "vendor_id")
    return int(raw) if raw.isdigit() else 0


def _vendor(vendor_id: int):
    if vendor_id <= 0:
        return None
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    return doc.to_dict() or {}


def _iso(value) -> str:
    text = _text(value).replace("T", " ")[:19]
    if text == "":
        return ""
    return text.replace(" ", "T") + "+00:00"


def _now() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")


def title_key(title: str) -> str:
    return make_title_key(title)


def _rows():
    return [doc.to_dict() or {} for doc in firestore.client().collection("requirements").stream()]


def feed(vendor_id: int) -> dict:
    groups = {}
    mine = {}
    for row in _rows():
        status = _text(row.get("status")) or "active"
        key, display = group_key(row)
        if status == "active" and int(row.get("vendor_id") or 0) == vendor_id and vendor_id:
            mine[key] = int(row.get("id") or 0)
            if _text(row.get("title_key")):
                mine[_text(row.get("title_key"))] = int(row.get("id") or 0)
        if status != "active":
            continue
        vendor = _vendor(int(row.get("vendor_id") or 0)) or {}
        title = display
        group = groups.setdefault(
            key,
            {
                "title_key": key,
                "cluster_key": key,
                "title": title,
                "canonical_title": title,
                "category": row.get("category"),
                "quantity": 0,
                "description": row.get("description"),
                "vendors": [],
                "vendor_ids": set(),
                "count": 0,
                "latest_at": "",
                "my_requirement_id": None,
            },
        )
        owner = int(row.get("vendor_id") or 0)
        if owner not in group["vendor_ids"]:
            group["vendor_ids"].add(owner)
            group["vendors"].append(
                {
                    "id": owner,
                    "shop_name": vendor.get("shop_name") or vendor.get("username") or f"Vendor #{owner}",
                    "logo_url": vendor.get("logo_url") or _file_url(vendor.get("logo")),
                    "requirement_id": int(row.get("id") or 0),
                    "title": _text(row.get("title")),
                    "canonical_title": title,
                    "quantity": int(row["quantity"]) if row.get("quantity") not in (None, "") else None,
                    "description": row.get("description"),
                    "updated_at": _iso(row.get("updated_at")),
                }
            )
            group["count"] += 1
        if row.get("quantity") not in (None, ""):
            group["quantity"] += int(row["quantity"])
        stamp = _iso(row.get("updated_at"))
        if stamp >= group["latest_at"]:
            group["latest_at"] = stamp
            group["title"] = title
            group["canonical_title"] = title
            group["category"] = row.get("category")
    data = []
    for key, group in groups.items():
        group.pop("vendor_ids", None)
        group["my_requirement_id"] = mine.get(key)
        data.append(group)
    data.sort(key=lambda row: row.get("latest_at") or "", reverse=True)
    return {"status": True, "message": "Requirements fetched successfully", "data": data}


def mine(vendor_id: int) -> tuple[dict, int]:
    if _vendor(vendor_id) is None:
        return {"status": False, "message": "Valid vendor_id is required", "data": []}, 422
    rows = [row for row in _rows() if int(row.get("vendor_id") or 0) == vendor_id]
    rows.sort(key=lambda row: _iso(row.get("updated_at")), reverse=True)
    return {
        "status": True,
        "message": "Your requirements fetched successfully",
        "data": [
            {
                "id": int(row.get("id") or 0),
                "title": row.get("title"),
                "category": row.get("category"),
                "quantity": int(row["quantity"]) if row.get("quantity") not in (None, "") else None,
                "description": row.get("description"),
                "status": _text(row.get("status")) or "active",
                "updated_at": _iso(row.get("updated_at")),
            }
            for row in rows
        ],
    }, 200


def _next_id() -> int:
    client = firestore.client()
    ref = client.collection("settings").document("counters")
    transaction = client.transaction()

    @firestore.transactional
    def allocate(transaction):
        snap = ref.get(transaction=transaction)
        current = int((snap.to_dict() or {}).get("requirements") or 0)
        if current <= 0:
            for row in _rows():
                current = max(current, int(row.get("id") or 0))
        nxt = current + 1
        transaction.set(ref, {"requirements": nxt}, merge=True)
        return nxt

    return allocate(transaction)


def create(req, vendor_id: int | None = None) -> tuple[dict, int]:
    if vendor_id is None:
        vendor_id = _vendor_id(req)
    if _vendor(vendor_id) is None:
        return {"status": False, "message": "Valid vendor_id is required"}, 422
    title = _field(req, "title")
    category = _field(req, "category")
    description = _field(req, "description")
    quantity_raw = _field(req, "quantity")
    if title == "" or len(title) > 200:
        return {"status": False, "message": "Enter a requirement title."}, 422
    if category not in CATEGORIES:
        return {"status": False, "message": "Invalid category"}, 422
    quantity = None
    if quantity_raw != "":
        if not quantity_raw.isdigit() or int(quantity_raw) < 1:
            return {"status": False, "message": "Quantity must be at least 1."}, 422
        quantity = int(quantity_raw)
    if len(description) > 2000:
        return {"status": False, "message": "Description is too long."}, 422

    stored_key = title_key(title)
    cluster = resolve(title, category, quantity, description)
    now = _now()
    for row in _rows():
        if int(row.get("vendor_id") or 0) != vendor_id:
            continue
        if _text(row.get("status")) != "active":
            continue
        if _text(row.get("title_key")) != stored_key:
            continue
        firestore.client().collection("requirements").document(str(row.get("id"))).update(
            {
                "title": title,
                "cluster_key": cluster["cluster_key"],
                "canonical_title": cluster["canonical_title"],
                "category": category,
                "quantity": quantity,
                "description": description or None,
                "updated_at": now,
            }
        )
        return {"status": True, "message": "Requirement updated", "data": {"id": int(row.get("id") or 0)}}, 200

    requirement_id = _next_id()
    firestore.client().collection("requirements").document(str(requirement_id)).set(
        {
            "id": requirement_id,
            "vendor_id": vendor_id,
            "title": title,
            "title_key": stored_key,
            "cluster_key": cluster["cluster_key"],
            "canonical_title": cluster["canonical_title"],
            "category": category,
            "quantity": quantity,
            "description": description or None,
            "status": "active",
            "created_at": now,
            "updated_at": now,
        }
    )
    _notify_posted(vendor_id, title, requirement_id)
    return {"status": True, "message": "Requirement posted", "data": {"id": requirement_id}}, 200


def _notify_posted(vendor_id: int, title: str, requirement_id: int) -> None:
    try:
        from push_apns import notify

        vendor = _vendor(vendor_id) or {}
        shop = _text(vendor.get("shop_name")) or _text(vendor.get("username")) or "A vendor"
        ids = []
        for doc in firestore.client().collection("vendors").stream():
            row = doc.to_dict() or {}
            other = int(row.get("id") or 0)
            if other != vendor_id and int(row.get("status") or 0) == 1:
                ids.append(other)
        notify(
            ids,
            "New requirement",
            f"{shop} posted: {title}",
            "requirement",
            {"target_id": str(requirement_id), "target_name": title},
        )
    except Exception:
        return


def _owned(requirement_id: int, vendor_id: int):
    doc = firestore.client().collection("requirements").document(str(requirement_id)).get()
    if not doc.exists:
        return None
    row = doc.to_dict() or {}
    if int(row.get("vendor_id") or 0) != vendor_id:
        return None
    row["_ref"] = doc.reference
    return row


def close(req, requirement_id: int, vendor_id: int | None = None) -> tuple[dict, int]:
    if vendor_id is None:
        vendor_id = _vendor_id(req)
    if vendor_id <= 0:
        return {"status": False, "message": "vendor_id required"}, 422
    row = _owned(requirement_id, vendor_id)
    if row is None:
        return {"status": False, "message": "Requirement not found"}, 404
    row["_ref"].update({"status": "closed", "updated_at": _now()})
    return {"status": True, "message": "Requirement closed"}, 200


def delete(req, requirement_id: int, vendor_id: int | None = None) -> tuple[dict, int]:
    if vendor_id is None:
        vendor_id = _vendor_id(req)
    if vendor_id <= 0:
        return {"status": False, "message": "vendor_id required"}, 422
    row = _owned(requirement_id, vendor_id)
    if row is None:
        return {"status": False, "message": "Requirement not found"}, 404
    row["_ref"].delete()
    return {"status": True, "message": "Requirement deleted"}, 200
