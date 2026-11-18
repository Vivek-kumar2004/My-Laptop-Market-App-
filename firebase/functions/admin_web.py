from __future__ import annotations

import html
import secrets
import urllib.parse
from datetime import date

from firebase_admin import firestore
from flask import Response

from admin_api import (
    activate,
    dashboard,
    deactivate,
    delete_vendor,
    inquiries,
    inquiry_delete,
    inquiry_show,
    invoice_html,
    invoices,
    vendor_show,
    vendor_store,
    vendor_update,
    vendors,
)
from auth import _password_matches
from messages import admin_thread, admin_threads
from web_session import _page

COOKIE = "mlm_admin"


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _redirect(location: str) -> Response:
    response = Response("", status=302)
    response.headers["Location"] = location
    return response


def admin_session(req) -> dict | None:
    token = _text(req.cookies.get(COOKIE) if req.cookies else "")
    if token == "":
        return None
    doc = firestore.client().collection("admin_tokens").document(token).get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}
    if not data.get("admin_id"):
        return None
    data["_token"] = token
    return data


def _find(username: str) -> dict | None:
    matches = firestore.client().collection("admins").where("username", "==", username).limit(1).stream()
    for doc in matches:
        data = doc.to_dict() or {}
        data["_ref"] = doc.reference
        return data
    return None


def _login_form(error: str = "") -> str:
    alert = f'<div class="error">{html.escape(error)}</div>' if error else ""
    return f"""
<h2>Admin Login</h2>
{alert}
<form action="loginAuth" method="post">
<label>Username</label>
<input type="text" name="username" required>
<label>Password</label>
<input type="password" name="password" required>
<button type="submit">Login</button>
</form>
"""


def login_page(error: str = "") -> Response:
    return _page("Admin Login", _login_form(error))


def login_auth(req) -> Response:
    username = _text(req.form.get("username") if req.form else "")
    password = req.form.get("password") if req.form else ""
    password = "" if password is None else str(password)
    admin = _find(username)
    if admin is None:
        return login_page("Invalid Username")
    if not _password_matches(password, _text(admin.get("pwd"))):
        return login_page("Invalid Password")
    token = secrets.token_urlsafe(32)
    firestore.client().collection("admin_tokens").document(token).set(
        {"admin_id": int(admin.get("id") or 0), "username": admin.get("username") or username}
    )
    response = _redirect("dashboard")
    response.set_cookie(COOKIE, token, max_age=120 * 60, httponly=True, secure=True, samesite="Lax", path="/")
    return response


def dashboard_page(req) -> Response:
    session = admin_session(req)
    if session is None:
        return _redirect("login")
    data = (dashboard().get("data") or {})
    cards = (
        ("Active vendors", data.get("active_vendors")),
        ("Inactive vendors", data.get("inactive_vendors")),
        ("Total vendors", data.get("total_vendors")),
        ("Products", data.get("total_products")),
        ("Invoices", data.get("total_invoices")),
        ("New inquiries", data.get("new_inquiries")),
        ("Inquiries", data.get("total_inquiries")),
        ("Active requirements", data.get("active_requirements")),
        ("Requirements", data.get("total_requirements")),
    )
    stats = "".join(
        f'<div class="stat"><b>{html.escape(str(value if value is not None else 0))}</b>{html.escape(label)}</div>'
        for label, value in cards
    )
    body = f"""
<h2>Admin Dashboard</h2>
<p>Signed in as <b>{html.escape(_text(session.get("username")))}</b></p>
<div class="stats">{stats}</div>
<a class="btn" href="vendor_list">Vendor list</a>
<a class="btn" href="invoices">Invoices</a>
<a class="btn" href="inquiries">Business inquiries</a>
<a class="btn" href="chats">Vendor chats</a>
<a class="btn" href="logout">Logout</a>
"""
    return _page("Admin Dashboard", body, wide=True)


def _need(req) -> Response | None:
    if admin_session(req) is None:
        return _redirect("/api/admin/login")
    return None


def _subscription(end) -> str:
    text = _text(end)[:10]
    if text == "":
        return "No subscription"
    try:
        end_date = date.fromisoformat(text)
    except ValueError:
        return html.escape(text)
    days = (end_date - date.today()).days
    shown = html.escape(end_date.strftime("%d %b %Y"))
    if days < 0:
        return f"Expired<br><small>{shown}</small>"
    if days <= 5:
        return f"{days} din baaki<br><small>Till: {shown}</small>"
    return f"{days} din<br><small>Till: {shown}</small>"


def _notice(req) -> str:
    notice = _text(req.args.get("notice") if req.args else "")
    error = _text(req.args.get("error") if req.args else "")
    if notice:
        return f'<div class="ok">{html.escape(notice)}</div>'
    if error:
        return f'<div class="error">{html.escape(error)}</div>'
    return ""


def vendor_list_page(req) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    rows = vendors("all").get("data") or []
    body_rows = []
    for row in rows:
        status = int(row.get("status") or 0)
        vendor_id = int(row.get("id") or 0)
        if status == 1:
            action = f'<a class="act stop" href="/api/admin/vendor_deactivate/{vendor_id}">Deactivate</a>'
        else:
            action = f'<a class="act go" href="/api/admin/vendor_activate/{vendor_id}">Activate</a>'
        action += f'<a class="act edit" href="/api/admin/vendor_edit/{vendor_id}">Edit</a>'
        action += (
            f'<form method="post" action="/api/admin/vendor_delete/{vendor_id}" '
            f"onsubmit=\"return confirm('Yeh vendor aur uske saare products delete ho jayenge. "
            f"Wapas nahi aayega. Continue?');\">"
            f'<button class="danger" type="submit">Delete</button></form>'
        )
        badge = "Active" if status == 1 else "Inactive"
        body_rows.append(
            "<tr>"
            f"<td>{vendor_id}</td>"
            f"<td>{html.escape(_text(row.get('shop_name')))}</td>"
            f"<td>{html.escape(_text(row.get('username')))}</td>"
            f"<td>{html.escape(_text(row.get('mobile')))}</td>"
            f"<td>{badge}</td>"
            f"<td>{_subscription(row.get('subscription_end'))}</td>"
            f"<td>{action}</td>"
            "</tr>"
        )
    empty = '<tr><td colspan="7">No Vendors Found</td></tr>' if not body_rows else ""
    body = f"""
<style>
table.vendors {{ width:100%; border-collapse:collapse; }}
table.vendors th, table.vendors td {{ border-bottom:1px solid #ddd; padding:8px; text-align:left; vertical-align:top; }}
table.vendors a.act, table.vendors button {{ display:inline-block; width:auto; margin:4px 4px 0 0; padding:6px 10px; }}
table.vendors a.stop {{ background:#dc3545; }}
table.vendors a.go {{ background:#198754; }}
table.vendors a.edit {{ background:#0d6efd; }}
table.vendors button.danger {{ background:#fff; color:#dc3545; border:1px solid #dc3545; }}
</style>
<h2>Vendor List</h2>
{_notice(req)}
<p><a href="/api/admin/dashboard">Dashboard</a> · <a href="/api/admin/add_vendor">Add vendor</a></p>
<table class="vendors">
<thead><tr><th>ID</th><th>Shop Name</th><th>Username</th><th>Mobile</th><th>Status</th><th>Subscription</th><th>Actions</th></tr></thead>
<tbody>{''.join(body_rows)}{empty}</tbody>
</table>
"""
    return _page("Vendor List", body, wide=True, width="1100px")


def _done(result: tuple[dict, int]) -> Response:
    payload, status = result
    message = _text(payload.get("message")) or "Done"
    if status >= 400 or not payload.get("success"):
        return _redirect("/api/admin/vendor_list?" + urllib.parse.urlencode({"error": message}))
    return _redirect("/api/admin/vendor_list?" + urllib.parse.urlencode({"notice": message}))


_FORM_FIELDS = (
    "shop_name",
    "mobile",
    "username",
    "gstin",
    "contact_person",
    "state",
    "state_code",
    "billing_address",
    "location",
)


def _values(req, vendor: dict | None = None) -> dict:
    vendor = vendor or {}
    if req.form:
        return {name: _text(req.form.get(name)) for name in _FORM_FIELDS}
    return {name: _text(vendor.get(name)) for name in _FORM_FIELDS}


def _vendor_form(mode: str, values: dict, error: str = "", vendor_id: int = 0) -> Response:
    adding = mode == "add"
    title = "Add Vendor" if adding else "Edit Vendor"
    action = "/api/admin/vendor_store" if adding else f"/api/admin/vendor_update/{vendor_id}"
    button = "Save Vendor" if adding else "Update Vendor"
    required = "required" if adding else ""
    hint = "" if adding else " <small>(Leave blank for old password)</small>"
    alert = f'<div class="error">{html.escape(error)}</div>' if error else ""

    def field(name: str) -> str:
        return html.escape(values.get(name) or "")

    body = f"""
<h2>{title}</h2>
{alert}
<p><a href="/api/admin/vendor_list">Vendor List</a></p>
<form action="{action}" method="post">
<label>Shop Name</label>
<input type="text" name="shop_name" value="{field("shop_name")}" required>
<label>Mobile Number</label>
<input type="text" name="mobile" maxlength="10" value="{field("mobile")}" required>
<label>Username</label>
<input type="text" name="username" value="{field("username")}" required>
<label>Password{hint}</label>
<input type="password" name="password" {required}>
<p><b>GST / Billing (invoice ke liye)</b></p>
<label>GSTIN</label>
<input type="text" name="gstin" maxlength="15" value="{field("gstin")}">
<label>Contact person</label>
<input type="text" name="contact_person" value="{field("contact_person")}">
<label>State</label>
<input type="text" name="state" value="{field("state")}">
<label>State code (GST)</label>
<input type="text" name="state_code" maxlength="2" value="{field("state_code")}">
<label>Billing address</label>
<textarea name="billing_address">{field("billing_address")}</textarea>
<label>Location (short)</label>
<input type="text" name="location" value="{field("location")}">
<button type="submit">{button}</button>
</form>
"""
    return _page(title, body, wide=True)


def vendor_add_page(req) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    return _vendor_form("add", _values(req))


def vendor_store_page(req) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    payload, status = vendor_store(req)
    if status >= 400 or not payload.get("success"):
        return _vendor_form("add", _values(req), _text(payload.get("message")))
    return _redirect("/api/admin/vendor_list?" + urllib.parse.urlencode({"notice": payload.get("message") or "Vendor added successfully"}))


def vendor_edit_page(req, vendor_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    payload, status = vendor_show(vendor_id)
    if status >= 400 or not payload.get("success"):
        return _redirect("/api/admin/vendor_list?" + urllib.parse.urlencode({"error": payload.get("message") or "Vendor not found"}))
    return _vendor_form("edit", _values(req, payload.get("data") or {}), vendor_id=vendor_id)


def vendor_update_page(req, vendor_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    payload, status = vendor_update(req, vendor_id)
    if status >= 400 or not payload.get("success"):
        return _vendor_form("edit", _values(req), _text(payload.get("message")), vendor_id)
    return _redirect("/api/admin/vendor_list?" + urllib.parse.urlencode({"notice": payload.get("message") or "Vendor updated successfully"}))


def _rupees(value) -> str:
    try:
        return f"{float(value):,.2f}"
    except (TypeError, ValueError):
        return "0.00"


def invoice_list_page(req) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    body_rows = []
    for row in invoices().get("data") or []:
        invoice_id = int(row.get("id") or 0)
        body_rows.append(
            "<tr>"
            f"<td>{html.escape(_text(row.get('invoice_number')) or str(invoice_id))}</td>"
            f"<td>{html.escape(_text(row.get('vendor_name')))}</td>"
            f"<td>{html.escape(_text(row.get('created_at')))}</td>"
            f"<td>Rs {_rupees(row.get('amount'))}</td>"
            f"<td>{html.escape(_text(row.get('razorpay_payment_id')))}</td>"
            f'<td><a class="act" href="/api/admin/invoice/{invoice_id}">View</a></td>'
            "</tr>"
        )
    table = "".join(body_rows) or '<tr><td colspan="6">No invoices found.</td></tr>'
    body = f"""
<style>
table.vendors {{ width:100%; border-collapse:collapse; }}
table.vendors th, table.vendors td {{ border-bottom:1px solid #ddd; padding:8px; text-align:left; vertical-align:top; }}
table.vendors a.act {{ display:inline-block; width:auto; margin:0; padding:6px 10px; background:#007bff; color:#fff; text-decoration:none; }}
</style>
<h2>All Vendor Invoices</h2>
<p><a href="/api/admin/dashboard">Dashboard</a></p>
<table class="vendors">
<thead><tr><th>Invoice #</th><th>Vendor</th><th>Date</th><th>Amount</th><th>Payment ID</th><th>Action</th></tr></thead>
<tbody>{table}</tbody>
</table>
"""
    return _page("Invoices", body, wide=True, width="1100px")


def invoice_page(req, invoice_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    return invoice_html(invoice_id)


def _short(value: str, limit: int = 50) -> str:
    text = _text(value)
    if len(text) <= limit:
        return text
    return text[:limit].rstrip() + "..."


def chat_list_page(req) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    rows = []
    for thread in admin_threads():
        left = int(thread["left_id"])
        right = int(thread["right_id"])
        preview = html.escape(_short(thread.get("preview") or "", 90))
        rows.append(
            "<tr>"
            f"<td>{html.escape(_text(thread.get('stamp')))}</td>"
            f"<td>{html.escape(_text(thread.get('left_name')))}</td>"
            f"<td>{html.escape(_text(thread.get('right_name')))}</td>"
            f"<td>{int(thread.get('count') or 0)}</td>"
            f"<td>{preview}</td>"
            "<td>"
            f'<a class="act" href="/api/admin/chats/{left}/{right}">Open</a>'
            "</td>"
            "</tr>"
        )
    table = "".join(rows) or '<tr><td colspan="6">No vendor chats yet.</td></tr>'
    body = f"""
<style>
table.vendors {{ width:100%; border-collapse:collapse; }}
table.vendors th, table.vendors td {{ border-bottom:1px solid #ddd; padding:8px; text-align:left; vertical-align:top; }}
table.vendors a.act {{ display:inline-block; width:auto; margin:4px 4px 0 0; padding:6px 10px; }}
</style>
<h2>Vendor chats</h2>
<p>Messages between shops. Opening a chat does not mark it read for the vendors.</p>
<p><a href="/api/admin/dashboard">Dashboard</a></p>
<table class="vendors">
<thead><tr><th>Last message</th><th>Shop</th><th>Shop</th><th>Messages</th><th>Preview</th><th></th></tr></thead>
<tbody>{table}</tbody>
</table>
"""
    return _page("Vendor chats", body, wide=True, width="1100px")


def chat_thread_page(req, left_id: int, right_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    low, high = (left_id, right_id) if left_id <= right_id else (right_id, left_id)
    if low == high or low <= 0:
        return _redirect("/api/admin/chats")
    transcript = admin_thread(low, high)
    if not transcript:
        return _redirect("/api/admin/chats")
    left_name = ""
    right_name = ""
    bubbles = []
    for row in transcript:
        sender = int(row.get("sender_id") or 0)
        name = _text(row.get("sender_name"))
        if sender == low:
            left_name = name
        elif sender == high:
            right_name = name
        bubbles.append(
            "<div style=\"margin:0 0 12px;padding:10px 12px;background:#f4f6f9;border-radius:8px;\">"
            f"<b>{html.escape(name)}</b>"
            f" <small>{html.escape(_text(row.get('created_at')))}</small>"
            f"<div style=\"white-space:pre-wrap;margin-top:6px;\">{html.escape(_text(row.get('message')))}</div>"
            "</div>"
        )
    title = f"{left_name or 'Vendor'} and {right_name or 'Vendor'}"
    body = f"""
<h2>{html.escape(title)}</h2>
<p><a href="/api/admin/chats">All vendor chats</a></p>
{''.join(bubbles)}
"""
    return _page("Vendor chat", body, wide=True, width="760px")


def inquiry_list_page(req) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    payload = inquiries()
    new_count = int(payload.get("new_count") or 0)
    badge = f' <span class="error">{new_count} new</span>' if new_count else ""
    body_rows = []
    for row in payload.get("data") or []:
        inquiry_id = int(row.get("id") or 0)
        status = _text(row.get("status")) or "read"
        label = "New" if status == "new" else "Read"
        subject = html.escape(_short(row.get("subject") or ""))
        name = html.escape(_text(row.get("name")))
        if status == "new":
            subject = f"<b>{subject}</b>"
            name = f"<b>{name}</b>"
        body_rows.append(
            "<tr>"
            f"<td>{html.escape(_text(row.get('created_at')))}</td>"
            f"<td>{name}</td>"
            f"<td>{html.escape(_text(row.get('email')))}</td>"
            f"<td>{subject}</td>"
            f"<td>{label}</td>"
            "<td>"
            f'<a class="act" href="/api/admin/inquiry/{inquiry_id}">View</a>'
            f'<form method="post" action="/api/admin/inquiry/{inquiry_id}/delete" '
            f"onsubmit=\"return confirm('Delete this inquiry?');\">"
            f'<button class="danger" type="submit">Delete</button></form>'
            "</td>"
            "</tr>"
        )
    table = "".join(body_rows) or '<tr><td colspan="6">No business inquiries yet.</td></tr>'
    body = f"""
<style>
table.vendors {{ width:100%; border-collapse:collapse; }}
table.vendors th, table.vendors td {{ border-bottom:1px solid #ddd; padding:8px; text-align:left; vertical-align:top; }}
table.vendors a.act, table.vendors button {{ display:inline-block; width:auto; margin:4px 4px 0 0; padding:6px 10px; }}
table.vendors button.danger {{ background:#dc3545; color:#fff; border:0; }}
</style>
<h2>Business Inquiries{badge}</h2>
{_notice(req)}
<p><a href="/api/admin/dashboard">Dashboard</a></p>
<table class="vendors">
<thead><tr><th>Date</th><th>Name / Company</th><th>Email</th><th>Subject</th><th>Status</th><th>Action</th></tr></thead>
<tbody>{table}</tbody>
</table>
"""
    return _page("Business Inquiries", body, wide=True, width="1100px")


def inquiry_detail_page(req, inquiry_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    payload, status = inquiry_show(inquiry_id)
    if status >= 400 or not payload.get("success"):
        return _redirect("/api/admin/inquiries?" + urllib.parse.urlencode({"error": payload.get("message") or "Inquiry not found"}))
    row = payload.get("data") or {}
    email = _text(row.get("email"))
    subject = _text(row.get("subject"))
    reply = "mailto:" + email + "?subject=" + urllib.parse.quote("Re: " + subject)
    body = f"""
<h2>Business Inquiry Details</h2>
<p><a href="/api/admin/inquiries">Back to List</a></p>
<p><b>Name / Company</b><br>{html.escape(_text(row.get("name")))}</p>
<p><b>Email</b><br><a href="mailto:{html.escape(email)}">{html.escape(email)}</a></p>
<p><b>Subject</b><br>{html.escape(subject)}</p>
<p><b>Submitted</b><br>{html.escape(_text(row.get("created_at")))}</p>
<p><b>Message</b></p>
<p style="white-space:pre-wrap;background:#f4f6f9;padding:12px;">{html.escape(_text(row.get("message")))}</p>
<p><a class="btn" href="{html.escape(reply, quote=True)}">Reply via Email</a></p>
<form method="post" action="/api/admin/inquiry/{int(row.get("id") or inquiry_id)}/delete" onsubmit="return confirm('Delete this inquiry?');">
<button class="danger" type="submit" style="background:#dc3545;">Delete</button>
</form>
"""
    return _page("Business Inquiry", body, wide=True)


def inquiry_delete_page(req, inquiry_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    payload, status = inquiry_delete(inquiry_id)
    key = "notice" if payload.get("success") and status < 400 else "error"
    message = payload.get("message") or "Inquiry not found"
    return _redirect("/api/admin/inquiries?" + urllib.parse.urlencode({key: message}))


def vendor_activate(req, vendor_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    return _done(activate(vendor_id))


def vendor_deactivate(req, vendor_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    return _done(deactivate(vendor_id))


def vendor_delete(req, vendor_id: int) -> Response:
    guest = _need(req)
    if guest is not None:
        return guest
    return _done(delete_vendor(vendor_id))


def logout(req) -> Response:
    session = admin_session(req)
    if session is not None:
        firestore.client().collection("admin_tokens").document(session["_token"]).delete()
    response = _redirect("login")
    response.set_cookie(COOKIE, "", max_age=0, httponly=True, secure=True, samesite="Lax", path="/")
    return response
