import hashlib
import hmac
import json
import os
import urllib.request
from datetime import date, datetime, timedelta, timezone

from firebase_admin import firestore
from flask import Response


AMOUNT = int(os.environ.get("RAZORPAY_SUBSCRIPTION_AMOUNT", "100"))
COMPANY_STATE = "07"
# Turn this back to True to show ₹1 Razorpay checkout on vendor website again.
SHOW_VENDOR_PAYMENT = False


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _keys() -> tuple[str, str]:
    return _text(os.environ.get("RAZORPAY_KEY_ID")), _text(os.environ.get("RAZORPAY_KEY_SECRET"))


def _vendor(vendor_id: int):
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}
    data["_ref"] = doc.reference
    return data


def _end_date(vendor: dict):
    raw = _text(vendor.get("subscription_end"))[:10]
    if raw == "":
        return None
    try:
        return date.fromisoformat(raw)
    except ValueError:
        return None


def _days_left(vendor: dict) -> int:
    end = _end_date(vendor)
    if end is None:
        return 0
    return max(0, (end - date.today()).days)


def _field(req, name: str) -> str:
    if req.form and name in req.form:
        return _text(req.form.get(name))
    body = req.get_json(silent=True) or {}
    if name in body and body.get(name) is not None:
        return _text(body.get(name))
    return _text(req.args.get(name))


def _page(vendor: dict, source: str, error: str = "") -> Response:
    import html

    shop = _text(vendor.get("shop_name")) or _text(vendor.get("username"))
    mobile = _text(vendor.get("mobile"))
    end = _end_date(vendor)
    days = _days_left(vendor)
    renewal = int(vendor.get("status") or 0) == 1 and end is not None
    price = AMOUNT // 100
    key_id, _secret = _keys()
    status = ""
    if renewal and end is not None:
        if days > 0:
            status = (
                "<div class='subscription-status'><strong>Current Subscription</strong><br>"
                f"Active till: <b>{html.escape(end.strftime('%d %b %Y'))}</b> ({days} din baaki)</div>"
            )
        else:
            status = (
                "<div class='subscription-status subscription-expired'><strong>Subscription Expired</strong><br>"
                "Aapke products hide hain. Renew karo activate karne ke liye.</div>"
            )
    title = "Renew Subscription" if renewal else "Activate Subscription"
    button = f"Renew — ₹{price}" if renewal else f"Subscribe Now — ₹{price}"
    alert = f"<div class='alert'>{html.escape(error)}</div>" if error else ""
    page = f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Activate Subscription — MyLaptopMarket</title>
<style>
body{{background:#f4f6f9;font-family:Arial,Helvetica,sans-serif;color:#222}}
.subscription-box{{max-width:480px;margin:60px auto}}
.plan-card{{border:2px solid #007bff;border-radius:12px;padding:30px;text-align:center;background:#fff}}
.price-tag{{font-size:3rem;font-weight:bold;color:#007bff}}
.price-tag small{{font-size:1rem;color:#666}}
.feature-list{{list-style:none;padding:0;margin:20px 0}}
.feature-list li{{padding:6px 0;color:#444}}
.feature-list li::before{{content:"✅ "}}
.subscription-status{{border-left:4px solid #28a745;background:#f0fff4;padding:12px 16px;border-radius:6px;margin-bottom:20px}}
.subscription-expired{{border-left-color:#dc3545;background:#fff5f5}}
.alert{{background:#fff5f5;color:#991b1b;border:1px solid #fca5a5;padding:10px 12px;border-radius:6px;margin-bottom:16px}}
button{{background:#007bff;color:#fff;border:0;border-radius:6px;padding:12px 16px;font-size:16px;width:100%;cursor:pointer}}
.muted{{color:#666}} a{{color:#666}}
</style></head>
<body><div class="subscription-box">
<div style="text-align:center"><h2><b>MyLaptop</b>Market</h2><p class="muted">Vendor Subscription</p></div>
{alert}{status}
<div class="plan-card"><h4>{title}</h4><p class="muted">Apni dukaan online rakhein</p>
<div class="price-tag">₹{price} <small>/30 din</small></div>
<ul class="feature-list"><li>Unlimited Products List</li><li>Mobile App me Visibility</li><li>30 Din Ki Guarantee</li><li>Auto-renewal Option</li></ul>
<button id="rzp-button">{button}</button>
<p class="muted">Secured by Razorpay | UPI, Card, NetBanking accepted</p></div>
</div>
<form id="payment-form" action="verify" method="POST" style="display:none">
<input type="hidden" name="razorpay_order_id" id="razorpay_order_id">
<input type="hidden" name="razorpay_payment_id" id="razorpay_payment_id">
<input type="hidden" name="razorpay_signature" id="razorpay_signature">
<input type="hidden" name="vendor_id" value="{int(vendor.get('id') or 0)}">
<input type="hidden" name="source" value="{html.escape(source)}">
</form>
<script src="https://checkout.razorpay.com/v1/checkout.js"></script>
<script>
document.getElementById('rzp-button').onclick = function(e) {{
  e.preventDefault();
  fetch('create_order', {{
    method: 'POST',
    headers: {{'Content-Type': 'application/json', 'Accept': 'application/json'}},
    body: JSON.stringify({{vendor_id: {int(vendor.get('id') or 0)}}})
  }}).then(async function(res) {{
    const text = await res.text();
    try {{ return JSON.parse(text); }} catch (err) {{
      alert('Payment server HTML bhej raha hai, JSON nahi.');
      throw err;
    }}
  }}).then(function(data) {{
    if (data.error) {{ alert('Error: ' + data.error); return; }}
    var options = {{
      key: {json.dumps(key_id)},
      amount: data.amount,
      currency: data.currency,
      name: 'MyLaptopMarket',
      description: 'Vendor Subscription — 30 Days',
      order_id: data.order_id,
      prefill: {{name: {json.dumps(shop)}, contact: {json.dumps(mobile)}}},
      theme: {{color: '#007bff'}},
      handler: function(response) {{
        document.getElementById('razorpay_order_id').value = response.razorpay_order_id;
        document.getElementById('razorpay_payment_id').value = response.razorpay_payment_id;
        document.getElementById('razorpay_signature').value = response.razorpay_signature;
        document.getElementById('payment-form').submit();
      }},
      modal: {{ondismiss: function() {{ alert('Payment cancelled. Koi baat nahi, baad me try karein!'); }}}}
    }};
    new Razorpay(options).open();
  }}).catch(function(err) {{ alert('Network error: ' + err); }});
}};
</script></body></html>"""
    return Response(page, mimetype="text/html")


def checkout(req) -> Response:
    if not SHOW_VENDOR_PAYMENT:
        return Response("", status=302, headers={"Location": "/api/vendor/login", "Cache-Control": "no-store"})
    host = (req.host or "").split(":")[0].lower()
    if "cloudfunctions.net" in host:
        query = req.query_string.decode() if isinstance(req.query_string, (bytes, bytearray)) else str(req.query_string or "")
        target = "https://mylaptopmarket.com/api/subscription/checkout"
        if query:
            target += "?" + query
        return Response("", status=302, headers={"Location": target, "Cache-Control": "no-store"})
    raw = _field(req, "vendor_id")
    if not raw.isdigit():
        return Response("Please login first.", status=400, mimetype="text/html")
    vendor = _vendor(int(raw))
    if vendor is None:
        return Response("Vendor not found.", status=404, mimetype="text/html")
    source = _field(req, "source") or "web"
    return _page(vendor, source)


def create_order(req) -> tuple[dict, int]:
    if not SHOW_VENDOR_PAYMENT:
        return {"error": "Subscription payment is paused."}, 503
    raw = _field(req, "vendor_id")
    if not raw.isdigit():
        return {"error": "Vendor ID missing"}, 400
    if _vendor(int(raw)) is None:
        return {"error": "Vendor not found"}, 404
    key_id, key_secret = _keys()
    if key_id == "" or key_secret == "":
        return {"error": "Razorpay keys missing in live .env"}, 500
    payload = json.dumps(
        {
            "receipt": f"sub_vendor_{raw}_{int(datetime.now().timestamp())}",
            "amount": AMOUNT,
            "currency": "INR",
            "notes": {"vendor_id": raw},
        }
    ).encode()
    request = urllib.request.Request(
        "https://api.razorpay.com/v1/orders",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    token = f"{key_id}:{key_secret}".encode()
    import base64

    request.add_header("Authorization", "Basic " + base64.b64encode(token).decode())
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            order = json.loads(response.read().decode())
    except Exception as exc:
        message = getattr(exc, "read", lambda: b"")()
        try:
            parsed = json.loads(message.decode() or "{}")
            detail = ((parsed.get("error") or {}).get("description")) or str(exc)
        except Exception:
            detail = str(exc) or "Razorpay se connect nahi ho paya"
        return {"error": detail}, 500
    if not order.get("id"):
        return {"error": "Razorpay se connect nahi ho paya"}, 500
    return {
        "order_id": order["id"],
        "amount": AMOUNT,
        "currency": "INR",
        "vendor_id": int(raw),
    }, 200


def _next_invoice(client) -> tuple[int, str]:
    year = date.today().year
    month = date.today().month
    start = year if month >= 4 else year - 1
    prefix = f"MLM/{str(start)[-2:]}-{str(start + 1)[-2:]}/"
    seq = 0
    max_id = 0
    for doc in client.collection("invoices").stream():
        row = doc.to_dict() or {}
        max_id = max(max_id, int(row.get("id") or 0))
        number = _text(row.get("invoice_number"))
        if number.startswith(prefix) and number.rsplit("/", 1)[-1].isdigit():
            seq = max(seq, int(number.rsplit("/", 1)[-1]))
    return max_id + 1, prefix + f"{seq + 1:04d}"


def _activate(vendor: dict, payment_id: str) -> str:
    today = date.today()
    end = _end_date(vendor)
    base = end if end is not None and end > today else today
    new_end = base + timedelta(days=30)
    vendor_id = int(vendor.get("id") or 0)
    vendor["_ref"].update(
        {
            "status": 1,
            "subscription_start": today.isoformat(),
            "subscription_end": new_end.isoformat(),
        }
    )
    client = firestore.client()
    for doc in client.collection("products").where("vendor_id", "==", vendor_id).stream():
        doc.reference.update({"status": 1})
    total = AMOUNT / 100
    subtotal = round(total / 1.18, 2)
    tax = round(total - subtotal, 2)
    same_state = _text(vendor.get("state_code")) == COMPANY_STATE
    invoice_id, number = _next_invoice(client)
    now = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    place = _text(vendor.get("state"))
    code = _text(vendor.get("state_code"))
    if code:
        place = f"{place} ({code})".strip()
    client.collection("invoices").document(str(invoice_id)).set(
        {
            "id": invoice_id,
            "invoice_number": number,
            "vendor_id": vendor_id,
            "amount": total,
            "subtotal": subtotal,
            "rate": subtotal,
            "qty": 1,
            "cgst": round(tax / 2, 2) if same_state else 0,
            "sgst": round(tax / 2, 2) if same_state else 0,
            "igst": 0 if same_state else tax,
            "tax_percent": 18,
            "hsn_sac": "998314",
            "place_of_supply": place or "Delhi (07)",
            "terms": "Paid",
            "due_date": today.isoformat(),
            "razorpay_payment_id": payment_id,
            "status": "paid",
            "created_at": now,
            "updated_at": now,
        }
    )
    return new_end.strftime("%d %b %Y")


def verify(req) -> Response:
    if not SHOW_VENDOR_PAYMENT:
        return Response("", status=302, headers={"Location": "/api/vendor/login", "Cache-Control": "no-store"})
    vendor_raw = _field(req, "vendor_id")
    order_id = _field(req, "razorpay_order_id")
    payment_id = _field(req, "razorpay_payment_id")
    signature = _field(req, "razorpay_signature")
    source = _field(req, "source") or "web"
    vendor = _vendor(int(vendor_raw)) if vendor_raw.isdigit() else None
    _key_id, key_secret = _keys()
    expected = hmac.new(
        key_secret.encode(),
        f"{order_id}|{payment_id}".encode(),
        hashlib.sha256,
    ).hexdigest()
    if vendor is None or len(signature) != len(expected) or not hmac.compare_digest(expected, signature):
        if vendor is None:
            return Response("Vendor not found.", status=404, mimetype="text/html")
        return _page(vendor, source, "Payment verification failed! Please contact support.")
    until = _activate(vendor, payment_id)
    if source == "app":
        return app_success(vendor_id=int(vendor_raw), until=until)
    return Response(
        f"Subscription activate ho gayi! Valid till: {until}",
        mimetype="text/html",
    )


def app_success(req=None, vendor_id: int = 0, until: str = "") -> Response:
    if req is not None and vendor_id == 0:
        raw = _field(req, "vendor_id")
        vendor_id = int(raw) if raw.isdigit() else 0
        until = _field(req, "until")
    import html

    link = f"mylaptopmarket://payment-success?vendorId={vendor_id}"
    until_html = f"<p>Valid till: <b>{html.escape(until)}</b></p>" if until else ""
    page = f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Payment Successful — MyLaptopMarket</title>
<style>
body{{background:#f4f6f9;font-family:Arial,Helvetica,sans-serif}}
.box{{max-width:420px;margin:80px auto;background:#fff;border-radius:16px;padding:32px 24px;text-align:center}}
.ok{{font-size:56px}} .btn{{display:inline-block;margin-top:18px;background:#2563eb;color:#fff;padding:12px 22px;border-radius:10px;font-weight:700;text-decoration:none}}
.hint{{color:#64748b;font-size:14px;margin-top:14px}}
</style></head><body><div class="box"><div class="ok">✅</div>
<h3>Payment successful</h3><p>Aapki subscription activate ho gayi hai.</p>{until_html}
<a class="btn" id="openApp" href="{html.escape(link)}">Open MyLaptopMarket App</a>
<p class="hint">Agar app auto-open na ho, pehle browser band karke app kholo,<br>phir <b>Seller Verification → Refresh Status</b> dabao.</p>
</div><script>setTimeout(function(){{window.location.href={json.dumps(link)};}},600);</script></body></html>"""
    return Response(page, mimetype="text/html")
