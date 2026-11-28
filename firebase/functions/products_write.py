from __future__ import annotations

import json
import re
import secrets
import time
import zipfile
import xml.etree.ElementTree as ET
from datetime import datetime
from io import BytesIO
from zoneinfo import ZoneInfo

from firebase_admin import firestore
from google.cloud import storage

BUCKET = "mylaptopmarket-app-media"
MEDIA_BASE = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api/media/products"
VENDOR_MEDIA_BASE = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api/media/vendors"
CATEGORIES = ["Laptops", "Desktop", "TFT/Screen", "Server", "Storage", "Accessories", "Graphics Card"]
IMAGE_TYPES = {
    "image/jpeg": "jpg",
    "image/jpg": "jpg",
    "image/png": "png",
    "image/gif": "gif",
    "image/webp": "webp",
    "image/heic": "heic",
    "image/heif": "heif",
}
MAX_BYTES = 5 * 1024 * 1024
MAX_ROWS = 400
MAX_COLS = 12


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _field(req, name: str) -> str:
    if req.form and name in req.form:
        return _text(req.form.get(name))
    body = req.get_json(silent=True) or {}
    if name in body:
        return _text(body.get(name))
    return _text(req.args.get(name))


def _bucket():
    return storage.Client().bucket(BUCKET)


def _file_name(value: str) -> str:
    return _text(value).split("?")[0].rstrip("/").split("/")[-1]


def _vendor(vendor_id: int):
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    return doc.to_dict() or {}


def _product(product_id: int):
    doc = firestore.client().collection("products").document(str(product_id)).get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}
    data["_ref"] = doc.reference
    return data


def _next_product_id() -> int:
    client = firestore.client()
    ref = client.collection("settings").document("counters")
    transaction = client.transaction()

    @firestore.transactional
    def allocate(transaction):
        snap = ref.get(transaction=transaction)
        current = int((snap.to_dict() or {}).get("products") or 0)
        if current <= 0:
            for doc in client.collection("products").stream():
                current = max(current, int((doc.to_dict() or {}).get("id") or 0))
        nxt = current + 1
        transaction.set(ref, {"products": nxt}, merge=True)
        return nxt

    return allocate(transaction)


def _images_of(product: dict) -> list[str]:
    raw = product.get("images")
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


def _save_image(upload, folder: str = "products") -> tuple[str | None, str | None]:
    data = upload.read()
    if len(data) > MAX_BYTES:
        return None, "Each photo must be 5 MB or smaller."
    content_type = (upload.mimetype or "").split(";")[0].lower()
    ext = IMAGE_TYPES.get(content_type)
    if ext is None:
        guessed = _file_name(upload.filename or "").rsplit(".", 1)[-1].lower()
        if guessed == "jpeg":
            guessed = "jpg"
        if guessed not in {"jpg", "png", "gif", "webp", "heic", "heif"}:
            return None, "Upload a jpeg, png, gif, or webp photo."
        ext = guessed
        content_type = {
            "jpg": "image/jpeg",
            "png": "image/png",
            "gif": "image/gif",
            "webp": "image/webp",
        }.get(ext, "application/octet-stream")
    if folder == "products":
        from watermark import apply_watermark

        data, marked_type = apply_watermark(data, ext)
        if marked_type:
            content_type = marked_type
    name = f"{int(time.time())}_{secrets.token_hex(8)}.{ext}"
    blob = _bucket().blob(f"{folder}/{name}")
    blob.upload_from_string(data, content_type=content_type)
    base = MEDIA_BASE if folder == "products" else VENDOR_MEDIA_BASE
    return f"{base}/{name}", None


def _uploads(req) -> list:
    files = req.files.getlist("images[]")
    if not files:
        files = req.files.getlist("images")
    return [item for item in files if item and item.filename]


def _store_front(req, current: str = "") -> tuple[str, str | None]:
    upload = req.files.get("front_image") if req.files else None
    if upload is not None and getattr(upload, "filename", ""):
        url, error = _save_image(upload)
        if error:
            return current, error
        if current and current != url:
            _delete_stored_image(current)
        return url, None
    if _field(req, "remove_front_image") in {"1", "true", "on"}:
        if current:
            _delete_stored_image(current)
        return "", None
    return current, None


def _delete_stored_image(value: str) -> None:
    folder = ""
    if "/api/media/products/" in value:
        folder = "products"
    elif "/api/media/vendors/" in value:
        folder = "vendors"
    if not folder:
        return
    name = _file_name(value)
    if not name or "/" in name or ".." in name:
        return
    blob = _bucket().blob(f"{folder}/{name}")
    if blob.exists():
        blob.delete()


def read_stored_image(folder: str, name: str):
    from flask import Response

    if folder not in {"products", "vendors"} or not re.fullmatch(r"[A-Za-z0-9._-]+", name):
        return Response("Not found", status=404)
    blob = _bucket().blob(f"{folder}/{name}")
    if not blob.exists():
        return Response("Not found", status=404)
    blob.reload()
    data = blob.download_as_bytes()
    response = Response(data, status=200, mimetype=blob.content_type or "application/octet-stream")
    response.headers["Cache-Control"] = "public, max-age=86400"
    response.headers["Access-Control-Allow-Origin"] = "*"
    return response


def read_product_image(name: str):
    return read_stored_image("products", name)


def add_product(req, vendor_id: int | None = None) -> tuple[dict, int]:
    vendor_raw = str(vendor_id) if vendor_id else _field(req, "vendor_id")
    name = _field(req, "product_name")
    category = _field(req, "category")
    description = _field(req, "description")
    price = _field(req, "price")
    stock = _field(req, "stock")
    if not vendor_raw.isdigit() or name == "" or category == "" or description == "" or price == "" or stock == "":
        return {"status": False, "message": "Check product name, category, description, price and stock."}, 422
    try:
        price_value = float(price)
        stock_value = int(stock)
    except ValueError:
        return {"status": False, "message": "Price and stock must be numbers."}, 422
    vendor_id = int(vendor_raw)
    if _vendor(vendor_id) is None:
        return {"status": False, "message": "Vendor not found"}, 404

    images = []
    for upload in _uploads(req):
        url, error = _save_image(upload)
        if error:
            return {"status": False, "message": error}, 422
        images.append(url)
    front, error = _store_front(req)
    if error:
        return {"status": False, "message": error}, 422

    status = _field(req, "status") or "1"
    product_id = _next_product_id()
    doc = {
        "id": product_id,
        "vendor_id": vendor_id,
        "product_name": name,
        "category": category,
        "description": description,
        "price": str(price_value if "." in price else int(price_value) if price_value.is_integer() else price_value),
        "stock": stock_value,
        "youtube_video": _field(req, "youtube_video") or None,
        "images": images,
        "front_image": front,
        "status": int(status) if status.lstrip("-").isdigit() else 1,
        "has_stock_sheet": False,
    }
    firestore.client().collection("products").document(str(product_id)).set(doc)
    from alerts import product_posted

    product_posted(vendor_id, name, product_id, int(doc["status"]))
    return {"status": True, "message": "Product added successfully", "data": {"id": product_id}}, 200


def update_product(req, product_id: int, vendor_id: int | None = None) -> tuple[dict, int]:
    vendor_raw = str(vendor_id) if vendor_id else _field(req, "vendor_id")
    if not vendor_raw.isdigit():
        return {"status": False, "message": "Product not found"}, 404
    product = _product(product_id)
    if product is None or int(product.get("vendor_id") or 0) != int(vendor_raw):
        return {"status": False, "message": "Product not found"}, 404

    name = _field(req, "product_name")
    category = _field(req, "category")
    description = _field(req, "description")
    price = _field(req, "price")
    stock = _field(req, "stock")
    if name == "" or category == "" or description == "" or price == "" or stock == "":
        return {"status": False, "message": "Check product name, category, description, price and stock."}, 422
    allowed = list(CATEGORIES)
    current = _text(product.get("category"))
    if current and current not in allowed:
        allowed.append(current)
    if category not in allowed:
        return {"status": False, "message": "Invalid category"}, 422
    try:
        price_value = float(price)
        stock_value = int(stock)
    except ValueError:
        return {"status": False, "message": "Price and stock must be numbers."}, 422

    images = _images_of(product)
    if req.form and "existing_images" in req.form:
        by_name = {_file_name(item): item for item in images}
        ordered = []
        for item in req.form.getlist("existing_images"):
            key = _file_name(item)
            if key in by_name:
                ordered.append(by_name.pop(key))
        for item in by_name.values():
            _delete_stored_image(item)
        images = ordered
    elif "keep_images" in (req.form or {}) or (req.get_json(silent=True) or {}).get("keep_images") is not None and req.is_json:
        raw = req.form.get("keep_images") if req.form and "keep_images" in req.form else (req.get_json(silent=True) or {}).get("keep_images")
        if isinstance(raw, str):
            try:
                raw = json.loads(raw)
            except json.JSONDecodeError:
                raw = []
        keep = {_file_name(item) for item in raw or [] if isinstance(item, str)}
        removed = [item for item in images if _file_name(item) not in keep]
        images = [item for item in images if _file_name(item) in keep]
        for item in removed:
            _delete_stored_image(item)

    for upload in _uploads(req):
        url, error = _save_image(upload)
        if error:
            return {"status": False, "message": error}, 422
        images.append(url)
    front, error = _store_front(req, _text(product.get("front_image")))
    if error:
        return {"status": False, "message": error}, 422

    status = _field(req, "status")
    product["_ref"].update(
        {
            "product_name": name,
            "category": category,
            "description": description,
            "price": str(int(price_value) if price_value.is_integer() else price_value),
            "stock": stock_value,
            "youtube_video": _field(req, "youtube_video") or None,
            "images": images,
            "front_image": front,
            "status": int(status) if status.lstrip("-").isdigit() else int(product.get("status") or 1),
        }
    )
    return {"status": True, "message": "Product updated successfully", "data": {"id": product_id}}, 200


def delete_product(req, product_id: int) -> tuple[dict, int]:
    vendor_raw = _field(req, "vendor_id")
    if not vendor_raw.isdigit() or int(vendor_raw) <= 0:
        return {"status": False, "message": "vendor_id is required"}, 422
    product = _product(product_id)
    if product is None or int(product.get("vendor_id") or 0) != int(vendor_raw):
        return {"status": False, "message": "Product not found"}, 404
    for item in _images_of(product):
        _delete_stored_image(item)
    _delete_stored_image(_text(product.get("front_image")))
    _delete_sheet_blob(product_id)
    product["_ref"].delete()
    return {"status": True, "message": "Product deleted successfully"}, 200


def _sheet_ref(product_id: int):
    return firestore.client().collection("stock_sheets").document(str(product_id))


def _delete_sheet_blob(product_id: int) -> None:
    doc = _sheet_ref(product_id).get()
    if not doc.exists:
        return
    path = _text((doc.to_dict() or {}).get("storage_path"))
    if path:
        blob = _bucket().blob(path)
        if blob.exists():
            blob.delete()
    doc.reference.delete()


def _local(tag: str) -> str:
    return tag.split("}")[-1]


def _column_index(cell_ref: str) -> int:
    letters = re.sub(r"[^A-Z]", "", cell_ref.upper()) or "A"
    index = 0
    for char in letters:
        index = index * 26 + (ord(char) - 64)
    return max(0, index - 1)


def _parse_xlsx(data: bytes) -> dict:
    try:
        archive = zipfile.ZipFile(BytesIO(data))
    except zipfile.BadZipFile:
        return {"headers": [], "rows": [], "truncated": False}
    shared = []
    if "xl/sharedStrings.xml" in archive.namelist():
        root = ET.fromstring(archive.read("xl/sharedStrings.xml"))
        for item in root:
            if _local(item.tag) != "si":
                continue
            parts = []
            for node in item.iter():
                if _local(node.tag) == "t" and node.text:
                    parts.append(node.text)
            shared.append("".join(parts))
    sheet_name = "xl/worksheets/sheet1.xml"
    if sheet_name not in archive.namelist():
        return {"headers": [], "rows": [], "truncated": False}
    root = ET.fromstring(archive.read(sheet_name))
    matrix = []
    for row in root.iter():
        if _local(row.tag) != "row":
            continue
        line = []
        cursor = 0
        for cell in list(row):
            if _local(cell.tag) != "c":
                continue
            col = _column_index(cell.attrib.get("r", ""))
            while cursor < col:
                line.append("")
                cursor += 1
            kind = cell.attrib.get("t", "")
            value = ""
            if kind == "s":
                raw = ""
                for child in cell:
                    if _local(child.tag) == "v" and child.text:
                        raw = child.text
                value = shared[int(raw)] if raw.isdigit() and int(raw) < len(shared) else ""
            elif kind == "inlineStr":
                value = "".join(node.text or "" for node in cell.iter() if _local(node.tag) == "t")
            else:
                for child in cell:
                    if _local(child.tag) == "v" and child.text:
                        value = child.text
            line.append(value)
            cursor += 1
        matrix.append(line)
    return _limit_matrix(matrix)


def _limit_matrix(matrix: list) -> dict:
    kept = []
    for row in matrix:
        if any(_text(cell) for cell in row):
            kept.append(row)
    truncated = len(kept) > MAX_ROWS + 1
    kept = kept[: MAX_ROWS + 1]
    width = min(MAX_COLS, max((len(row) for row in kept), default=0))
    if any(len(row) > MAX_COLS for row in kept):
        truncated = True
    headers = []
    first = kept[0] if kept else []
    for index in range(width):
        label = _text(first[index] if index < len(first) else "")
        headers.append(label or f"Column {index + 1}")
    rows = []
    for row in kept[1:]:
        rows.append([_text(row[index] if index < len(row) else "") for index in range(width)])
    return {"headers": headers, "rows": rows, "truncated": truncated}


def upload_stock_sheet(req, product_id: int, vendor_id: int | None = None) -> tuple[dict, int]:
    vendor_raw = str(vendor_id) if vendor_id else _field(req, "vendor_id")
    if not vendor_raw.isdigit():
        return {"status": False, "message": "Product not found"}, 404
    product = _product(product_id)
    if product is None or int(product.get("vendor_id") or 0) != int(vendor_raw):
        return {"status": False, "message": "Product not found"}, 404
    upload = req.files.get("file") or req.files.get("stock_sheet")
    if upload is None or not upload.filename:
        return {"status": False, "message": "Upload an Excel file (.xlsx or .xls)"}, 422
    ext = _file_name(upload.filename).rsplit(".", 1)[-1].lower()
    if ext not in {"xlsx", "xls"}:
        return {"status": False, "message": "Upload an Excel file (.xlsx or .xls)"}, 422
    data = upload.read()
    if len(data) > MAX_BYTES:
        return {"status": False, "message": "Excel file must be 5 MB or smaller"}, 422
    if not data.startswith(b"PK"):
        return {"status": False, "message": "Save the sheet as .xlsx and upload it again."}, 422
    parsed = _parse_xlsx(data)
    stored = f"stock-sheets/{product_id}_{secrets.token_hex(12)}.xlsx"
    _delete_sheet_blob(product_id)
    _bucket().blob(stored).upload_from_string(
        data,
        content_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    )
    _sheet_ref(product_id).set(
        {
            "product_id": product_id,
            "storage_path": stored,
            "original_name": _file_name(upload.filename) or "stock.xlsx",
            "row_count": len(parsed["rows"]),
        }
    )
    product["_ref"].update({"has_stock_sheet": True})
    return {
        "status": True,
        "message": "Stock sheet saved",
        "data": {
            "product_id": product_id,
            "original_name": _file_name(upload.filename) or "stock.xlsx",
            "row_count": len(parsed["rows"]),
        },
    }, 200


def read_stock_sheet(product_id: int, vendor_id: int) -> tuple[dict, int]:
    if vendor_id <= 0:
        return {"status": False, "message": "Login required"}, 401
    vendor = _vendor(vendor_id)
    if vendor is None or int(vendor.get("status") or 0) != 1:
        return {"status": False, "message": "Login required"}, 401
    doc = _sheet_ref(product_id).get()
    if not doc.exists:
        return {"status": False, "message": "No stock sheet on this listing"}, 404
    meta = doc.to_dict() or {}
    blob = _bucket().blob(_text(meta.get("storage_path")))
    if not blob.exists():
        return {"status": False, "message": "Stock sheet file is missing"}, 404
    parsed = _parse_xlsx(blob.download_as_bytes())
    viewed = datetime.now(ZoneInfo("Asia/Kolkata")).strftime("%d %b %Y, %I:%M %p")
    return {
        "status": True,
        "original_name": meta.get("original_name") or "",
        "viewer_name": vendor.get("shop_name") or "Vendor",
        "viewed_at": viewed,
        "headers": parsed["headers"],
        "rows": parsed["rows"],
        "truncated": parsed["truncated"],
    }, 200


def delete_stock_sheet(req, product_id: int, vendor_id: int | None = None) -> tuple[dict, int]:
    vendor_raw = str(vendor_id) if vendor_id else _field(req, "vendor_id")
    if not vendor_raw.isdigit():
        return {"status": False, "message": "Product not found"}, 404
    product = _product(product_id)
    if product is None or int(product.get("vendor_id") or 0) != int(vendor_raw):
        return {"status": False, "message": "Product not found"}, 404
    _delete_sheet_blob(product_id)
    product["_ref"].update({"has_stock_sheet": False})
    return {"status": True, "message": "Stock sheet removed"}, 200
