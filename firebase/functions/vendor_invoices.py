from __future__ import annotations

import html
from datetime import datetime

from firebase_admin import firestore
from flask import Response

from admin_api import invoice_html
from web_session import _page, vendor_session


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _redirect(location: str) -> Response:
    response = Response("", status=302)
    response.headers["Location"] = location
    return response


def _login() -> Response:
    return _redirect("login")


def _rows(vendor_id: int) -> list[dict]:
    rows = []
    for doc in firestore.client().collection("invoices").stream():
        row = doc.to_dict() or {}
        if int(row.get("vendor_id") or 0) != vendor_id:
            continue
        row["id"] = int(row.get("id") or doc.id or 0)
        rows.append(row)
    rows.sort(key=lambda row: (_text(row.get("created_at")), row["id"]), reverse=True)
    return rows


def _date(value) -> str:
    raw = _text(value).replace("T", " ")[:10]
    try:
        return datetime.strptime(raw, "%Y-%m-%d").strftime("%d %b %Y")
    except ValueError:
        return raw


def _amount(value) -> str:
    try:
        return f"{float(value):,.2f}"
    except (TypeError, ValueError):
        return "0.00"


def invoice_list(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    body_rows = []
    for row in _rows(vendor_id):
        body_rows.append(
            "<tr>"
            f"<td>{html.escape(_text(row.get('invoice_number')) or str(row['id']))}</td>"
            f"<td>{html.escape(_date(row.get('created_at')))}</td>"
            f"<td>Rs {_amount(row.get('amount'))}</td>"
            f"<td>{html.escape(_text(row.get('razorpay_payment_id')))}</td>"
            f'<td><a href="/api/vendor/invoice/{row["id"]}" target="_blank" class="btn btn-sm btn-primary">'
            '<i class="fas fa-file-invoice"></i> View / Download</a></td>'
            "</tr>"
        )
    table = "".join(body_rows) or '<tr><td colspan="5" class="text-center">No invoices found.</td></tr>'
    body = f"""
<h3>My Invoices 🧾</h3>
<hr>
<div class="card">
<div class="card-body table-responsive p-0">
<table class="table table-hover text-nowrap">
<thead><tr>
<th>Invoice #</th><th>Date</th><th>Amount</th><th>Payment ID</th><th>Action</th>
</tr></thead>
<tbody>{table}</tbody>
</table>
</div>
</div>
"""
    return _page("My Invoices", body, wide=True)


def invoice_page(req, invoice_id: int) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    doc = firestore.client().collection("invoices").document(str(invoice_id)).get()
    if not doc.exists:
        return Response("Invoice not found", status=404)
    row = doc.to_dict() or {}
    if int(row.get("vendor_id") or 0) != int(session.get("vendor_id") or 0):
        return Response("Unauthorized action.", status=403)
    return invoice_html(invoice_id)
