from firebase_admin import firestore

FALLBACK = "https://mylaptopmarket.com/mylaptop/assets/images/no-product.svg"


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _images(raw) -> list[str]:
    if isinstance(raw, list):
        return [item for item in raw if isinstance(item, str) and item]
    return []


def _specs(raw) -> list[str]:
    if isinstance(raw, list):
        return [_text(item) for item in raw if _text(item)]
    return []


def format_listing(row: dict) -> dict:
    images = _images(row.get("images"))
    price = _text(row.get("price")) or "0"
    return {
        "id": row.get("id"),
        "name": _text(row.get("name")),
        "category": _text(row.get("category")),
        "description": _text(row.get("description")),
        "price": price,
        "stock": int(row.get("stock") or 0),
        "images": images,
        "main_image": images[0] if images else FALLBACK,
        "specs": _specs(row.get("specs")),
        "condition": _text(row.get("condition")),
        "contact_name": _text(row.get("contact_name")),
        "contact_phone": _text(row.get("contact_phone")),
        "city": _text(row.get("city")),
        "state": _text(row.get("state")),
        "independent": True,
    }


def _rows() -> list[dict]:
    listings = []
    for doc in firestore.client().collection("market_listings").stream():
        row = doc.to_dict() or {}
        if int(row.get("status") or 0) != 1:
            continue
        listings.append(row)
    listings.sort(key=lambda row: (int(row.get("sheet_order") or 0), int(row.get("id") or 0)))
    return listings


def list_market(category: str = "", search: str = "") -> dict:
    needle = search.strip().lower()
    data = []
    for row in _rows():
        if category and _text(row.get("category")) != category:
            continue
        haystack = " ".join(
            [
                _text(row.get("name")),
                _text(row.get("category")),
                _text(row.get("city")),
                _text(row.get("contact_name")),
                " ".join(_specs(row.get("specs"))),
            ]
        ).lower()
        if needle and needle not in haystack:
            continue
        data.append(format_listing(row))
    return {
        "status": True,
        "message": "Open market listings fetched successfully",
        "data": data,
    }


def market_listing(listing_id: int) -> tuple[dict, int]:
    doc = firestore.client().collection("market_listings").document(str(listing_id)).get()
    if not doc.exists:
        return {"status": False, "message": "Listing not found"}, 404
    row = doc.to_dict() or {}
    if int(row.get("status") or 0) != 1:
        return {"status": False, "message": "Listing not found"}, 404
    return {"status": True, "message": "Listing fetched successfully", "data": format_listing(row)}, 200
