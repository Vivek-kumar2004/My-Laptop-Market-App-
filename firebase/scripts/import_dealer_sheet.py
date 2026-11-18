"""Add dealer-group listings as independent Open Market cards.

These are not vendor products. Repeat posts and buy requests are skipped.
Photos are watermarked into the product media bucket.
"""

from __future__ import annotations

import csv
import re
import secrets
import sys
import time
from datetime import datetime
from io import BytesIO
from pathlib import Path

import bcrypt
import firebase_admin
import hashlib
from firebase_admin import firestore
from google.api_core.exceptions import AlreadyExists
from google.cloud import storage
from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "functions"))

from watermark import apply_watermark  # noqa: E402

CSV_PATH = Path("/Users/apple/Downloads/products_with_contact.csv")
IMAGE_DIR = Path(
    "/Users/apple/Downloads/WhatsApp Chat - Hyderabad IT Tech Deals Laptops & Desktops Wholesale Dealers"
)
PROJECT = "mylaptopmarket-app"
BUCKET = "mylaptopmarket-app-media"
MEDIA_BASE = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api/media/products"
BATCH = "hyderabad-dealers-2026-11-12"

CATEGORIES = {
    "Laptop": "Laptops",
    "Desktop / SFF": "Desktop",
    "Monitor": "TFT/Screen",
    "CPU / Processor": "Accessories",
    "SSD": "Storage",
    "Spare Parts": "Accessories",
    "RAM": "RAM",
    "Accessories": "Accessories",
}
PLACES = (
    ("patparganj", "Delhi", "Patparganj, Delhi"),
    ("navi mumbai", "Maharashtra", "Navi Mumbai"),
    ("mumbai", "Maharashtra", "Mumbai"),
    ("bangalore", "Karnataka", "Bangalore"),
    ("bengaluru", "Karnataka", "Bangalore"),
    ("chennai", "Tamil Nadu", "Chennai"),
    ("hyderabad", "Telangana", "Hyderabad"),
    ("ahmedabad", "Gujarat", "Ahmedabad"),
    ("hosur", "Tamil Nadu", "Hosur"),
    ("gujarat", "Gujarat", "Gujarat"),
    ("delhi", "Delhi", "Delhi"),
    ("pune", "Maharashtra", "Pune"),
    ("kolkata", "West Bengal", "Kolkata"),
    ("nehru place", "Delhi", "Nehru Place"),
    ("new delhi", "Delhi", "New Delhi"),
    ("secunderabad", "Telangana", "Secunderabad"),
    ("trichy", "Tamil Nadu", "Trichy"),
    ("jaipur", "Rajasthan", "Jaipur"),
    ("kerala", "Kerala", "Kerala"),
    ("punjab", "Punjab", "Punjab"),
)


def text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def phones(raw: str) -> list[str]:
    found = []
    for chunk in re.findall(r"\d[\d\s-]{8,}\d", raw or ""):
        digits = re.sub(r"\D", "", chunk)
        if len(digits) > 10 and digits.startswith("91"):
            digits = digits[-10:]
        elif len(digits) > 10:
            digits = digits[-10:]
        if len(digits) == 10 and digits[0] in "6789":
            found.append(digits)
    unique = []
    for item in found:
        if item not in unique:
            unique.append(item)
    return unique


def place(raw: str) -> tuple[str, str]:
    folded = text(raw).lower()
    for needle, state, city in PLACES:
        if needle in folded:
            return state, city
    cleaned = re.sub(r"\s+", " ", text(raw))
    return "", cleaned or "India"


def price_of(row: dict) -> str:
    amounts = []
    for key in ("price_min_inr", "price_text"):
        blob = text(row.get(key)).replace(",", "")
        for chunk in re.findall(r"\d+(?:\.\d+)?", blob):
            value = float(chunk)
            if value > 0:
                amounts.append(value)
    if not amounts:
        return "0"
    amounts = sorted(set(amounts))
    chosen = amounts[0]
    product_prices = [value for value in amounts if value >= 5000]
    if chosen < 5000 and product_prices:
        chosen = product_prices[0]
    return str(int(chosen) if chosen.is_integer() else chosen)


def stock_of(row: dict) -> int:
    raw = text(row.get("product_qty")).replace(",", "")
    blob = f"{text(row.get('product_name'))} {text(row.get('raw_message'))[:400]}"
    if raw.isdigit():
        value = int(raw)
        if re.search(rf"[A-Za-z]{raw}\b", blob):
            return 1
        if 1 <= value <= 20000:
            return value
    return 1


def clean_copy(row: dict) -> str:
    raw = text(row.get("raw_message")) or text(row.get("product_description"))
    raw = raw.replace(" | ", "\n")
    raw = re.sub(r"https://(?:wa\.me|whatsapp\.com)/\S+", "", raw)
    raw = re.sub(r"\[\d{2}/\d{2}/\d{2}[^\]]*\]", "", raw)
    raw = re.sub(r"[*_`]+", "", raw)
    raw = re.sub(r"[ \t]+\n", "\n", raw)
    raw = re.sub(r"\n{3,}", "\n\n", raw).strip()
    return raw[:4500]


def title_of(row: dict) -> str:
    name = re.sub(r"\s+", " ", text(row.get("product_name")))
    name = name.strip(" |")
    if re.fullmatch(r"\+?\d[\d\s+-]{8,}", name):
        name = ""
    if not name:
        name = " ".join(part for part in (text(row.get("brand")), text(row.get("model")), text(row.get("product_type"))) if part)
    if not name:
        first = clean_copy(row).split("\n", 1)[0].strip()
        name = re.sub(r"\s+", " ", first)[:180]
    if len(name) > 180:
        name = name[:177].rstrip() + "..."
    return name or "Laptop stock"


def usable_shop(value: str) -> bool:
    folded = value.lower()
    blocked = ("welcome", "ideal for", "bulk project", "enquir", "limited stock", "contact", "best price")
    return len(value) >= 3 and not any(word in folded for word in blocked)


def shop_label(row: dict, mobile: str) -> str:
    company = re.sub(r"\s+", " ", text(row.get("vendor_company"))).strip(" -|")
    person = re.sub(r"\s+", " ", text(row.get("vendor_name"))).strip(" -|")
    if usable_shop(company):
        label = company
    elif usable_shop(person):
        label = person
    else:
        label = company or person or f"Dealer {mobile}"
    return label[:80] or f"Dealer {mobile}"


def description_of(row: dict, mobile_list: list[str], city: str) -> str:
    body = clean_copy(row)
    person = text(row.get("vendor_name"))
    company = text(row.get("vendor_company"))
    lines = [body] if body else []
    contact = ", ".join(mobile_list)
    footer = []
    if person and person.lower() not in body.lower():
        footer.append(f"Seller: {person}")
    if company and company.lower() not in body.lower():
        footer.append(f"Shop: {company}")
    if contact and contact not in body:
        footer.append(f"Contact: {contact}")
    if city and city.lower() not in body.lower() and city != "India":
        footer.append(f"Location: {city}")
    specs = []
    for label, key in (
        ("Brand", "brand"),
        ("Processor", "processor"),
        ("Generation", "generation"),
        ("RAM", "ram"),
        ("Storage", "storage"),
        ("Screen", "screen"),
        ("Condition", "condition"),
    ):
        value = text(row.get(key))
        if value and value.lower() not in body.lower():
            specs.append(f"{label}: {value}")
    if specs:
        footer.extend(specs)
    if footer:
        lines.append("\n".join(footer))
    return "\n\n".join(part for part in lines if part).strip() or title_of(row)


def _unique_ref(client, kind: str, key: str):
    digest = hashlib.sha256(f"{kind}\n{key}".encode("utf-8")).hexdigest()[:40]
    return client.collection("vendor_uniques").document(f"{kind}_{digest}")


def claim_vendor_keys(client, vendor_id: int, username: str, mobile: str, email: str, shop: str) -> str | None:
    keys = [
        ("username", username.lower(), "The username has already been taken."),
        ("mobile", mobile, "The mobile has already been taken."),
        ("email", email.lower(), "The email has already been taken."),
        ("shop", re.sub(r"\s+", " ", shop).casefold(), "This shop name is already registered."),
    ]
    created = []
    for kind, key, message in keys:
        ref = _unique_ref(client, kind, key)
        try:
            ref.create({"vendor_id": int(vendor_id), "kind": kind})
            created.append(ref)
        except AlreadyExists:
            owner = int((ref.get().to_dict() or {}).get("vendor_id") or 0)
            if owner != int(vendor_id):
                for made in created:
                    made.delete()
                return message
    return None


def next_counter(client, field: str) -> int:
    ref = client.collection("settings").document("counters")
    transaction = client.transaction()

    @firestore.transactional
    def allocate(transaction):
        snap = ref.get(transaction=transaction)
        current = int((snap.to_dict() or {}).get(field) or 0)
        nxt = current + 1
        transaction.set(ref, {field: nxt}, merge=True)
        return nxt

    return allocate(transaction)


def image_names(row: dict) -> list[str]:
    names = []
    for part in text(row.get("product_images")).split(";"):
        name = part.strip()
        if name and name not in names:
            names.append(name)
    return names


def jpeg_under_limit(data: bytes) -> bytes:
    image = ImageOps.exif_transpose(Image.open(BytesIO(data))).convert("RGB")
    quality = 85
    while quality >= 45:
        output = BytesIO()
        image.save(output, format="JPEG", quality=quality, optimize=True)
        encoded = output.getvalue()
        if len(encoded) <= 4_800_000:
            return encoded
        quality -= 10
    image.thumbnail((1600, 1600))
    output = BytesIO()
    image.save(output, format="JPEG", quality=70, optimize=True)
    return output.getvalue()


def upload_photo(bucket, path: Path) -> str:
    original = path.read_bytes()
    marked, content_type = apply_watermark(original, "jpg")
    payload = marked or original
    if len(payload) > 4_800_000 or not content_type:
        payload = jpeg_under_limit(payload)
        content_type = "image/jpeg"
    name = f"{int(time.time())}_{secrets.token_hex(8)}.jpg"
    blob = bucket.blob(f"products/{name}")
    blob.upload_from_string(payload, content_type=content_type or "image/jpeg")
    return f"{MEDIA_BASE}/{name}"


def specs_of(row: dict) -> list[str]:
    specs = []
    kind = text(row.get("product_type"))
    processor = text(row.get("processor"))
    generation = text(row.get("generation"))
    if processor and len(processor) < 40:
        label = processor
        if generation and generation.lower() not in processor.lower():
            label = f"{processor} {generation}"
        specs.append(label)
    ram = text(row.get("ram"))
    if re.search(r"\d", ram) and "GB" in ram.upper():
        specs.append(ram if "RAM" in ram.upper() else f"{ram} RAM")
    storage = text(row.get("storage"))
    storage_type = text(row.get("storage_type"))
    if kind != "Monitor" and re.search(r"\d", storage) and re.search(r"GB|TB", storage, re.I):
        bit = storage
        if storage_type and storage_type.upper() not in bit.upper():
            bit = f"{bit} {storage_type}"
        specs.append(bit)
    screen = text(row.get("screen"))
    if screen and re.search(r"\d", screen):
        specs.append(screen if "inch" in screen.lower() else screen)
    condition = text(row.get("condition"))
    if condition and len(condition) < 48:
        specs.append(condition)
    unique = []
    for item in specs:
        if item not in unique:
            unique.append(item)
    return unique[:6]


def poster_name(row: dict) -> str:
    person = text(row.get("vendor_name"))
    company = text(row.get("vendor_company"))
    if usable_shop(person):
        return person[:80]
    if usable_shop(company):
        return company[:80]
    return person or company or "Dealer"


def selected_rows() -> list[dict]:
    with CSV_PATH.open(newline="", encoding="utf-8-sig") as handle:
        rows = list(csv.DictReader(handle))
    chosen = []
    for row in rows:
        intent = text(row.get("intent")).upper()
        if intent not in {"SELL", "INFO"}:
            continue
        if text(row.get("is_repeat")).lower() == "yes":
            continue
        if not phones(row.get("vendor_contact") or ""):
            continue
        chosen.append(row)
    return chosen


def main() -> None:
    apply = "--apply" in sys.argv
    if not firebase_admin._apps:
        firebase_admin.initialize_app(options={"projectId": PROJECT})
    client = firestore.client()
    done_rows = set()
    for doc in client.collection("market_listings").stream():
        row = doc.to_dict() or {}
        if text(row.get("import_batch")) == BATCH:
            done_rows.add(text(row.get("sheet_row")))

    rows = [row for row in selected_rows() if text(row.get("s_no")) not in done_rows]
    print(f"to_import={len(rows)} already_imported={len(done_rows)} apply={apply}")
    uploaded = 0
    missing_images = []
    bucket = storage.Client().bucket(BUCKET) if apply else None
    for row in rows:
        mobile_list = phones(row.get("vendor_contact") or "")
        mobile = mobile_list[0]
        state, city = place(row.get("vendor_location") or "")
        image_list = image_names(row)
        absent = [name for name in image_list if not (IMAGE_DIR / name).is_file()]
        missing_images.extend(absent)
        print(
            f"#{row.get('s_no')} {title_of(row)[:70]} | {poster_name(row)[:32]} | "
            f"images={len(image_list) - len(absent)}/{len(image_list)} price={price_of(row)} qty={stock_of(row)}"
        )
        if not apply:
            continue
        urls = []
        for name in image_list:
            path = IMAGE_DIR / name
            if not path.is_file():
                continue
            urls.append(upload_photo(bucket, path))
            uploaded += 1
        listing_id = next_counter(client, "market_listings")
        stamp = text(row.get("source_datetime")).replace(" ", "T")
        doc = {
            "id": listing_id,
            "name": title_of(row),
            "category": CATEGORIES.get(text(row.get("product_type")), "Laptops"),
            "description": description_of(row, mobile_list, city),
            "price": price_of(row),
            "stock": stock_of(row),
            "images": urls,
            "specs": specs_of(row),
            "condition": text(row.get("condition"))[:48],
            "contact_name": poster_name(row),
            "contact_phone": mobile,
            "contact_phones": mobile_list,
            "city": city,
            "state": state,
            "status": 1,
            "independent": True,
            "sheet_order": int(text(row.get("s_no")) or listing_id),
            "sheet_row": text(row.get("s_no")),
            "import_batch": BATCH,
            "created_at": stamp or datetime.now().isoformat(timespec="seconds"),
        }
        client.collection("market_listings").document(str(listing_id)).set(doc)
        print(f"  saved open-market listing {listing_id} photos {len(urls)}")

    if missing_images:
        print("missing_images", len(set(missing_images)))
    if apply:
        print(f"uploaded_photos={uploaded}")


if __name__ == "__main__":
    main()
