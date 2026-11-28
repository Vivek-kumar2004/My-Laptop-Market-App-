import secrets
from datetime import date, timedelta

import bcrypt
from firebase_admin import firestore

from accounts import _next_vendor_id, _taken, hold_vendor_keys, release_vendor_keys, replace_vendor_keys
from auth import _file_url, _password_matches


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _token(req) -> str:
    header = req.headers.get("Authorization") or ""
    if header.lower().startswith("bearer "):
        return header[7:].strip()
    extra = req.headers.get("X-Admin-Token") or req.args.get("token") or ""
    return _text(extra)


def current_admin(req):
    token = _token(req)
    if token == "":
        return None
    doc = firestore.client().collection("admin_tokens").document(token).get()
    if not doc.exists:
        return None
    return doc.to_dict() or {}


def login(username: str, password: str) -> tuple[dict, int]:
    username = _text(username)
    password = password or ""
    if username == "" or password == "":
        return {"success": False, "message": "Username and password are required."}, 422
    matches = firestore.client().collection("admins").where("username", "==", username).limit(1).stream()
    admin = None
    for doc in matches:
        admin = doc.to_dict() or {}
        break
    if admin is None or not _password_matches(password, _text(admin.get("pwd"))):
        return {"success": False, "message": "Invalid username or password."}, 200
    token = secrets.token_urlsafe(32)
    firestore.client().collection("admin_tokens").document(token).set(
        {"admin_id": int(admin.get("id") or 0), "username": admin.get("username") or username}
    )
    return {
        "success": True,
        "token": token,
        "admin": {"id": int(admin.get("id") or 0), "username": admin.get("username") or username},
    }, 200


def _guard(req):
    admin = current_admin(req)
    if admin is None:
        return None, ({"success": False, "message": "Admin login required."}, 401)
    return admin, None


def _count(name: str, status: str | None = None) -> int:
    total = 0
    for doc in firestore.client().collection(name).stream():
        if status is None or _text((doc.to_dict() or {}).get("status")) == status:
            total += 1
    return total


def dashboard() -> dict:
    client = firestore.client()
    active = inactive = 0
    for doc in client.collection("vendors").stream():
        if int((doc.to_dict() or {}).get("status") or 0) == 1:
            active += 1
        else:
            inactive += 1
    products = sum(1 for _ in client.collection("products").stream())
    return {
        "success": True,
        "data": {
            "active_vendors": active,
            "inactive_vendors": inactive,
            "total_vendors": active + inactive,
            "new_inquiries": _count("inquiries", "new"),
            "total_inquiries": _count("inquiries"),
            "total_invoices": _count("invoices"),
            "total_products": products,
            "active_requirements": _count("requirements", "active"),
            "total_requirements": _count("requirements"),
        },
    }


def _vendor_row(vendor: dict) -> dict:
    logo = vendor.get("logo_url") or _file_url(vendor.get("logo"))
    return {
        "id": int(vendor.get("id") or 0),
        "username": _text(vendor.get("username")),
        "shop_name": vendor.get("shop_name"),
        "mobile": vendor.get("mobile"),
        "location": vendor.get("location"),
        "status": int(vendor.get("status") or 0),
        "subscription_start": vendor.get("subscription_start"),
        "subscription_end": vendor.get("subscription_end"),
        "gstin": vendor.get("gstin") or None,
        "state": vendor.get("state") or None,
        "state_code": vendor.get("state_code"),
        "billing_address": vendor.get("billing_address"),
        "contact_person": vendor.get("contact_person"),
        "logo_url": logo,
        "admin_warning": _text(vendor.get("admin_warning")),
        "password_plain": _text(vendor.get("password_plain")) or None,
    }


def vendors(filter_name: str) -> dict:
    rows = []
    wanted = _text(filter_name).lower() or "all"
    for doc in firestore.client().collection("vendors").stream():
        vendor = doc.to_dict() or {}
        status = int(vendor.get("status") or 0)
        if wanted == "active" and status != 1:
            continue
        if wanted == "inactive" and status != 0:
            continue
        rows.append(_vendor_row(vendor))
    rows.sort(key=lambda row: row["id"], reverse=True)
    return {"success": True, "data": rows}


def _load(vendor_id: int):
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}
    data["_ref"] = doc.reference
    return data


def save_announcement(req) -> tuple[dict, int]:
    message = _text(_request_body(req).get("message"))
    if len(message) > 500:
        return {"success": False, "message": "Notification must be 500 characters or less."}, 422
    firestore.client().collection("settings").document("announcement").set({"text": message}, merge=True)
    if message != "":
        preview = message[:117] + "..." if len(message) > 120 else message
        from push_apns import notify_active

        notify_active("My Laptop Market", preview, "announcement")
    return {
        "success": True,
        "message": "Notification removed." if message == "" else "Notification sent to all vendors.",
        "data": {"text": message},
    }, 200


def warn_vendor(req, vendor_id: int) -> tuple[dict, int]:
    vendor = _load(vendor_id)
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}, 404
    message = _text(_request_body(req).get("message"))
    if len(message) > 500:
        return {"success": False, "message": "Warning must be 500 characters or less."}, 422
    vendor.pop("_ref").update({"admin_warning": message})
    shop = vendor.get("shop_name") or vendor.get("username")
    if message != "":
        from push_apns import notify

        try:
            notify(
                [vendor_id],
                "Warning from My Laptop Market",
                message,
                "warning",
                {"target_id": str(vendor_id), "target_name": shop},
            )
        except Exception:
            pass
    return {
        "success": True,
        "message": f"Warning removed for {shop}." if message == "" else f"Warning sent to {shop}.",
    }, 200


def vendor_show(vendor_id: int) -> tuple[dict, int]:
    vendor = _load(vendor_id)
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}, 404
    return {"success": True, "data": _vendor_row(vendor)}, 200


def _request_body(req) -> dict:
    body = req.get_json(silent=True)
    if isinstance(body, dict):
        return body
    if req.form:
        return req.form.to_dict()
    return {}


def _vendor_input(body: dict, creating: bool) -> tuple[dict, str]:
    shop = _text(body.get("shop_name"))
    username = _text(body.get("username"))
    mobile = _text(body.get("mobile"))
    password = body.get("password")
    password = "" if password is None else str(password)
    if shop == "" or len(shop) > 200:
        return {}, "The shop name field is required."
    if username == "" or len(username) > 100:
        return {}, "The username field is required."
    if mobile == "" or len(mobile) > 15:
        return {}, "The mobile field is required."
    if creating and password.strip() == "":
        return {}, "The password field is required."
    if password.strip() != "" and len(password) < 4:
        return {}, "The password must be at least 4 characters."
    fields = {"shop_name": shop, "username": username, "mobile": mobile}
    if password.strip() != "":
        fields["password"] = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
        fields["password_plain"] = password
    for key in ("location", "gstin", "state", "state_code", "billing_address", "contact_person"):
        if key in body:
            value = body.get(key)
            fields[key] = None if value is None or _text(value) == "" else _text(value)
    return fields, ""


def vendor_store(req) -> tuple[dict, int]:
    fields, error = _vendor_input(_request_body(req), creating=True)
    if error:
        return {"success": False, "message": error}, 422
    if _taken("username", fields["username"]):
        return {"success": False, "message": "The username has already been taken."}, 422
    if _taken("mobile", fields["mobile"]):
        return {"success": False, "message": "The mobile has already been taken."}, 422
    if _taken("shop_name", fields["shop_name"]):
        return {"success": False, "message": "This shop name is already registered."}, 422
    vendor_id = _next_vendor_id()
    held = hold_vendor_keys(vendor_id, fields["username"], fields["mobile"], "", fields["shop_name"])
    if held:
        return {"success": False, "message": held}, 422
    vendor = {
        "id": vendor_id,
        "username": fields.pop("username"),
        "password": fields.pop("password"),
        "shop_name": fields.pop("shop_name"),
        "mobile": fields.pop("mobile"),
        "status": 1,
        "logo": None,
        "logo_url": None,
        "email": None,
        "subscription_start": None,
        "subscription_end": None,
        "gst_verified": 0,
        "gst_details": None,
        "shop_photo": None,
        "admin_warning": "",
        "location": None,
        "gstin": None,
        "state": None,
        "state_code": None,
        "billing_address": None,
        "contact_person": None,
    }
    vendor.update(fields)
    firestore.client().collection("vendors").document(str(vendor_id)).set(vendor)
    return {
        "success": True,
        "message": "Vendor added successfully",
        "data": _vendor_row(vendor),
    }, 200


def vendor_update(req, vendor_id: int) -> tuple[dict, int]:
    vendor = _load(vendor_id)
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}, 404
    fields, error = _vendor_input(_request_body(req), creating=False)
    if error:
        return {"success": False, "message": error}, 422
    if _taken("username", fields["username"], vendor_id):
        return {"success": False, "message": "The username has already been taken."}, 422
    if _taken("mobile", fields["mobile"], vendor_id):
        return {"success": False, "message": "The mobile has already been taken."}, 422
    if _taken("shop_name", fields["shop_name"], vendor_id):
        return {"success": False, "message": "This shop name is already registered."}, 422
    held = replace_vendor_keys(
        vendor_id,
        vendor,
        fields["username"],
        fields["mobile"],
        vendor.get("email"),
        fields["shop_name"],
    )
    if held:
        return {"success": False, "message": held}, 422
    vendor.pop("_ref").update(fields)
    vendor.update(fields)
    return {
        "success": True,
        "message": "Vendor updated successfully",
        "data": _vendor_row(vendor),
    }, 200


def _set_products(vendor_id: int, status: int) -> None:
    client = firestore.client()
    for doc in client.collection("products").where("vendor_id", "==", vendor_id).stream():
        doc.reference.update({"status": status})


def activate(vendor_id: int) -> tuple[dict, int]:
    vendor = _load(vendor_id)
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}, 404
    today = date.today()
    end = _text(vendor.get("subscription_end"))[:10]
    expired = True
    if end:
        try:
            expired = date.fromisoformat(end) < today
        except ValueError:
            expired = True
    updates = {"status": 1}
    if expired:
        updates["subscription_start"] = today.isoformat()
        updates["subscription_end"] = (today + timedelta(days=30)).isoformat()
    vendor["_ref"].update(updates)
    vendor.update(updates)
    _set_products(vendor_id, 1)
    return {
        "success": True,
        "message": "Vendor activated. Subscription: " + _text(vendor.get("subscription_end")),
        "data": _vendor_row(vendor),
    }, 200


def deactivate(vendor_id: int) -> tuple[dict, int]:
    vendor = _load(vendor_id)
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}, 404
    vendor["_ref"].update({"status": 0})
    vendor["status"] = 0
    _set_products(vendor_id, 0)
    return {"success": True, "message": "Vendor deactivated successfully", "data": _vendor_row(vendor)}, 200


def delete_vendor(vendor_id: int) -> tuple[dict, int]:
    vendor = _load(vendor_id)
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}, 404
    client = firestore.client()
    from products_write import _delete_sheet_blob, _delete_stored_image, _images_of

    for doc in client.collection("products").where("vendor_id", "==", vendor_id).stream():
        product = doc.to_dict() or {}
        for image in _images_of(product):
            _delete_stored_image(image)
        _delete_stored_image(_text(product.get("front_image")))
        _delete_sheet_blob(int(product.get("id") or 0))
        doc.reference.delete()
    for field in ("sender_id", "receiver_id"):
        for doc in client.collection("messages").where(field, "==", vendor_id).stream():
            doc.reference.delete()
    _delete_stored_image(_text(vendor.get("logo")))
    _delete_stored_image(_text(vendor.get("shop_photo")))
    from password_reset import clear as clear_password_otp

    clear_password_otp(vendor_id)
    release_vendor_keys(vendor)
    label = vendor.get("shop_name") or vendor.get("username")
    vendor["_ref"].delete()
    return {"success": True, "message": f'Vendor "{label}" and their products were deleted.'}, 200


def _when(value, with_time: bool = False):
    from datetime import datetime

    text = _text(value).replace("T", " ")[:19]
    if text == "":
        return None
    parsed = None
    for size, pattern in ((19, "%Y-%m-%d %H:%M:%S"), (10, "%Y-%m-%d")):
        try:
            parsed = datetime.strptime(text[:size], pattern)
            break
        except ValueError:
            continue
    if parsed is None:
        return text
    if with_time:
        return parsed.strftime("%d %b %Y, %I:%M %p")
    return parsed.strftime("%d %b %Y")


def _iso(value) -> str:
    text = _text(value).replace("T", " ")[:19]
    if text == "":
        return ""
    return text.replace(" ", "T") + "+00:00"


def _inquiry_row(row: dict) -> dict:
    return {
        "id": int(row.get("id") or 0),
        "name": _text(row.get("name")),
        "email": _text(row.get("email")),
        "subject": _text(row.get("subject")),
        "message": _text(row.get("message")),
        "status": _text(row.get("status")) or "read",
        "created_at": _when(row.get("created_at"), True),
    }


def inquiries() -> dict:
    rows = []
    new_count = 0
    for doc in firestore.client().collection("inquiries").stream():
        row = doc.to_dict() or {}
        if _text(row.get("status")) == "new":
            new_count += 1
        rows.append(_inquiry_row(row))
    rows.sort(key=lambda row: row["id"], reverse=True)
    return {"success": True, "new_count": new_count, "data": rows}


def inquiry_show(inquiry_id: int) -> tuple[dict, int]:
    ref = firestore.client().collection("inquiries").document(str(inquiry_id))
    doc = ref.get()
    if not doc.exists:
        return {"success": False, "message": "Inquiry not found"}, 404
    row = doc.to_dict() or {}
    if _text(row.get("status")) == "new":
        ref.update({"status": "read"})
        row["status"] = "read"
    return {"success": True, "data": _inquiry_row(row)}, 200


def inquiry_delete(inquiry_id: int) -> tuple[dict, int]:
    ref = firestore.client().collection("inquiries").document(str(inquiry_id))
    if not ref.get().exists:
        return {"success": False, "message": "Inquiry not found"}, 404
    ref.delete()
    return {"success": True, "message": "Inquiry deleted successfully."}, 200


def _form_field(req, name: str) -> str:
    if req.form and name in req.form:
        return _text(req.form.get(name))
    body = req.get_json(silent=True) or {}
    if name in body and body.get(name) is not None:
        return _text(body.get(name))
    return ""


def _next_inquiry_id() -> int:
    client = firestore.client()
    ref = client.collection("settings").document("counters")
    transaction = client.transaction()

    @firestore.transactional
    def allocate(transaction):
        snap = ref.get(transaction=transaction)
        current = int((snap.to_dict() or {}).get("inquiries") or 0)
        if current <= 0:
            for doc in client.collection("inquiries").stream():
                current = max(current, int((doc.to_dict() or {}).get("id") or 0))
        nxt = current + 1
        transaction.set(ref, {"inquiries": nxt}, merge=True)
        return nxt

    return allocate(transaction)


def create_inquiry(req) -> tuple[dict, int]:
    import re
    from datetime import datetime, timezone

    name = _form_field(req, "name")
    email = _form_field(req, "email")
    subject = _form_field(req, "subject")
    message = _form_field(req, "message")
    if name == "" or email == "" or subject == "" or message == "":
        return {"status": False, "message": "Please fill all fields."}, 422
    if len(name) > 200 or len(email) > 200 or len(subject) > 255 or len(message) > 5000:
        return {"status": False, "message": "Please check the form and try again."}, 422
    if not re.fullmatch(r"[^@\s]+@[^@\s]+\.[^@\s]+", email):
        return {"status": False, "message": "Please enter a valid email address."}, 422
    now = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    inquiry_id = _next_inquiry_id()
    firestore.client().collection("inquiries").document(str(inquiry_id)).set(
        {
            "id": inquiry_id,
            "name": name,
            "email": email,
            "subject": subject,
            "message": message,
            "status": "new",
            "created_at": now,
            "updated_at": now,
        }
    )
    return {
        "status": True,
        "message": "Your business inquiry has been submitted successfully. We will contact you soon.",
        "data": {"id": inquiry_id},
    }, 201


def _vendor_by_id(vendor_id: int) -> dict:
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    return doc.to_dict() or {} if doc.exists else {}


def _invoice_row(row: dict) -> dict:
    vendor = _vendor_by_id(int(row.get("vendor_id") or 0))
    def money(key):
        value = row.get(key)
        if value is None or value == "":
            return None
        return float(value)

    return {
        "id": int(row.get("id") or 0),
        "invoice_number": row.get("invoice_number"),
        "vendor_id": int(row.get("vendor_id") or 0),
        "vendor_name": vendor.get("shop_name") or vendor.get("username") or "Vendor",
        "vendor_mobile": vendor.get("mobile"),
        "vendor_gstin": vendor.get("gstin") or None,
        "amount": float(row.get("amount") or 0),
        "subtotal": money("subtotal"),
        "cgst": money("cgst"),
        "sgst": money("sgst"),
        "igst": money("igst"),
        "razorpay_payment_id": row.get("razorpay_payment_id"),
        "status": row.get("status"),
        "created_at": _when(row.get("created_at")),
    }


def invoices() -> dict:
    rows = [_invoice_row(doc.to_dict() or {}) for doc in firestore.client().collection("invoices").stream()]
    rows.sort(key=lambda row: row["id"], reverse=True)
    return {"success": True, "data": rows}


def invoice_show(invoice_id: int) -> tuple[dict, int]:
    doc = firestore.client().collection("invoices").document(str(invoice_id)).get()
    if not doc.exists:
        return {"success": False, "message": "Invoice not found"}, 404
    return {"success": True, "data": _invoice_row(doc.to_dict() or {})}, 200


def _money(value) -> float:
    if value is None or value == "":
        return 0.0
    return float(value)


def _rupees_in_words(amount: float) -> str:
    ones = ["", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"]
    tens = ["", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"]

    def convert(number: int) -> str:
        if number == 0:
            return "Zero"
        out = ""
        crore, number = divmod(number, 10000000)
        lakh, number = divmod(number, 100000)
        thousand, number = divmod(number, 1000)
        hundred, number = divmod(number, 100)
        if crore:
            out += convert(crore) + " Crore "
        if lakh:
            out += convert(lakh) + " Lakh "
        if thousand:
            out += convert(thousand) + " Thousand "
        if hundred:
            out += ones[hundred] + " Hundred "
        if number:
            if number < 20:
                out += ones[number]
            else:
                out += tens[number // 10]
                if number % 10:
                    out += " " + ones[number % 10]
        return " ".join(out.split())

    rounded = round(float(amount), 2)
    rupees = int(rounded)
    paise = int(round((rounded - rupees) * 100))
    words = "Indian Rupee " + convert(rupees)
    if paise:
        words += " and " + convert(paise) + " Paise"
    return words + " Only"


def invoice_html(invoice_id: int):
    import html
    from datetime import datetime
    from flask import Response

    doc = firestore.client().collection("invoices").document(str(invoice_id)).get()
    if not doc.exists:
        return Response("Invoice not found", status=404)
    row = doc.to_dict() or {}
    vendor = _vendor_by_id(int(row.get("vendor_id") or 0))
    amount = _money(row.get("amount"))
    percent = _money(row.get("tax_percent")) or 18
    subtotal = _money(row.get("subtotal")) or round(amount / (1 + percent / 100), 2)
    cgst = _money(row.get("cgst"))
    sgst = _money(row.get("sgst"))
    igst = _money(row.get("igst"))
    if cgst + sgst + igst == 0:
        igst = round(amount - subtotal, 2)
    rate = _money(row.get("rate")) or subtotal
    qty = _money(row.get("qty")) or 1
    shop = vendor.get("shop_name") or vendor.get("username") or "Vendor"
    address = _text(vendor.get("billing_address")) or _text(vendor.get("location")) or "N/A"
    state = _text(vendor.get("state"))
    state_code = _text(vendor.get("state_code"))
    place = _text(row.get("place_of_supply")) or (f"{state} ({state_code})" if state and state_code else state or "Delhi")
    created = _text(row.get("created_at")).replace("T", " ")[:10]
    try:
        invoice_date = datetime.strptime(created, "%Y-%m-%d").strftime("%d/%m/%Y") if created else ""
    except ValueError:
        invoice_date = created
    due = _text(row.get("due_date"))[:10]
    try:
        due_date = datetime.strptime(due, "%Y-%m-%d").strftime("%d/%m/%Y") if due else invoice_date
    except ValueError:
        due_date = invoice_date
    gstin = _text(vendor.get("gstin")) or "Unregistered"
    payment = _text(row.get("razorpay_payment_id"))
    tax_cells = (
        f"<td class='right'>{igst:.2f}<br>{percent:.0f}%</td>"
        if igst > 0
        else f"<td class='right'>{cgst:.2f}<br>{percent / 2:.1f}%</td><td class='right'>{sgst:.2f}<br>{percent / 2:.1f}%</td>"
    )
    tax_head = "<th class='right'>IGST</th>" if igst > 0 else "<th class='right'>CGST</th><th class='right'>SGST</th>"
    tax_rows = (
        f"<tr><td>IGST ({percent:.0f}%)</td><td class='right'>{igst:.2f}</td></tr>"
        if igst > 0
        else f"<tr><td>CGST ({percent / 2:.1f}%)</td><td class='right'>{cgst:.2f}</td></tr>"
        f"<tr><td>SGST ({percent / 2:.1f}%)</td><td class='right'>{sgst:.2f}</td></tr>"
    )
    e = html.escape
    page = f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Tax Invoice {e(_text(row.get("invoice_number")))}</title>
<style>
*{{box-sizing:border-box}} body{{margin:0;background:#e9ecef;font-family:Arial,Helvetica,sans-serif;color:#222;font-size:13px}}
.wrap{{max-width:900px;margin:24px auto}} .toolbar{{text-align:right;margin-bottom:12px}}
.toolbar button{{padding:8px 14px;border:0;border-radius:4px;cursor:pointer;font-size:13px}}
.btn-print{{background:#0d6efd;color:#fff}} .btn-close{{background:#6c757d;color:#fff;margin-left:6px}}
.sheet{{background:#fff;border:1px solid #ccc;padding:28px 32px}}
.top{{display:flex;justify-content:space-between;gap:20px;border-bottom:2px solid #1a73e8;padding-bottom:14px}}
.seller h2{{margin:0 0 6px;font-size:20px;color:#1a73e8}} .muted{{color:#555;line-height:1.45;margin:0}}
.title-block{{text-align:right}} .title-block h1{{margin:0;font-size:26px;letter-spacing:1px;color:#444}}
.two{{display:flex;gap:16px;margin:16px 0}} .box{{flex:1;border:1px solid #ccc;padding:10px 12px;min-height:120px}}
.box h4{{margin:0 0 8px;font-size:12px;color:#1a73e8;text-transform:uppercase}}
table{{width:100%;border-collapse:collapse}} th,td{{border:1px solid #ccc;padding:8px;vertical-align:top}}
th{{background:#f3f6fb;font-size:11px;text-transform:uppercase}} .right{{text-align:right}} .center{{text-align:center}}
.totals-row{{display:flex;gap:16px;margin-top:14px}} .words{{flex:1;font-size:12px}} .totals{{width:280px}}
.totals td{{border:0;padding:5px 0}} .grand td{{font-size:15px;font-weight:bold;border-top:2px solid #1a73e8}}
.sign{{margin-top:28px;text-align:right}} .line{{margin-top:48px;font-size:12px}}
.foot{{margin-top:16px;text-align:center;color:#777;font-size:11px}}
@media print {{body{{background:#fff}} .toolbar{{display:none}} .wrap{{margin:0;max-width:none}} .sheet{{border:0;padding:0}}}}
</style></head><body>
<div class="wrap"><div class="toolbar">
<button class="btn-print" type="button" onclick="window.print()">Print / Save as PDF</button>
<button class="btn-close" type="button" onclick="window.close()">Close</button></div>
<div class="sheet"><div class="top"><div class="seller">
<img src="https://storage.googleapis.com/mylaptopmarket-app-web/images/mylaptoplogo.png" alt="logo" style="max-height:52px">
<h2>A.S. TRADERS</h2><p class="muted" style="font-weight:bold">My Laptop Market</p>
<p class="muted">SHOP NO. 05, PLOT NO. 19, VEER SAVARKAR BLOCK<br>SHAKARPUR<br>EAST DELHI, Delhi - 110092<br>India<br>Phone: 8595864379<br>support@mylaptopmarket.com</p>
<p><strong>GSTIN:</strong> 07DIBPM1446L1Z5<br><strong>MSME/Udyam No:</strong> UDYAM-DL-07-0008370</p>
</div><div class="title-block"><h1>TAX INVOICE</h1><p><strong>Invoice#</strong> {e(_text(row.get("invoice_number")) or str(invoice_id))}</p></div></div>
<div class="two"><div class="box"><h4>Bill To :</h4><p><strong>{e(shop)}</strong><br>{e(address)}<br>{e(state)} {e('(' + state_code + ')') if state_code else ''} India<br>{e(_text(vendor.get('contact_person')))}<br>Phone: {e(_text(vendor.get('mobile')) or 'N/A')}<br><strong>GSTIN</strong> {e(gstin)}</p></div>
<div class="box"><h4>Ship To :</h4><p><strong>{e(shop)}</strong><br>{e(address)}<br>{e(state)} India<br><strong>GSTIN</strong> {e(gstin)}</p></div></div>
<p><strong>Place Of Supply:</strong> {e(place)}</p>
<table><tr><th>Invoice Date</th><th>Terms</th><th>Due Date</th><th>P.O.#</th><th>E-Way Bill#</th></tr>
<tr><td>{e(invoice_date)}</td><td>{e(_text(row.get('terms')) or 'Paid')}</td><td>{e(due_date)}</td><td>{e(_text(row.get('po_number')) or '—')}</td><td>{e(_text(row.get('eway_bill')) or '—')}</td></tr></table>
<table style="margin-top:14px"><tr><th class="center">#</th><th>Item &amp; Description</th><th class="center">HSN/SAC</th><th class="center">Nos.</th><th class="right">Rate</th>{tax_head}<th class="right">Amount</th></tr>
<tr><td class="center">1</td><td><strong>Vendor Subscription — 30 Days</strong><br><span class="muted">Premium seller listing on MyLaptopMarket marketplace</span>{('<br><span class="muted">Payment ID: ' + e(payment) + '</span>') if payment else ''}</td>
<td class="center">{e(_text(row.get('hsn_sac')) or '998314')}</td><td class="center">{qty:.2f}<br>Nos.</td><td class="right">{rate:.2f}</td>{tax_cells}<td class="right">{subtotal:.2f}</td></tr></table>
<div class="totals-row"><div class="words"><strong>Total In Words:</strong><br>{e(_rupees_in_words(amount))}</div>
<div class="totals"><table><tr><td>Sub Total</td><td class="right">{subtotal:.2f}</td></tr>{tax_rows}
<tr class="grand"><td>Total</td><td class="right">{amount:.2f}</td></tr>
<tr><td>Balance Due</td><td class="right">0.00</td></tr>
<tr><td>Items in Total</td><td class="right">{qty:.2f}</td></tr></table></div></div>
<div class="sign"><div>For A.S. TRADERS</div><div class="line">A.S. TRADERS</div></div>
<p class="foot">This is a computer generated invoice and requires no signature.</p>
</div></div></body></html>"""
    return Response(page, mimetype="text/html")


def _requirement_row(row: dict) -> dict:
    vendor = _vendor_by_id(int(row.get("vendor_id") or 0))
    title = _text(row.get("canonical_title")) or _text(row.get("title"))
    return {
        "id": int(row.get("id") or 0),
        "vendor_id": int(row.get("vendor_id") or 0),
        "vendor_name": vendor.get("shop_name") or vendor.get("username") or f"Vendor #{row.get('vendor_id')}",
        "vendor_username": vendor.get("username"),
        "vendor_mobile": vendor.get("mobile"),
        "vendor_logo_url": vendor.get("logo_url") or _file_url(vendor.get("logo")),
        "title": _text(row.get("title")),
        "canonical_title": title,
        "category": row.get("category"),
        "quantity": int(row["quantity"]) if row.get("quantity") not in (None, "") else None,
        "description": row.get("description"),
        "status": _text(row.get("status")) or "active",
        "created_at": _when(row.get("created_at"), True),
        "updated_at": _when(row.get("updated_at"), True),
    }


def requirements(filter_name: str) -> dict:
    from cluster import group_key

    wanted = _text(filter_name).lower() or "all"
    groups = {}
    for doc in firestore.client().collection("requirements").stream():
        row = doc.to_dict() or {}
        status = _text(row.get("status")) or "active"
        if wanted in {"active", "closed"} and status != wanted:
            continue
        key, display = group_key(row)
        vendor = _vendor_by_id(int(row.get("vendor_id") or 0))
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
                "count": 0,
                "active_count": 0,
                "latest_at": "",
                "vendors": [],
                "vendor_ids": set(),
            },
        )
        vendor_id = int(row.get("vendor_id") or 0)
        card = {
            "id": vendor_id,
            "shop_name": vendor.get("shop_name") or vendor.get("username") or f"Vendor #{vendor_id}",
            "username": vendor.get("username"),
            "mobile": vendor.get("mobile"),
            "logo_url": vendor.get("logo_url") or _file_url(vendor.get("logo")),
            "requirement_id": int(row.get("id") or 0),
            "title": _text(row.get("title")),
            "canonical_title": title,
            "quantity": int(row["quantity"]) if row.get("quantity") not in (None, "") else None,
            "description": row.get("description"),
            "status": status,
            "created_at": _iso(row.get("created_at")),
            "updated_at": _iso(row.get("updated_at")),
        }
        if vendor_id not in group["vendor_ids"]:
            group["vendor_ids"].add(vendor_id)
            group["vendors"].append(card)
            group["count"] += 1
        if status == "active":
            group["active_count"] += 1
        if row.get("quantity") not in (None, ""):
            group["quantity"] += int(row["quantity"])
        stamp = _iso(row.get("updated_at"))
        if stamp >= group["latest_at"]:
            group["latest_at"] = stamp
            group["title"] = title
            group["canonical_title"] = title
            group["category"] = row.get("category")
    feed = []
    for group in groups.values():
        group.pop("vendor_ids", None)
        feed.append(group)
    feed.sort(key=lambda row: row.get("latest_at") or "", reverse=True)
    return {"success": True, "data": feed}


def requirement_show(requirement_id: int) -> tuple[dict, int]:
    doc = firestore.client().collection("requirements").document(str(requirement_id)).get()
    if not doc.exists:
        return {"success": False, "message": "Requirement not found"}, 404
    return {"success": True, "data": _requirement_row(doc.to_dict() or {})}, 200


def requirement_close(requirement_id: int) -> tuple[dict, int]:
    ref = firestore.client().collection("requirements").document(str(requirement_id))
    doc = ref.get()
    if not doc.exists:
        return {"success": False, "message": "Requirement not found"}, 404
    ref.update({"status": "closed"})
    row = doc.to_dict() or {}
    row["status"] = "closed"
    return {"success": True, "message": "Requirement closed", "data": _requirement_row(row)}, 200


def admin_products() -> dict:
    vendors = {doc.id: doc.to_dict() or {} for doc in firestore.client().collection("vendors").stream()}
    rows = []
    for doc in firestore.client().collection("products").stream():
        product = doc.to_dict() or {}
        vendor = vendors.get(str(product.get("vendor_id"))) or {}
        images = product.get("images")
        main = _text(product.get("front_image")) or None
        if not main and isinstance(images, list) and images:
            main = images[0]
        elif not main and isinstance(images, str) and images:
            main = images
        try:
            price = float(product.get("price") or 0)
        except (TypeError, ValueError):
            price = 0
        rows.append(
            {
                "id": int(product.get("id") or 0),
                "product_name": _text(product.get("product_name")),
                "category": product.get("category"),
                "price": price,
                "stock": int(product.get("stock") or 0),
                "status": int(product.get("status") or 0),
                "vendor_id": int(product.get("vendor_id") or 0),
                "vendor_name": vendor.get("shop_name") or "Vendor",
                "main_image": main,
            }
        )
    rows.sort(key=lambda row: row["id"], reverse=True)
    return {"success": True, "data": rows}


def admin_delete_product(product_id: int) -> tuple[dict, int]:
    from products_write import _delete_sheet_blob, _delete_stored_image, _images_of, _product

    product = _product(product_id)
    if product is None:
        return {"success": False, "message": "Product not found"}, 404
    name = _text(product.get("product_name")) or f"Product #{product_id}"
    for image in _images_of(product):
        _delete_stored_image(image)
    _delete_stored_image(_text(product.get("front_image")))
    _delete_sheet_blob(product_id)
    product["_ref"].delete()
    return {"success": True, "message": f'Product "{name}" was deleted.'}, 200
