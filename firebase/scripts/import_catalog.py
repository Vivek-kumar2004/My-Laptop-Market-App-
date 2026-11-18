"""Copy products and shop accounts from the MySQL dump into Firestore.

Only the password hash is copied, never the plain password.
Photos stay on the current website; this stores those image links only.
"""

import json
import re
import sys
from pathlib import Path

import firebase_admin
from firebase_admin import firestore

SQL_PATH = Path("/Users/apple/Downloads/mylaptop_data (1).sql")
PROJECT = "mylaptopmarket-app"
SITE = "https://mylaptopmarket.com/mylaptop"

VENDOR_FIELDS = (
    "id",
    "username",
    "shop_name",
    "logo",
    "mobile",
    "email",
    "status",
    "location",
    "subscription_start",
    "subscription_end",
    "state",
    "gstin",
    "gst_verified",
    "shop_photo",
    "admin_warning",
    "state_code",
    "billing_address",
    "contact_person",
    "created_at",
)


def parse_values(sql: str, table: str) -> list[list]:
    rows = []
    for match in re.finditer(rf"INSERT INTO `{table}` \((.*?)\) VALUES\s*", sql, re.S):
        columns = re.findall(r"`(\w+)`", match.group(1))
        index = match.end()
        tuples = _read_tuples(sql, index)
        for values in tuples:
            if len(values) != len(columns):
                raise SystemExit(f"{table} column mismatch: {len(columns)} columns, {len(values)} values")
            rows.append(dict(zip(columns, values)))
    return rows


def _read_tuples(sql: str, index: int) -> list[list]:
    tuples = []
    while index < len(sql):
        while index < len(sql) and sql[index] in " \r\n\t":
            index += 1
        if index >= len(sql) or sql[index] == ";":
            break
        if sql[index] == ",":
            index += 1
            continue
        if sql[index] != "(":
            raise SystemExit(f"expected tuple at {index}: {sql[index:index+40]!r}")
        index += 1
        values = []
        while True:
            while index < len(sql) and sql[index] in " \r\n\t":
                index += 1
            value, index = _read_value(sql, index)
            values.append(value)
            while index < len(sql) and sql[index] in " \r\n\t":
                index += 1
            if sql[index] == ",":
                index += 1
                continue
            if sql[index] == ")":
                index += 1
                tuples.append(values)
                break
            raise SystemExit(f"bad tuple at {index}: {sql[index:index+40]!r}")
    return tuples


def _read_value(sql: str, index: int):
    if sql.startswith("NULL", index):
        return None, index + 4
    if sql[index] != "'":
        end = index
        while sql[end] not in ",)":
            end += 1
        return _number(sql[index:end].strip()), end
    index += 1
    chars = []
    while index < len(sql):
        char = sql[index]
        if char == "\\":
            nxt = sql[index + 1]
            chars.append({"n": "\n", "r": "\r", "t": "\t", "0": "\0", "Z": "\x1a"}.get(nxt, nxt))
            index += 2
            continue
        if char == "'":
            if sql[index + 1] == "'":
                chars.append("'")
                index += 2
                continue
            return "".join(chars), index + 1
        chars.append(char)
        index += 1
    raise SystemExit("unterminated string")


def _number(raw: str):
    if re.fullmatch(r"-?\d+", raw):
        return int(raw)
    if re.fullmatch(r"-?\d+\.\d+", raw):
        return float(raw)
    return raw


def image_urls(raw) -> list[str]:
    if not raw:
        return []
    try:
        names = json.loads(raw)
    except json.JSONDecodeError:
        names = [raw]
    urls = []
    for name in names:
        if not isinstance(name, str) or not name.strip():
            continue
        file_name = name.strip().split("/")[-1]
        if file_name.startswith("http://") or file_name.startswith("https://"):
            urls.append(file_name)
        else:
            urls.append(f"{SITE}/assets/uploads/products/{file_name}")
    return urls


def logo_url(raw):
    if not raw:
        return None
    file_name = str(raw).strip().split("/")[-1]
    if file_name.startswith("http://") or file_name.startswith("https://"):
        return file_name
    return f"{SITE}/assets/uploads/vendors/{file_name}"


def main() -> None:
    sql = SQL_PATH.read_text(encoding="utf-8", errors="replace")
    vendors = parse_values(sql, "vendors")
    products = parse_values(sql, "products")
    sheets = parse_values(sql, "product_stock_sheets") if "INSERT INTO `product_stock_sheets`" in sql else []
    sheet_ids = {int(row["product_id"]) for row in sheets}
    messages = parse_values(sql, "messages") if "INSERT INTO `messages`" in sql else []
    admins = parse_values(sql, "admins") if "INSERT INTO `admins`" in sql else []
    invoices = parse_values(sql, "invoices") if "INSERT INTO `invoices`" in sql else []
    requirements = parse_values(sql, "vendor_requirements") if "INSERT INTO `vendor_requirements`" in sql else []
    retail_products = parse_values(sql, "retail_products") if "INSERT INTO `retail_products`" in sql else []
    retail_inquiries = parse_values(sql, "retail_inquiries") if "INSERT INTO `retail_inquiries`" in sql else []
    if len(vendors) < 50 or len(products) < 10 or len(admins) < 1:
        raise SystemExit(f"Dump looks too small to replace the test copy: vendors {len(vendors)}, products {len(products)}")

    if not firebase_admin._apps:
        firebase_admin.initialize_app(options={"projectId": PROJECT})
    client = firestore.client()
    batch = client.batch()
    pending = 0

    def commit_ready(force=False):
        nonlocal batch, pending
        if pending and (force or pending >= 400):
            batch.commit()
            batch = client.batch()
            pending = 0

    for vendor in vendors:
        public = {field: vendor.get(field) for field in VENDOR_FIELDS}
        public["logo_url"] = logo_url(vendor.get("logo"))
        public["password"] = vendor.get("password") or ""
        details = vendor.get("gst_details")
        if isinstance(details, str) and details.strip():
            try:
                parsed = json.loads(details)
            except json.JSONDecodeError:
                parsed = None
            public["gst_details"] = parsed if isinstance(parsed, dict) else None
        else:
            public["gst_details"] = None
        ref = client.collection("vendors").document(str(vendor["id"]))
        batch.set(ref, public)
        pending += 1
        commit_ready()

    for product in products:
        images = image_urls(product.get("images"))
        doc = {
            "id": int(product["id"]),
            "vendor_id": int(product["vendor_id"]),
            "product_name": product.get("product_name"),
            "category": product.get("category"),
            "description": product.get("description"),
            "images": images,
            "stock": product.get("stock"),
            "youtube_video": product.get("youtube_video"),
            "price": product.get("price"),
            "status": int(product.get("status") or 0),
            "has_stock_sheet": int(product["id"]) in sheet_ids,
            "created_at": product.get("created_at"),
            "updated_at": product.get("updated_at"),
        }
        ref = client.collection("products").document(str(product["id"]))
        batch.set(ref, doc)
        pending += 1
        commit_ready()

    max_message_id = 0
    for message in messages:
        message_id = int(message["id"])
        max_message_id = max(max_message_id, message_id)
        ref = client.collection("messages").document(str(message_id))
        batch.set(
            ref,
            {
                "id": message_id,
                "sender_id": int(message["sender_id"]),
                "receiver_id": int(message["receiver_id"]),
                "message": message.get("message") or "",
                "is_read": int(message.get("is_read") or 0),
                "created_at": message.get("created_at"),
                "updated_at": message.get("updated_at"),
            },
        )
        pending += 1
        commit_ready()

    for admin in admins:
        ref = client.collection("admins").document(str(admin["id"]))
        batch.set(
            ref,
            {
                "id": int(admin["id"]),
                "username": admin.get("username") or "",
                "pwd": admin.get("pwd") or "",
            },
        )
        pending += 1
        commit_ready()

    for invoice in invoices:
        ref = client.collection("invoices").document(str(invoice["id"]))
        batch.set(ref, invoice)
        pending += 1
        commit_ready()

    for requirement in requirements:
        ref = client.collection("requirements").document(str(requirement["id"]))
        batch.set(ref, requirement)
        pending += 1
        commit_ready()

    for product in retail_products:
        ref = client.collection("retail_products").document(str(product["id"]))
        batch.set(ref, product)
        pending += 1
        commit_ready()

    max_retail_inquiry = 0
    for inquiry in retail_inquiries:
        inquiry_id = int(inquiry["id"])
        max_retail_inquiry = max(max_retail_inquiry, inquiry_id)
        ref = client.collection("retail_inquiries").document(str(inquiry_id))
        batch.set(ref, inquiry)
        pending += 1
        commit_ready()

    commit_ready(force=True)

    def drop_missing(collection: str, keep: set[int]) -> int:
        removed = 0
        for doc in client.collection(collection).stream():
            data = doc.to_dict() or {}
            try:
                doc_id = int(data.get("id") or doc.id)
            except (TypeError, ValueError):
                continue
            if doc_id not in keep:
                doc.reference.delete()
                removed += 1
        return removed

    removed = {
        "vendors": drop_missing("vendors", {int(row["id"]) for row in vendors}),
        "products": drop_missing("products", {int(row["id"]) for row in products}),
        "messages": drop_missing("messages", {int(row["id"]) for row in messages}),
        "invoices": drop_missing("invoices", {int(row["id"]) for row in invoices}),
        "requirements": drop_missing("requirements", {int(row["id"]) for row in requirements}),
        "retail_products": drop_missing("retail_products", {int(row["id"]) for row in retail_products}),
        "retail_inquiries": drop_missing("retail_inquiries", {int(row["id"]) for row in retail_inquiries}),
        "admins": drop_missing("admins", {int(row["id"]) for row in admins}),
    }
    max_vendor_id = max((int(row["id"]) for row in vendors), default=0)
    max_product_id = max((int(row["id"]) for row in products), default=0)
    max_invoice_id = max((int(row["id"]) for row in invoices), default=0)
    max_requirement_id = max((int(row["id"]) for row in requirements), default=0)
    announcements = parse_values(sql, "app_announcements") if "INSERT INTO `app_announcements`" in sql else []
    latest_announcement = ""
    if announcements:
        latest = max(announcements, key=lambda row: int(row.get("id") or 0))
        latest_announcement = latest.get("message") or ""
    client.collection("settings").document("announcement").set({"text": latest_announcement}, merge=True)
    client.collection("settings").document("counters").set(
        {
            "messages": max_message_id,
            "retail_inquiries": max_retail_inquiry,
            "vendors": max_vendor_id,
            "products": max_product_id,
            "invoices": max_invoice_id,
            "requirements": max_requirement_id,
        },
        merge=True,
    )
    print(f"vendors {len(vendors)}")
    print(f"admins {len(admins)}")
    print(f"products {len(products)}")
    print(f"stock_sheets {len(sheet_ids)}")
    print(f"messages {len(messages)}")
    print(f"invoices {len(invoices)}")
    print(f"requirements {len(requirements)}")
    print(f"retail_products {len(retail_products)}")
    print(f"retail_inquiries {len(retail_inquiries)}")
    print(f"removed {removed}")
    print(f"announcement_chars {len(latest_announcement)}")


if __name__ == "__main__":
    sys.exit(main())
