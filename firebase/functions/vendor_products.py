from __future__ import annotations

import html
from urllib.parse import quote

from firebase_admin import firestore
from flask import Response

from products_write import CATEGORIES, _delete_sheet_blob, _delete_stored_image, _file_name, _images_of, _uploads, read_stock_sheet
from products_write import add_product, delete_stock_sheet, update_product, upload_stock_sheet
from web_session import ASSET, _page, vendor_session

SITE = "https://mylaptopmarket.com/mylaptop"


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _own(vendor_id: int, product_id: int):
    doc = firestore.client().collection("products").document(str(product_id)).get()
    if not doc.exists:
        return None
    row = doc.to_dict() or {}
    if int(row.get("vendor_id") or 0) != vendor_id:
        return None
    row["_ref"] = doc.reference
    return row


def _redirect(location: str) -> Response:
    response = Response("", status=302)
    response.headers["Location"] = location
    return response


def _login() -> Response:
    return _redirect("login")


def product_list(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    client = firestore.client()
    rows = []
    for doc in client.collection("products").where("vendor_id", "==", vendor_id).stream():
        row = doc.to_dict() or {}
        row["id"] = int(row.get("id") or doc.id)
        rows.append(row)
    rows.sort(key=lambda row: row["id"], reverse=True)
    sheets = {int(doc.id) for doc in client.collection("stock_sheets").stream() if str(doc.id).isdigit()}
    notice = _text(req.args.get("notice"))
    error = _text(req.args.get("error"))
    body_rows = []
    for index, row in enumerate(rows, start=1):
        images = _images_of(row)
        image = _text(row.get("front_image")) or (images[0] if images else "")
        if image and not image.startswith("http"):
            image = f"{SITE}/assets/uploads/products/{image}"
        picture = (
            f'<img src="{html.escape(image)}" alt="Product Image" class="img-fluid img-thumbnail" style="max-height:80px;">'
            if image
            else '<span class="text-muted">No image</span>'
        )
        active = int(row.get("status") or 0) == 1
        status = (
            '<span class="badge badge-success">Active</span>'
            if active
            else '<span class="badge badge-danger">Deactive</span>'
        )
        toggle = (
            f'<a href="/api/vendor/product_deactivate/{row["id"]}" class="btn btn-sm btn-warning mb-1">Deactivate</a>'
            if active
            else f'<a href="/api/vendor/product_activate/{row["id"]}" class="btn btn-sm btn-success mb-1">Activate</a>'
        )
        sheet = (
            f'<a href="/api/vendor/stock-sheet/{row["id"]}" class="btn btn-sm btn-secondary mb-1">View sheet</a> '
            if row["id"] in sheets
            else ""
        )
        description = html.escape(_text(row.get("description")))
        body_rows.append(
            "<tr>"
            f"<td>{index}</td>"
            f"<td><strong>{html.escape(_text(row.get('product_name')))}</strong><br>"
            f'<small class="text-muted" style="display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;max-width:320px;">{description}</small></td>'
            f"<td>{html.escape(_text(row.get('category')) or 'N/A')}</td>"
            f"<td>{html.escape(_text(row.get('stock')))}</td>"
            f"<td>{html.escape(_text(row.get('price')))}</td>"
            f"<td>{status}</td>"
            f'<td style="width:140px;">{picture}</td>'
            f"<td>{sheet}{toggle} "
            f'<a href="/api/vendor/product_edit/{row["id"]}" class="btn btn-sm btn-info mb-1">Edit</a> '
            f'<a href="/api/vendor/product_delete/{row["id"]}" class="btn btn-sm btn-danger mb-1" '
            "onclick=\"return confirm('Are you sure you want to delete this product?')\">Delete</a></td>"
            "</tr>"
        )
    table = "".join(body_rows) or '<tr><td colspan="8" class="text-center">No products found. Add your first product.</td></tr>'
    body = f"""
<link rel="stylesheet" href="{ASSET}/plugins/datatables-bs4/css/dataTables.bootstrap4.min.css">
<div class="row mb-2">
<div class="col-sm-6"><h1>Product List</h1></div>
<div class="col-sm-6 text-right">
<a href="/api/vendor/add_product" class="btn btn-primary"><i class="fas fa-plus"></i> Add Product</a>
</div>
</div>
{_alert(notice, error)}
<div class="card">
<div class="card-header"><h3 class="card-title">Your Products</h3></div>
<div class="card-body table-responsive">
<table id="vendorTable" class="table table-bordered table-striped">
<thead><tr>
<th>S.No</th><th>Product</th><th>Category</th><th>Stock</th><th>Price</th><th>Status</th><th>Images</th><th>Actions</th>
</tr></thead>
<tbody>{table}</tbody>
</table>
</div>
</div>
"""
    scripts = f"""
<script src="{ASSET}/plugins/datatables/jquery.dataTables.min.js"></script>
<script src="{ASSET}/plugins/datatables-bs4/js/dataTables.bootstrap4.min.js"></script>
<script>
$(function () {{
  if ($.fn.DataTable) {{
    $("#vendorTable").DataTable({{
      responsive: false,
      autoWidth: false,
      pageLength: 10,
      order: []
    }});
  }}
}});
</script>
"""
    return _page("Product List", body, wide=True, extra_scripts=scripts)


def _set_status(req, product_id: int, status: int, notice: str) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    product = _own(int(session.get("vendor_id") or 0), product_id)
    if product is not None:
        product["_ref"].update({"status": status})
    return _redirect("../product_list?notice=" + quote(notice))


def activate(req, product_id: int) -> Response:
    return _set_status(req, product_id, 1, "Product activated.")


def deactivate(req, product_id: int) -> Response:
    return _set_status(req, product_id, 0, "Product deactivated.")


def _alert(notice: str = "", error: str = "") -> str:
    parts = []
    if notice:
        parts.append(
            '<div class="alert alert-success alert-dismissible fade show">'
            f'{html.escape(notice)}<button type="button" class="close" data-dismiss="alert">&times;</button></div>'
        )
    if error:
        parts.append(
            '<div class="alert alert-danger alert-dismissible fade show">'
            f'{html.escape(error)}<button type="button" class="close" data-dismiss="alert">&times;</button></div>'
        )
    return "".join(parts)


def _category_options(selected: str) -> str:
    options = ['<option value="">Select Category</option>']
    for category in CATEGORIES:
        mark = " selected" if category == selected else ""
        options.append(f'<option value="{html.escape(category)}"{mark}>{html.escape(category)}</option>')
    if selected and selected not in CATEGORIES:
        options.append(f'<option value="{html.escape(selected)}" selected>{html.escape(selected)} (current)</option>')
    return "".join(options)


def _picked(req, product: dict, name: str) -> str:
    if req.form and name in req.form:
        return _text(req.form.get(name))
    return _text(product.get(name))


def _product_form(req, product: dict | None, notice: str = "", error: str = "") -> str:
    product = product or {}
    product_id = int(product.get("id") or 0)
    editing = product_id > 0
    name = _picked(req, product, "product_name")
    category = _picked(req, product, "category")
    stock = _picked(req, product, "stock")
    video = _picked(req, product, "youtube_video")
    description = _picked(req, product, "description")
    price = _picked(req, product, "price")
    status = _picked(req, product, "status") or "1"
    action = f"/api/vendor/product_update/{product_id}" if editing else "/api/vendor/product_store"
    heading = "Edit Product" if editing else "Add Product"
    images = _images_of(product) if editing else []
    front = _text(product.get("front_image")) if editing else ""
    front_block = ""
    if front:
        front_src = front if front.startswith("http") else f"{SITE}/assets/uploads/products/{_file_name(front)}"
        front_block = (
            f'<div class="mb-2"><img src="{html.escape(front_src)}" class="img-fluid img-thumbnail" '
            'style="max-height:120px" alt="Front photo"></div>'
            '<div class="custom-control custom-checkbox mb-2">'
            '<input type="checkbox" class="custom-control-input" id="removeFront" name="remove_front_image" value="1">'
            '<label class="custom-control-label" for="removeFront">Remove front photo</label>'
            "</div>"
        )
    cards = []
    for image in images:
        filename = _file_name(image)
        src = image if image.startswith("http") else f"{SITE}/assets/uploads/products/{filename}"
        cards.append(
            '<div class="col-md-2 mb-2 position-relative">'
            f'<input type="hidden" name="existing_images" value="{html.escape(image)}">'
            '<div class="card card-outline card-secondary"><div class="card-body p-1">'
            f'<img src="{html.escape(src)}" class="img-fluid img-thumbnail" alt="Product Image">'
            "</div>"
            f'<a href="/api/vendor/product_image_delete/{product_id}/{html.escape(filename)}" '
            'class="btn btn-sm btn-danger position-absolute" style="top:5px;right:5px;z-index:10;" '
            "onclick=\"return confirm('Delete this image?')\"><i class=\"fas fa-trash\"></i></a>"
            "</div></div>"
        )
    existing = ""
    if cards:
        existing = (
            '<div class="mb-2 text-muted">Current images</div>'
            f'<div class="row">{"".join(cards)}</div>'
        )
    sheet = ""
    if editing:
        doc = firestore.client().collection("stock_sheets").document(str(product_id)).get()
        if doc.exists:
            current = html.escape(_text((doc.to_dict() or {}).get("original_name")) or "stock.xlsx")
            sheet = (
                f'<p class="text-muted mb-2">Current sheet: {current} '
                f'<a href="/api/vendor/stock-sheet/{product_id}" class="ml-2">View sheet</a></p>'
                '<div class="custom-control custom-checkbox mb-2">'
                '<input type="checkbox" class="custom-control-input" id="removeStockSheet" name="remove_stock_sheet" value="1">'
                '<label class="custom-control-label" for="removeStockSheet">Remove current sheet</label>'
                "</div>"
            )
    required = "" if editing else " required"
    image_hint = (
        "Leave blank to keep existing images. Upload new images to append."
        if editing
        else "You can select multiple images. JPG, PNG, WEBP, or HEIC, up to 5 MB each."
    )
    hint_class = "text-muted" if editing else "text-danger"
    active = " selected" if status == "1" else ""
    inactive = " selected" if status == "0" else ""
    submit = "Update Product" if editing else "Save Product"
    return f"""
<div class="row mb-2">
<div class="col-sm-6"><h1>{heading}</h1></div>
<div class="col-sm-6 text-right">
<a href="/api/vendor/product_list" class="btn btn-secondary"><i class="fas fa-list"></i> Product List</a>
</div>
</div>
{_alert(notice, error)}
<div class="card card-primary">
<div class="card-header"><h3 class="card-title">{heading}</h3></div>
<form class="adminlte-form" action="{action}" method="post" enctype="multipart/form-data">
<div class="card-body"><div class="row">
<div class="col-md-6"><div class="form-group">
<label>Product Name</label>
<input type="text" name="product_name" class="form-control" placeholder="Enter Product Name" required value="{html.escape(name)}">
</div></div>
<div class="col-md-6"><div class="form-group">
<label>Category</label>
<select name="category" class="form-control" required>{_category_options(category)}</select>
</div></div>
<div class="col-md-6"><div class="form-group">
<label>Stock Quantity</label>
<input type="number" name="stock" class="form-control" placeholder="Enter Stock Quantity" required value="{html.escape(stock)}">
</div></div>
<div class="col-md-12"><div class="form-group">
<label>YouTube Video Link (Optional)</label>
<input type="text" name="youtube_video" class="form-control" placeholder="https://youtube.com/..." value="{html.escape(video)}">
</div></div>
<div class="col-md-12"><div class="form-group">
<label>Description</label>
<textarea name="description" class="form-control" rows="5" placeholder="Enter Product Description" required>{html.escape(description)}</textarea>
</div></div>
<div class="col-md-12"><div class="form-group">
<label>Front photo (thumbnail)</label>
{front_block}
<input type="file" name="front_image" class="form-control" accept="image/*">
<small class="text-muted">Shown as the main photo on stock listings. One photo, JPG, PNG, WEBP, or HEIC, up to 5 MB.</small>
</div></div>
<div class="col-md-12"><div class="form-group">
<label>Product Images</label>
<input type="file" name="images" id="productImages" class="form-control" multiple accept="image/*"{required}>
<small class="{hint_class}">{image_hint}</small>
</div>
<div id="imagePreview" class="row"></div>
{existing}
</div>
<div class="col-md-12"><div class="form-group">
<label>Private stock sheet</label>
{sheet}
<input type="file" name="stock_sheet" class="form-control" accept=".xlsx,.xls,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet">
<small class="text-muted">Excel stays private. Other vendors can read the rows in the app, but the file cannot be downloaded or shared.</small>
</div></div>
<div class="col-md-6"><div class="form-group">
<label>Price</label>
<input type="number" name="price" class="form-control" placeholder="Enter Product Price" required value="{html.escape(price)}">
</div></div>
<div class="col-md-6"><div class="form-group">
<label>Status</label>
<select name="status" class="form-control">
<option value="1"{active}>Active</option>
<option value="0"{inactive}>Inactive</option>
</select>
</div></div>
</div></div>
<div class="card-footer">
<button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> {submit}</button>
</div>
</form>
</div>
<script>
document.addEventListener("DOMContentLoaded", function () {{
  var fileInput = document.getElementById("productImages");
  var preview = document.getElementById("imagePreview");
  if (!fileInput || !preview) return;
  function updatePreview() {{
    preview.innerHTML = "";
    Array.from(fileInput.files).forEach(function (file, index) {{
      var reader = new FileReader();
      reader.onload = function (event) {{
        var col = document.createElement("div");
        col.className = "col-md-2 mb-2 position-relative";
        var img = document.createElement("img");
        img.src = event.target.result;
        img.className = "img-fluid img-thumbnail";
        img.alt = file.name;
        var remove = document.createElement("button");
        remove.type = "button";
        remove.className = "btn btn-sm btn-danger position-absolute";
        remove.style.top = "5px";
        remove.style.right = "5px";
        remove.style.zIndex = "10";
        remove.innerHTML = '<i class="fas fa-trash"></i>';
        remove.addEventListener("click", function () {{
          var next = new DataTransfer();
          Array.from(fileInput.files).forEach(function (item, itemIndex) {{
            if (itemIndex !== index) next.items.add(item);
          }});
          fileInput.files = next.files;
          updatePreview();
        }});
        col.appendChild(img);
        col.appendChild(remove);
        preview.appendChild(col);
      }};
      reader.readAsDataURL(file);
    }});
  }}
  fileInput.addEventListener("change", updatePreview);
}});
</script>
"""


def add_form(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    notice = _text(req.args.get("notice"))
    error = _text(req.args.get("error"))
    return _page("Add Product", _product_form(req, None, notice, error), wide=True)


def _sheet_note(req, product_id: int, vendor_id: int) -> str:
    upload = req.files.get("stock_sheet") if req.files else None
    remove = req.form.get("remove_stock_sheet") if req.form else None
    note = ""
    if _text(remove) == "1":
        payload, status = delete_stock_sheet(req, product_id, vendor_id)
        if status != 200:
            note = _text(payload.get("message"))
    if upload is not None and upload.filename:
        payload, status = upload_stock_sheet(req, product_id, vendor_id)
        if status != 200:
            note = _text(payload.get("message"))
    return note


def store(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    category = _text(req.form.get("category") if req.form else "")
    if category not in CATEGORIES:
        return _page("Add Product", _product_form(req, None, error="Invalid category"), wide=True)
    if not _uploads(req):
        return _page("Add Product", _product_form(req, None, error="Product images are required."), wide=True)
    payload, status = add_product(req, vendor_id)
    if status != 200:
        return _page("Add Product", _product_form(req, None, error=_text(payload.get("message"))), wide=True)
    product_id = int((payload.get("data") or {}).get("id") or 0)
    note = _sheet_note(req, product_id, vendor_id)
    notice = "Product added successfully"
    if note:
        notice = f"{notice}. Excel sheet was not saved: {note}"
    return _redirect(f"product_edit/{product_id}?notice=" + quote(notice))


def edit_form(req, product_id: int) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    product = _own(int(session.get("vendor_id") or 0), product_id)
    if product is None:
        return _page("Edit Product", _alert(error="Product not found.") + '<p><a href="../product_list">Product list</a></p>')
    return _page(
        "Edit Product",
        _product_form(req, product, _text(req.args.get("notice")), _text(req.args.get("error"))),
        wide=True,
    )


def save(req, product_id: int) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    product = _own(vendor_id, product_id)
    if product is None:
        return _page("Edit Product", _alert(error="Product not found.") + '<p><a href="../product_list">Product list</a></p>')
    payload, status = update_product(req, product_id, vendor_id)
    if status != 200:
        return _page("Edit Product", _product_form(req, product, error=_text(payload.get("message"))), wide=True)
    note = _sheet_note(req, product_id, vendor_id)
    notice = "Product updated successfully"
    if note:
        notice = f"{notice}. Excel sheet was not saved: {note}"
    return _redirect(f"../product_edit/{product_id}?notice=" + quote(notice))


def delete_image(req, product_id: int, image_name: str) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    vendor_id = int(session.get("vendor_id") or 0)
    product = _own(vendor_id, product_id)
    if product is None:
        return _redirect("../../product_list?error=" + quote("Product not found"))
    removed = None
    kept = []
    for item in _images_of(product):
        if removed is None and _file_name(item) == image_name:
            removed = item
        else:
            kept.append(item)
    if removed is None:
        return _redirect(f"../../product_edit/{product_id}?error=" + quote("Image not found"))
    _delete_stored_image(removed)
    product["_ref"].update({"images": kept})
    return _redirect(f"../../product_edit/{product_id}?notice=" + quote("Image deleted successfully"))


def stock_sheet_page(req, product_id: int) -> Response:
    session = vendor_session(req)
    if session is None:
        return _redirect("/api/vendor/login")
    payload, status = read_stock_sheet(product_id, int(session.get("vendor_id") or 0))
    if status == 401:
        return _redirect("/api/vendor/login")
    product = firestore.client().collection("products").document(str(product_id)).get()
    name = _text((product.to_dict() or {}).get("product_name")) if product.exists else "Listing"
    back = '<p><a href="/api/vendor/search_products">Back</a></p>'
    if status != 200:
        message = _text(payload.get("message")) or "No stock sheet on this listing."
        return _page("Stock sheet", back + _alert(error=message) + f"<h2>{html.escape(name or 'Listing')}</h2>", wide=True)
    headers = payload.get("headers") or []
    rows = payload.get("rows") or []
    head = "".join(f"<th>{html.escape(_text(cell))}</th>" for cell in headers)
    body_rows = []
    for row in rows:
        cells = "".join(f"<td>{html.escape(_text(cell))}</td>" for cell in row)
        body_rows.append(f"<tr>{cells}</tr>")
    if not body_rows:
        span = max(1, len(headers))
        body_rows.append(f'<tr><td colspan="{span}">This sheet has no data rows.</td></tr>')
    note = "Rows can be read here. The Excel file cannot be downloaded or shared."
    viewed = _text(payload.get("viewed_at"))
    if viewed:
        note += f" Viewed {viewed}."
    original = _text(payload.get("original_name"))
    truncated = "<p>Showing the first rows and columns only.</p>" if payload.get("truncated") else ""
    body = f"""
{back}
<h2>Stock sheet</h2>
<h3>{html.escape(name or "Listing")}</h3>
<p><b>My Laptop Market</b></p>
{f"<p>{html.escape(original)}</p>" if original else ""}
<p>{html.escape(note)}</p>
{truncated}
<table>
<tr>{head}</tr>
{"".join(body_rows)}
</table>
"""
    return _page("Stock sheet", body, wide=True)


def remove(req, product_id: int) -> Response:
    session = vendor_session(req)
    if session is None:
        return _login()
    product = _own(int(session.get("vendor_id") or 0), product_id)
    if product is not None:
        for item in _images_of(product):
            _delete_stored_image(item)
        _delete_stored_image(_text(product.get("front_image")))
        _delete_sheet_blob(product_id)
        product["_ref"].delete()
    return _redirect("../product_list?notice=" + quote("Product deleted."))
