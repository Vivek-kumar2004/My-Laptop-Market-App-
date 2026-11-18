from __future__ import annotations

import html
from urllib.parse import quote

from flask import Response

from products_write import CATEGORIES
from requirements_api import close, create, delete, feed, mine
from web_session import _page, vendor_session


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _redirect(location: str) -> Response:
    response = Response("", status=302)
    response.headers["Location"] = location
    return response


def _board(notice: str = "", error: str = "") -> str:
    query = []
    if notice:
        query.append("notice=" + quote(notice))
    if error:
        query.append("error=" + quote(error))
    suffix = ("?" + "&".join(query)) if query else ""
    return "/api/vendor/requirements" + suffix


def _login() -> Response:
    return _redirect("/api/vendor/login")


def _alert(notice: str = "", error: str = "") -> str:
    parts = []
    if notice:
        parts.append(f'<div class="alert alert-success">{html.escape(notice)}</div>')
    if error:
        parts.append(f'<div class="alert alert-danger">{html.escape(error)}</div>')
    return "".join(parts)


def _logo(url: str, size: int) -> str:
    src = _text(url)
    if not src.startswith("http"):
        return '<i class="fas fa-store mr-1"></i>'
    return (
        f'<img src="{html.escape(src)}" alt="" style="width:{size}px;height:{size}px;'
        f'border-radius:50%;object-fit:cover;margin-right:{6 if size < 30 else 10}px;">'
    )


def _clip(value: str, limit: int = 180) -> str:
    text = _text(value)
    if len(text) <= limit:
        return text
    return text[: limit - 3] + "..."


def board(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    cards = []
    for group in feed(vendor_id).get("data") or []:
        shops = []
        category = html.escape(_text(group.get("category")))
        for vendor in group.get("vendors") or []:
            requirement_id = int(vendor.get("requirement_id") or vendor.get("id") or 0)
            shop = html.escape(_text(vendor.get("shop_name")) or "Shop")
            qty = vendor.get("quantity")
            qty_badge = (
                f'<span class="badge badge-secondary">Qty: {html.escape(_text(qty))}</span>'
                if qty not in (None, "", 0)
                else ""
            )
            detail = html.escape(_text(vendor.get("description")) or "No extra description provided.")
            title = html.escape(_text(vendor.get("canonical_title")) or _text(vendor.get("title")) or _text(group.get("title")))
            shops.append(
                f'<button type="button" class="btn btn-sm btn-outline-primary mr-1 mb-1 d-inline-flex align-items-center" '
                f'data-toggle="modal" data-target="#vendorReqModal{requirement_id}">'
                f'{_logo(vendor.get("logo_url"), 18)}{shop}</button>'
                f'<div class="modal fade" id="vendorReqModal{requirement_id}" tabindex="-1" role="dialog">'
                '<div class="modal-dialog" role="document"><div class="modal-content">'
                '<div class="modal-header align-items-center">'
                f'{_logo(vendor.get("logo_url"), 36)}<h5 class="modal-title mb-0">{shop}</h5>'
                '<button type="button" class="close" data-dismiss="modal"><span>&times;</span></button></div>'
                f'<div class="modal-body"><h6>{title}</h6><p class="mb-1">'
                f'<span class="badge badge-info">{category}</span> {qty_badge}</p>'
                f'<p class="text-muted mb-0">{detail}</p></div>'
                '<div class="modal-footer"><button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button></div>'
                "</div></div></div>"
            )
        mine_id = int(group.get("my_requirement_id") or 0)
        close_form = ""
        if mine_id:
            close_form = (
                f'<form class="adminlte-form" action="/api/vendor/requirements/{mine_id}/close" method="post" '
                "onsubmit=\"return confirm('Close your post for this requirement?');\">"
                '<button type="submit" class="btn btn-sm btn-outline-secondary">Close mine</button></form>'
            )
        qty = group.get("quantity")
        qty_badge = (
            f'<span class="badge badge-secondary">Qty: {html.escape(_text(qty))}</span>'
            if qty not in (None, "", 0)
            else ""
        )
        count = int(group.get("count") or 0)
        count_badge = f'<span class="badge badge-warning">{count} vendors</span>' if count > 1 else ""
        description = _clip(group.get("description"))
        cards.append(
            '<div class="card mb-3"><div class="card-body">'
            '<div class="d-flex justify-content-between align-items-start"><div>'
            f'<h5 class="mb-1">{html.escape(_text(group.get("title")))}</h5>'
            f'<span class="badge badge-info">{category}</span> {qty_badge} {count_badge}'
            "</div>"
            f"{close_form}</div>"
            + (f'<p class="mt-2 mb-2 text-muted">{html.escape(description)}</p>' if description else "")
            + '<div class="mt-2"><strong class="small text-uppercase text-muted">Vendors looking for this:</strong>'
            f'<div class="mt-1">{"".join(shops)}</div></div></div></div>'
        )
    posts = []
    payload, status = mine(vendor_id)
    for item in (payload.get("data") or []) if status == 200 else []:
        item_id = int(item.get("id") or 0)
        active = _text(item.get("status")) == "active"
        close_form = ""
        if active:
            close_form = (
                f'<form class="adminlte-form d-inline" action="/api/vendor/requirements/{item_id}/close" method="post">'
                '<button type="submit" class="btn btn-sm btn-outline-secondary">Close</button></form> '
            )
        posts.append(
            '<li class="list-group-item">'
            f'<div class="font-weight-bold">{html.escape(_text(item.get("title")))}</div>'
            f'<small class="text-muted">{html.escape(_text(item.get("category")))} · '
            f'{html.escape(_text(item.get("status")).capitalize())}</small>'
            '<div class="mt-2">'
            + close_form
            + f'<form class="adminlte-form d-inline" action="/api/vendor/requirements/{item_id}/delete" method="post" '
            "onsubmit=\"return confirm('Delete this requirement?');\">"
            '<button type="submit" class="btn btn-sm btn-outline-danger">Delete</button></form>'
            "</div></li>"
        )
    board_cards = "".join(cards) or (
        '<div class="card"><div class="card-body text-center text-muted">'
        "No active requirements yet. Be the first to post one.</div></div>"
    )
    mine_items = "".join(posts) or '<li class="list-group-item text-muted">You have not posted any requirements.</li>'
    body = f"""
<div class="d-flex justify-content-between align-items-center mb-3">
<h3 class="mb-0">Requirements</h3>
<a href="/api/vendor/requirements/create" class="btn btn-primary"><i class="fas fa-plus"></i> Post Requirement</a>
</div>
{_alert(_text(req.args.get("notice")), _text(req.args.get("error")))}
<p class="text-muted">Similar product needs are grouped into one card (same brand/series/model). Different models stay separate.</p>
<div class="row">
<div class="col-lg-8">
<h5 class="mb-3">Active board</h5>
{board_cards}
</div>
<div class="col-lg-4">
<h5 class="mb-3">My posts</h5>
<div class="card"><div class="card-body p-0"><ul class="list-group list-group-flush">{mine_items}</ul></div></div>
</div>
</div>
"""
    return _page("Requirements", body, wide=True)


def _categories(selected: str) -> str:
    options = ['<option value="">Select Category</option>']
    for name in CATEGORIES:
        mark = " selected" if name == selected else ""
        options.append(f'<option value="{html.escape(name)}"{mark}>{html.escape(name)}</option>')
    return "".join(options)


def _create_form(req, error: str = "") -> str:
    title = _text(req.form.get("title") if req.form else "")
    category = _text(req.form.get("category") if req.form else "")
    quantity = _text(req.form.get("quantity") if req.form else "")
    description = _text(req.form.get("description") if req.form else "")
    return f"""
<div class="d-flex justify-content-between align-items-center mb-3">
<h3 class="mb-0">Post Requirement</h3>
<a href="/api/vendor/requirements" class="btn btn-secondary">Back to board</a>
</div>
{_alert(error=error)}
<div class="card"><div class="card-body">
<form class="adminlte-form" action="/api/vendor/requirements" method="post">
<div class="form-group">
<label>Title <span class="text-danger">*</span></label>
<input type="text" name="title" class="form-control" maxlength="200" required value="{html.escape(title)}" placeholder="e.g. Need 50 Dell laptops">
<small class="form-text text-muted">Vendors with the same title (ignoring case) appear on one card.</small>
</div>
<div class="form-group">
<label>Category <span class="text-danger">*</span></label>
<select name="category" class="form-control" required>{_categories(category)}</select>
</div>
<div class="form-group">
<label>Quantity (optional)</label>
<input type="number" name="quantity" class="form-control" min="1" value="{html.escape(quantity)}" placeholder="e.g. 50">
</div>
<div class="form-group">
<label>Description (optional)</label>
<textarea name="description" class="form-control" rows="4" maxlength="2000" placeholder="Specs, budget, timeline...">{html.escape(description)}</textarea>
</div>
<button type="submit" class="btn btn-primary">Post Requirement</button>
</form>
</div></div>
"""


def create_page(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    return _page("Post Requirement", _create_form(req), wide=True)


def store(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    result, status = create(req, int(session.get("vendor_id") or 0))
    if status != 200:
        return _page("Post Requirement", _create_form(req, _text(result.get("message")) or "Could not post."), wide=True)
    notice = "Your requirement was updated." if "updated" in _text(result.get("message")).lower() else "Requirement posted successfully."
    return _redirect(_board(notice))


def close_post(req, requirement_id: int) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    result, status = close(req, requirement_id, int(session.get("vendor_id") or 0))
    if status != 200:
        return _redirect(_board(error=_text(result.get("message")) or "Requirement not found"))
    return _redirect(_board("Requirement closed."))


def remove_post(req, requirement_id: int) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    result, status = delete(req, requirement_id, int(session.get("vendor_id") or 0))
    if status != 200:
        return _redirect(_board(error=_text(result.get("message")) or "Requirement not found"))
    return _redirect(_board("Requirement deleted."))
