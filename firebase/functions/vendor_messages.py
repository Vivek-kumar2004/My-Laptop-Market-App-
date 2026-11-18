from __future__ import annotations

import html
import json
import re
from datetime import datetime
from urllib.parse import quote

from firebase_admin import firestore
from flask import Response

from messages import chat, contacts, conversations, send_message
from web_session import _page, vendor_session

SITE = "https://mylaptopmarket.com"


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


def _vendor(vendor_id: int) -> dict | None:
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    return doc.to_dict() or {}


def _active(vendor_id: int) -> dict | None:
    vendor = _vendor(vendor_id)
    if vendor is None or int(vendor.get("status") or 0) != 1:
        return None
    return vendor


def _product(product_id: int) -> dict | None:
    if product_id <= 0:
        return None
    doc = firestore.client().collection("products").document(str(product_id)).get()
    if not doc.exists:
        return None
    row = doc.to_dict() or {}
    if int(row.get("status") or 0) != 1:
        return None
    row["id"] = int(row.get("id") or product_id)
    return row


def _context(product: dict) -> str:
    price = _text(product.get("price")).replace(",", "")
    amount = ""
    try:
        number = float(price)
        if number > 0:
            amount = f" · ₹{int(number):,}"
    except ValueError:
        amount = ""
    name = _text(product.get("product_name")) or "Listing"
    return "\n".join(
        [
            f"Regarding product: {name}",
            f"Listing #{int(product.get('id') or 0)}{amount}",
            f"{SITE}/product.html?id={int(product.get('id') or 0)}",
        ]
    )


def _json(body: dict, status: int = 200) -> Response:
    return Response(json.dumps(body), status=status, mimetype="application/json")


def _clock(value) -> str:
    text = _text(value).replace("T", " ").replace("Z", "")[:19]
    for size, pattern in ((19, "%Y-%m-%d %H:%M:%S"), (16, "%Y-%m-%d %H:%M"), (10, "%Y-%m-%d")):
        try:
            return datetime.strptime(text[:size], pattern).strftime("%d %b %Y, %I:%M %p")
        except ValueError:
            continue
    return text


def _alert(notice: str = "", error: str = "") -> str:
    parts = []
    if notice:
        parts.append(f'<div class="alert alert-success">{html.escape(notice)}</div>')
    if error:
        parts.append(f'<div class="alert alert-danger">{html.escape(error)}</div>')
    return "".join(parts)


def _logo_src(vendor: dict | None) -> str:
    vendor = vendor or {}
    logo = _text(vendor.get("logo_url")) or _text(vendor.get("logo"))
    if logo.startswith("http"):
        return logo
    if logo:
        return "https://storage.googleapis.com/mylaptopmarket-app-web/mylaptop/assets/uploads/vendors/" + logo.split("/")[-1]
    return "https://storage.googleapis.com/mylaptopmarket-app-web/images/mylaptoplogo.png"


def _avatar(vendor: dict | None, size: int) -> str:
    return (
        f'<img src="{html.escape(_logo_src(vendor))}" alt="" '
        f'style="width:{size}px;height:{size}px;border-radius:50%;object-fit:cover;background:#fff;">'
    )


def _day(value) -> str:
    text = _text(value).replace("T", " ")[:10]
    try:
        return datetime.strptime(text, "%Y-%m-%d").strftime("%d %b")
    except ValueError:
        return text


def _message_html(value) -> str:
    escaped = html.escape(_text(value))
    linked = re.sub(
        r"(https?://[^\s<]+)",
        r'<a href="\1" target="_blank" rel="noopener">\1</a>',
        escaped,
    )
    return linked.replace("\n", "<br>")


def inbox(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    contact_id = int(_text(req.args.get("contact") or "0") or 0)
    product_id = int(_text(req.args.get("product") or "0") or 0)
    product = _product(product_id)
    if product is not None:
        contact_id = int(product.get("vendor_id") or 0)
    if contact_id == vendor_id:
        contact_id = 0
        product = None
    contact = _active(contact_id) if contact_id else None
    if contact is None:
        product = None
    thread = chat(vendor_id, contact_id)["messages"] if contact else []
    rows = []
    conversation_rows = conversations(vendor_id)["conversations"]
    for item in conversation_rows:
        other = int(item.get("contact_id") or 0)
        other_vendor = _vendor(other)
        unread = int(item.get("unread_count") or 0)
        preview = _text(item.get("last_message")).replace("\n", " ")
        if len(preview) > 60:
            preview = preview[:57] + "..."
        badge = f'<span class="badge badge-danger mt-2">{unread} new</span>' if unread else ""
        active = " active" if other == contact_id else ""
        rows.append(
            f'<a href="/api/vendor/messages?contact={other}" class="conversation-item{active}">'
            '<div class="d-flex justify-content-between align-items-start">'
            '<div class="conversation-name d-flex align-items-center" style="gap:8px;">'
            f'{_avatar(other_vendor, 28)}{html.escape(_text(item.get("contact_name")) or "Shop")}</div>'
            f'<div class="conversation-meta">{html.escape(_day(item.get("last_message_time")))}</div></div>'
            f'<div class="conversation-preview">{html.escape(preview)}</div>{badge}</a>'
        )
    options = ['<option value="">Start new chat...</option>']
    for item in contacts(vendor_id)["contacts"]:
        other = int(item.get("id") or 0)
        selected = " selected" if other == contact_id else ""
        label = _text(item.get("shop_name")) or _text(item.get("username")) or "Shop"
        options.append(f'<option value="{other}"{selected}>{html.escape(label)}</option>')
    bubbles = []
    for item in thread:
        mine = int(item.get("sender_id") or 0) == vendor_id
        kind = "mine" if mine else "theirs"
        if "Regarding product:" in _text(item.get("message")):
            kind += " product-context"
        bubbles.append(
            f'<div class="chat-bubble {kind}">{_message_html(item.get("message"))}'
            f'<div class="chat-time">{html.escape(_clock(item.get("created_at")))}</div></div>'
        )
    if contact:
        name = html.escape(_text(contact.get("shop_name")) or _text(contact.get("username")))
        banner = ""
        hidden_product = ""
        if product:
            product_name = html.escape(_text(product.get("product_name")) or "Listing")
            product_link = f"{SITE}/product.html?id={int(product.get('id') or 0)}"
            banner = (
                '<div class="chat-product-banner"><i class="fas fa-box-open"></i><div>'
                '<div class="small text-uppercase" style="letter-spacing:.06em;color:#64748b;">Product enquiry</div>'
                f'<a href="{product_link}" target="_blank" rel="noopener">{product_name}</a></div></div>'
            )
            hidden_product = f'<input type="hidden" name="product_id" value="{int(product.get("id") or 0)}">'
        chat_panel = f"""
<div class="chat-header d-flex align-items-center" style="gap:10px;">
{_avatar(contact, 36)}
<div><strong>{name}</strong><div class="small" style="opacity:.75;">B2B vendor chat</div></div>
</div>
{banner}
<div class="chat-body" id="chatBody">{"".join(bubbles)}</div>
<div class="chat-compose">
<form class="adminlte-form" action="/api/vendor/messages/send" method="post" id="messageForm">
<input type="hidden" name="receiver_id" value="{contact_id}">
{hidden_product}
<div class="input-group">
<textarea name="message" class="form-control" rows="2" placeholder="Type your message..." required maxlength="5000"></textarea>
<div class="input-group-append"><button class="btn btn-primary px-4" type="submit"><i class="fas fa-paper-plane"></i></button></div>
</div>
</form>
</div>
<script>
(function () {{
  var box = document.getElementById("chatBody");
  if (!box) return;
  function esc(text) {{
    return String(text || "").replace(/[&<>"']/g, function (ch) {{
      return {{"&":"&amp;","<":"&lt;",">":"&gt;","\\"":"&quot;","'":"&#39;"}}[ch];
    }});
  }}
  function render(messages) {{
    box.innerHTML = messages.map(function (msg) {{
      var cls = (msg.is_mine ? "mine" : "theirs") +
        ((msg.message || "").indexOf("Regarding product:") !== -1 ? " product-context" : "");
      var body = esc(msg.message).replace(/\\n/g, "<br>");
      body = body.replace(/(https?:\\/\\/[^\\s<]+)/g, '<a href="$1" target="_blank" rel="noopener">$1</a>');
      return '<div class="chat-bubble ' + cls + '">' + body + '<div class="chat-time">' + esc(msg.time) + "</div></div>";
    }}).join("");
    box.scrollTop = box.scrollHeight;
  }}
  function poll() {{
    fetch("/api/vendor/messages/poll/{contact_id}", {{headers: {{"Accept": "application/json"}}, credentials: "same-origin"}})
      .then(function (res) {{ return res.json(); }})
      .then(function (data) {{ if (data.success && data.messages) render(data.messages); }})
      .catch(function () {{}});
  }}
  box.scrollTop = box.scrollHeight;
  setInterval(poll, 5000);
}})();
</script>
"""
    else:
        chat_panel = """
<div class="chat-empty">
<i class="fas fa-comments fa-3x mb-3 text-primary"></i>
<h5>Select a conversation</h5>
<p>Choose a vendor from the list or start a new chat to message other businesses on the platform.</p>
</div>
"""
    conversation_list = "".join(rows) or (
        '<div class="p-4 text-center text-muted"><i class="fas fa-inbox fa-2x mb-2"></i>'
        "<p class=\"mb-0\">No conversations yet. Start a new chat with a vendor.</p></div>"
    )
    body = f"""
<style>
.messages-wrapper {{ height: calc(100vh - 180px); min-height: 520px; }}
.conversations-panel, .chat-panel {{ height: 100%; border-radius: 12px; overflow: hidden; background: #fff; box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08); display: flex; flex-direction: column; }}
.conversations-header, .chat-header {{ padding: 16px 18px; background: linear-gradient(135deg, #0f172a, #1e3a5f); color: #fff; flex-shrink: 0; }}
.conversation-list, .chat-body {{ flex: 1; min-height: 0; overflow-y: auto; }}
.conversation-item {{ display: block; padding: 14px 18px; border-bottom: 1px solid #eef2f7; color: inherit; text-decoration: none; }}
.conversation-item:hover, .conversation-item.active {{ background: #f0f7ff; text-decoration: none; color: inherit; }}
.conversation-name {{ font-weight: 700; color: #0f172a; }}
.conversation-preview {{ color: #64748b; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 4px; }}
.conversation-meta {{ font-size: 11px; color: #94a3b8; }}
.chat-body {{ padding: 20px; background: #f8fafc; }}
.chat-bubble {{ max-width: 75%; padding: 10px 14px; border-radius: 16px; margin-bottom: 12px; font-size: 14px; line-height: 1.5; word-break: break-word; }}
.chat-bubble a {{ color: inherit; text-decoration: underline; word-break: break-all; }}
.chat-bubble.mine {{ margin-left: auto; background: #3376cd; color: #fff; border-bottom-right-radius: 4px; }}
.chat-bubble.theirs {{ margin-right: auto; background: #fff; color: #0f172a; border: 1px solid #e2e8f0; border-bottom-left-radius: 4px; }}
.chat-bubble.product-context {{ border: 1px solid rgba(51, 118, 205, 0.35); background: #eef6ff; color: #0f172a; }}
.chat-bubble.mine.product-context {{ background: #1d4f9c; color: #fff; border-color: transparent; }}
.chat-product-banner {{ display: flex; align-items: center; gap: 12px; margin: 12px 16px 0; padding: 12px 14px; border-radius: 12px; background: #eef6ff; border: 1px solid #cfe0f5; }}
.chat-product-banner a {{ font-weight: 700; color: #1d4f9c; }}
.chat-time {{ font-size: 11px; opacity: 0.75; margin-top: 4px; }}
.chat-compose {{ padding: 12px 16px 16px; }}
.chat-compose textarea.form-control {{ min-height: 72px; resize: vertical; }}
.chat-empty {{ height: 100%; display: flex; align-items: center; justify-content: center; flex-direction: column; color: #64748b; text-align: center; padding: 40px; }}
</style>
<h3>Messages <i class="fas fa-comments"></i></h3>
<hr>
{_alert(_text(req.args.get("notice")), _text(req.args.get("error")))}
<div class="row messages-wrapper">
<div class="col-lg-4 mb-3 mb-lg-0">
<div class="conversations-panel">
<div class="conversations-header">
<div class="d-flex justify-content-between align-items-center mb-3">
<strong>Conversations</strong><span class="badge badge-light">{len(conversation_rows)}</span>
</div>
<form class="adminlte-form" method="get" action="/api/vendor/messages">
<div class="input-group input-group-sm">
<select name="contact" class="form-control" required>{"".join(options)}</select>
<div class="input-group-append"><button class="btn btn-primary" type="submit">Open</button></div>
</div>
</form>
</div>
<div class="conversation-list">{conversation_list}</div>
</div>
</div>
<div class="col-lg-8"><div class="chat-panel">{chat_panel}</div></div>
</div>
"""
    return _page("Messages", body, wide=True)


def poll(req, contact_id: int) -> Response:
    session = vendor_session(req)
    if session is None:
        return _json({"success": False, "error": "Unauthorized"}, 401)
    vendor_id = int(session.get("vendor_id") or 0)
    if contact_id == vendor_id:
        return _json({"success": False, "error": "Invalid contact"}, 400)
    rows = []
    for item in chat(vendor_id, contact_id).get("messages") or []:
        rows.append(
            {
                "id": int(item.get("id") or 0),
                "sender_id": int(item.get("sender_id") or 0),
                "receiver_id": int(item.get("receiver_id") or 0),
                "message": _text(item.get("message")),
                "is_mine": int(item.get("sender_id") or 0) == vendor_id,
                "time": _clock(item.get("created_at")),
            }
        )
    return _json({"success": True, "messages": rows})


def send(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    receiver_raw = _text(req.form.get("receiver_id") if req.form else "")
    text = _text(req.form.get("message") if req.form else "")
    product_raw = _text(req.form.get("product_id") if req.form else "")
    back = "../messages"
    if not receiver_raw.isdigit():
        return _redirect(back + "?error=" + quote("Vendor not found or inactive."))
    receiver_id = int(receiver_raw)
    back = f"../messages?contact={receiver_id}"
    if receiver_id == vendor_id:
        return _redirect("../messages?error=" + quote("You cannot message yourself."))
    if _active(receiver_id) is None:
        return _redirect(back + "&error=" + quote("Vendor not found or inactive."))
    if text == "":
        return _redirect(back + "&error=" + quote("Message is required."))
    product_id = int(product_raw) if product_raw.isdigit() else 0
    product = _product(product_id)
    if product and int(product.get("vendor_id") or 0) == receiver_id:
        context = _context(product)
        needle = f"product.html?id={product_id}"
        if needle not in text:
            text = context + "\n\n" + text
    if len(text) > 5000:
        return _redirect(back + "&error=" + quote("The message may not be greater than 5000 characters."))
    result, status = send_message(vendor_id, receiver_id, text)
    if status != 200:
        return _redirect(back + "&error=" + quote(_text(result.get("message")) or "Could not send."))
    return _redirect(back + "&notice=" + quote("Message sent."))
