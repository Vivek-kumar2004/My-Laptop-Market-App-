import json
import re
from datetime import date, datetime, timezone

from firebase_admin import firestore

from catalog import SITE

DEFAULT_LOGO = "https://storage.googleapis.com/mylaptopmarket-app-web/images/mylaptoplogo.png"


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


def _open(vendor: dict, today: str) -> bool:
    if int(vendor.get("status") or 0) != 1:
        return False
    end = _text(vendor.get("subscription_end"))
    if end == "":
        return True
    return end[:10] >= today


def _image_urls(raw) -> list[str]:
    if isinstance(raw, list):
        names = raw
    elif isinstance(raw, str) and raw:
        try:
            decoded = json.loads(raw)
        except json.JSONDecodeError:
            decoded = [raw]
        names = decoded if isinstance(decoded, list) else [raw]
    else:
        names = []
    urls = []
    for item in names:
        if not isinstance(item, str) or not item.strip():
            continue
        name = item.strip().split("/")[-1]
        if name.startswith("http://") or name.startswith("https://"):
            urls.append(name)
        else:
            urls.append(f"{SITE}/assets/uploads/products/{name}")
    return urls


def _logo_url(vendor: dict) -> str:
    raw = _text(vendor.get("logo_url") or vendor.get("logo"))
    if raw.startswith("http://") or raw.startswith("https://"):
        return raw.split("?")[0]
    if raw:
        return f"{SITE}/assets/uploads/vendors/{raw.split('/')[-1]}"
    return DEFAULT_LOGO


def _card(product: dict, vendor: dict) -> dict:
    urls = _image_urls(product.get("images"))
    row = dict(product)
    row.pop("_ref", None)
    row["image_urls"] = urls
    row["main_image"] = urls[0] if urls else f"{SITE}/assets/images/no-product.svg"
    row["shop_name"] = vendor.get("shop_name")
    row["mobile"] = vendor.get("mobile")
    row["location"] = vendor.get("location")
    row["vendor_logo"] = vendor.get("logo")
    row["vendor_logo_url"] = _logo_url(vendor)
    return row


def _visible():
    today = date.today().isoformat()
    client = firestore.client()
    vendors = {doc.id: doc.to_dict() or {} for doc in client.collection("vendors").stream()}
    rows = []
    for doc in client.collection("retail_products").stream():
        product = doc.to_dict() or {}
        if int(product.get("status") or 0) != 1:
            continue
        vendor = vendors.get(str(product.get("vendor_id"))) or {}
        if not _open(vendor, today):
            continue
        rows.append(_card(product, vendor))
    rows.sort(key=lambda row: int(row.get("id") or 0), reverse=True)
    return rows


def products(search: str = "", category: str = "") -> dict:
    needle = search.lower()
    chosen = category if category.lower() != "all" else ""
    rows = []
    for row in _visible():
        if chosen and _text(row.get("category")) != chosen:
            continue
        blob = " ".join(
            [
                _text(row.get("product_name")),
                _text(row.get("category")),
                _text(row.get("description")),
                _text(row.get("shop_name")),
            ]
        ).lower()
        if needle and needle not in blob:
            continue
        rows.append(row)
    return {"success": True, "count": len(rows), "products": rows}


def product(product_id: int) -> tuple[dict, int]:
    for row in _visible():
        if int(row.get("id") or 0) == product_id:
            return {"success": True, "product": row}, 200
    return {"success": False, "message": "Product not found"}, 404


def categories() -> dict:
    names = sorted({_text(row.get("category")) for row in _visible() if _text(row.get("category"))})
    return {"success": True, "categories": names}


def _next_inquiry_id() -> int:
    client = firestore.client()
    ref = client.collection("settings").document("counters")
    transaction = client.transaction()

    @firestore.transactional
    def allocate(transaction):
        snap = ref.get(transaction=transaction)
        current = int((snap.to_dict() or {}).get("retail_inquiries") or 0)
        if current <= 0:
            for doc in client.collection("retail_inquiries").stream():
                current = max(current, int((doc.to_dict() or {}).get("id") or 0))
        nxt = current + 1
        transaction.set(ref, {"retail_inquiries": nxt}, merge=True)
        return nxt

    return allocate(transaction)


def inquiry(req) -> tuple[dict, int]:
    if _field(req, "company_url_hp") != "":
        return {"success": True, "message": "Enquiry sent! The seller will contact you soon."}, 201
    product_raw = _field(req, "retail_product_id")
    name = _field(req, "customer_name")
    phone = _field(req, "phone")
    email = _field(req, "email")
    message = _field(req, "message")
    if not product_raw.isdigit() or name == "" or phone == "" or message == "":
        return {"success": False, "message": "Please check the form and try again."}, 422
    if len(name) > 200 or len(phone) > 20 or len(message) > 5000:
        return {"success": False, "message": "Please check the form and try again."}, 422
    if email and not re.fullmatch(r"[^@\s]+@[^@\s]+\.[^@\s]+", email):
        return {"success": False, "message": "Please check the form and try again."}, 422
    doc = firestore.client().collection("retail_products").document(product_raw).get()
    if not doc.exists:
        return {"success": False, "message": "Please check the form and try again."}, 422
    product_row = doc.to_dict() or {}
    now = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    inquiry_id = _next_inquiry_id()
    firestore.client().collection("retail_inquiries").document(str(inquiry_id)).set(
        {
            "id": inquiry_id,
            "retail_product_id": int(product_raw),
            "vendor_id": int(product_row.get("vendor_id") or 0),
            "customer_name": name,
            "phone": phone,
            "email": email or None,
            "message": message,
            "status": "new",
            "created_at": now,
            "updated_at": now,
        }
    )
    return {
        "success": True,
        "message": "Enquiry sent! The seller will contact you soon.",
        "id": inquiry_id,
    }, 201
