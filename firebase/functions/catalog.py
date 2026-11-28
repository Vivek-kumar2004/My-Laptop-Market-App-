import json
from datetime import date

from firebase_admin import firestore

from auth import DEFAULT_LOGO, _file_url
from subscription import SHOW_VENDOR_PAYMENT

SITE = "https://mylaptopmarket.com/mylaptop"


def _text(value) -> str:
    if value is None:
        return ""
    return str(value)


def _trusted(vendor: dict) -> bool:
    photo = _text(vendor.get("shop_photo")).strip()
    return int(vendor.get("gst_verified") or 0) == 1 and photo != ""


def _subscription_open(vendor: dict, today: str) -> bool:
    # Payment checkout is paused, so an old end date must not hide a live listing.
    if not SHOW_VENDOR_PAYMENT:
        return True
    end = vendor.get("subscription_end")
    if end is None or _text(end).strip() == "":
        return True
    return _text(end)[:10] >= today


def _active_vendor(vendor: dict, today: str) -> bool:
    return int(vendor.get("status") or 0) == 1 and _subscription_open(vendor, today)


def _images(raw) -> list[str]:
    if isinstance(raw, list):
        return [item for item in raw if isinstance(item, str) and item]
    if isinstance(raw, str) and raw:
        try:
            decoded = json.loads(raw)
        except json.JSONDecodeError:
            decoded = None
        if isinstance(decoded, list):
            return [item for item in decoded if isinstance(item, str) and item]
        return [raw]
    return []


def _file_name(value: str) -> str:
    return value.split("?")[0].rstrip("/").split("/")[-1]


def _image_parts(raw) -> tuple[list[str], list[str]]:
    names = []
    urls = []
    for item in _images(raw):
        if item.startswith("http://") or item.startswith("https://"):
            urls.append(item)
            names.append(_file_name(item))
        else:
            name = _file_name(item)
            names.append(name)
            urls.append(f"{SITE}/assets/uploads/products/{name}")
    return names, urls


def _logo_url(vendor: dict) -> str:
    return _file_url(vendor.get("logo_url") or vendor.get("logo")) or DEFAULT_LOGO


def _display_images(product: dict) -> tuple[str, list[str], list[str]]:
    front_raw = _text(product.get("front_image"))
    _, front_urls = _image_parts(front_raw) if front_raw else ([], [])
    front = front_urls[0] if front_urls else ""
    names, urls = _image_parts(product.get("images"))
    ordered = [front] if front else []
    front_name = _file_name(front) if front else ""
    for url in urls:
        if front_name and _file_name(url) == front_name:
            continue
        ordered.append(url)
    return front, names, ordered


def format_product(product: dict, vendor: dict, include_vendor: bool, for_web: bool = False) -> dict:
    front, names, urls = _display_images(product)
    fallback = f"{SITE}/assets/images/no-product.svg"
    if for_web:
        item = {
            "id": product.get("id"),
            "product_name": product.get("product_name"),
            "category": product.get("category"),
            "description": product.get("description"),
            "price": _text(product.get("price")),
            "stock": product.get("stock"),
            "youtube_video": product.get("youtube_video"),
            "images": urls,
            "image_urls": urls,
            "main_image": urls[0] if urls else fallback,
            "front_image": front,
            "has_stock_sheet": bool(product.get("has_stock_sheet")),
            "vendor_shop_trusted": _trusted(vendor),
        }
    else:
        item = {
            "id": product.get("id"),
            "vendor_id": product.get("vendor_id"),
            "product_name": product.get("product_name"),
            "category": product.get("category"),
            "description": product.get("description"),
            "price": _text(product.get("price")),
            "stock": product.get("stock"),
            "status": int(product.get("status") or 0),
            "youtube_video": product.get("youtube_video"),
            "images": json.dumps(names),
            "image_urls": urls,
            "main_image": urls[0] if urls else fallback,
            "front_image": front,
            "created_at": product.get("created_at"),
            "updated_at": product.get("updated_at"),
            "has_stock_sheet": bool(product.get("has_stock_sheet")),
            "vendor_shop_trusted": _trusted(vendor),
            "vendor_gst_verified": int(vendor.get("gst_verified") or 0),
            "vendor_shop_photo": vendor.get("shop_photo"),
            "vendor_state": vendor.get("state") or "",
            "vendor_city": vendor.get("location") or "",
            "vendor_logo_url": _logo_url(vendor),
        }
    if include_vendor:
        item.update(
            {
                "vendor_id": product.get("vendor_id"),
                "vendor_name": vendor.get("shop_name"),
                "vendor_number": vendor.get("mobile"),
                "vendor_logo": vendor.get("logo"),
                "vendor_logo_url": _logo_url(vendor),
            }
        )
        if for_web:
            item["vendor_location"] = vendor.get("location") or ""
    return item


def announcement_text() -> str:
    doc = firestore.client().collection("settings").document("announcement").get()
    if not doc.exists:
        return ""
    return _text((doc.to_dict() or {}).get("text")).strip()


def list_products(
    category: str = "",
    vendor_id: str = "",
    search: str = "",
    include_vendor: bool = False,
    only_active: bool = True,
    search_shop: bool = True,
    for_web: bool = False,
) -> tuple[list[dict], list[dict]]:
    today = date.today().isoformat()
    client = firestore.client()
    vendors = {doc.id: doc.to_dict() or {} for doc in client.collection("vendors").stream()}
    products = []
    vendor_counts: dict[str, int] = {}
    needle = search.strip().lower()

    for doc in client.collection("products").stream():
        product = doc.to_dict() or {}
        if only_active and int(product.get("status") or 0) != 1:
            continue
        vendor_key = str(product.get("vendor_id") or "")
        vendor = vendors.get(vendor_key)
        if vendor is None:
            vendor = next((row for key, row in vendors.items() if str(row.get("id") or key) == vendor_key), None)
        if vendor is None:
            if only_active:
                continue
            vendor = {}
        elif only_active and not _active_vendor(vendor, today):
            continue
        if category and _text(product.get("category")) != category:
            continue
        if vendor_id and vendor_key != str(vendor_id):
            continue
        name = _text(product.get("product_name")).lower()
        cat = _text(product.get("category")).lower()
        shop = _text(vendor.get("shop_name")).lower()
        shop_hit = search_shop and needle in shop
        if needle and needle not in name and needle not in cat and not shop_hit:
            continue
        products.append(format_product(product, vendor, include_vendor, for_web))
        vendor_counts[vendor_key] = vendor_counts.get(vendor_key, 0) + 1

    products.sort(key=lambda row: int(row.get("id") or 0), reverse=True)

    vendor_matches = []
    if needle and not vendor_id:
        for key, count in vendor_counts.items():
            vendor = vendors.get(key) or {}
            shop = _text(vendor.get("shop_name")).lower()
            if needle not in shop:
                continue
            match = {
                "vendor_id": vendor.get("id") or (int(key) if key.isdigit() else key),
                "vendor_name": vendor.get("shop_name"),
                "vendor_location": vendor.get("location") or "",
                "product_count": count,
            }
            logo = _file_url(vendor.get("logo_url") or vendor.get("logo"))
            if logo:
                match["vendor_logo_url"] = logo
            vendor_matches.append(match)
        vendor_matches.sort(key=lambda row: int(row.get("product_count") or 0), reverse=True)
        vendor_matches = vendor_matches[:20]

    return products, vendor_matches


def product_categories() -> dict:
    products, _vendors = list_products()
    names = sorted({_text(row.get("category")).strip() for row in products if _text(row.get("category")).strip()})
    return {
        "status": True,
        "message": "Categories fetched successfully",
        "data": names,
    }


def public_product_list(category: str = "", search: str = "") -> dict:
    products, _vendors = list_products(category, search=search, search_shop=False, for_web=True)
    return {
        "status": True,
        "message": "Product list fetched successfully",
        "authenticated": False,
        "data": products,
    }


def home_showcase() -> dict:
    products, _vendors = list_products(include_vendor=True)
    cards = []
    grouped: dict[str, dict] = {}
    for product in products:
        key = str(product.get("vendor_id") or "")
        logo = product.get("vendor_logo") or product.get("vendor_logo_url")
        logo_url = _file_url(logo) or DEFAULT_LOGO
        bucket = grouped.get(key)
        if bucket is None:
            bucket = {
                "vendor_id": product.get("vendor_id"),
                "vendor_name": product.get("vendor_name"),
                "vendor_location": product.get("vendor_location") or product.get("vendor_city") or "",
                "product_count": 0,
                "has_logo": bool(_text(product.get("vendor_logo")).strip()),
                "vendor_logo_url": logo_url,
            }
            grouped[key] = bucket
        bucket["product_count"] += 1
        if len(cards) < 8:
            card = {
                "id": product.get("id"),
                "product_name": product.get("product_name"),
                "category": product.get("category"),
                "price": product.get("price"),
                "stock": product.get("stock"),
                "image_urls": product.get("image_urls") or [],
                "main_image": product.get("main_image"),
                "has_stock_sheet": product.get("has_stock_sheet"),
                "vendor_shop_trusted": product.get("vendor_shop_trusted"),
                "vendor_id": product.get("vendor_id"),
                "vendor_name": product.get("vendor_name"),
                "vendor_logo_url": logo_url,
            }
            cards.append(card)
    vendors = list(grouped.values())
    vendors.sort(key=lambda row: (0 if row.get("has_logo") else 1, -int(row.get("product_count") or 0)))
    for row in vendors:
        row.pop("has_logo", None)
    return {"status": True, "products": cards, "vendors": vendors[:10]}
