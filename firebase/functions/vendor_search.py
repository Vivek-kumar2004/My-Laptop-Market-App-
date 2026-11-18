from __future__ import annotations

import html
import re

from firebase_admin import firestore
from flask import Response

from catalog import list_products
from web_session import _page, vendor_session

SITE = "https://mylaptopmarket.com"
FALLBACK = "https://storage.googleapis.com/mylaptopmarket-app-web/mylaptop/assets/images/no-product.svg"


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _redirect(location: str) -> Response:
    response = Response("", status=302)
    response.headers["Location"] = location
    return response


def _words(value, limit: int = 45) -> str:
    parts = _text(value).split()
    if len(parts) <= limit:
        return " ".join(parts)
    return " ".join(parts[:limit]) + "..."


def _youtube_id(value: str) -> str:
    match = re.search(
        r"(?:youtube\.com/(?:watch\?v=|embed/|shorts/|v/)|youtu\.be/)([a-zA-Z0-9_-]{11})",
        _text(value),
    )
    return match.group(1) if match else ""


def _images(row: dict) -> list[str]:
    urls = row.get("image_urls") if isinstance(row.get("image_urls"), list) else row.get("images")
    if not isinstance(urls, list):
        return []
    return [_text(item) for item in urls if _text(item).startswith("http")]


def _card(row: dict, viewer: int, sheets: set[int]) -> str:
    product_id = int(row.get("id") or 0)
    vendor_id = int(row.get("vendor_id") or 0)
    name = html.escape(_text(row.get("product_name")) or "Product")
    shop = html.escape(_text(row.get("vendor_name")) or "Shop")
    location = html.escape(_text(row.get("vendor_location")) or "Location Not Available")
    phone = _text(row.get("vendor_number"))
    price = html.escape(_text(row.get("price")) or "0")
    description = html.escape(_words(row.get("description")))
    detail = f"{SITE}/product.html?id={product_id}"
    images = _images(row)
    main = html.escape(images[0] if images else FALLBACK)
    thumbs = "".join(
        f'<a href="{html.escape(src)}" target="_blank" rel="noopener">'
        f'<img src="{html.escape(src)}" class="jd-thumb" alt=""></a>'
        for src in images
    )
    logo = _text(row.get("vendor_logo_url"))
    seller = (
        f'<img src="{html.escape(logo)}" alt="" style="width:18px;height:18px;border-radius:50%;object-fit:cover;margin-right:4px;vertical-align:middle;">'
        if logo.startswith("http")
        else '<i class="fas fa-store mr-1"></i>'
    )
    stamp = ""
    if row.get("vendor_shop_trusted"):
        stamp = (
            ' <span style="display:inline-flex;align-items:center;gap:4px;background:#FFF7D6;color:#92400E;'
            'border:1px solid #E8B931;border-radius:999px;padding:2px 8px;font-size:11px;font-weight:700;">'
            '<i class="fas fa-check-circle"></i> Verified</span>'
        )
    try:
        stock_count = int(row.get("stock") or 0)
    except (TypeError, ValueError):
        stock_count = 0
    stock_text = f"{stock_count} In Stock" if stock_count > 0 else "Out Of Stock"
    stock_class = "stock-badge" if stock_count > 0 else "stock-badge out-stock"
    youtube = _youtube_id(row.get("youtube_video"))
    video = ""
    if youtube:
        video = (
            f'<button type="button" class="btn youtube-btn btn-block mb-3" onclick="playYouTubeVideo(\'{youtube}\')">'
            '<i class="fab fa-youtube mr-2"></i> Watch Product Video</button>'
        )
    call = ""
    if phone:
        call = (
            f'<a href="tel:{html.escape(phone)}" class="btn btn-success btn-block call-btn mb-3">'
            '<i class="fas fa-phone-alt mr-2"></i> Call Vendor</a>'
        )
    sheet = ""
    if product_id in sheets or row.get("has_stock_sheet"):
        sheet = (
            f'<a href="/api/vendor/stock-sheet/{product_id}" class="btn btn-secondary btn-block mb-3">'
            '<i class="fas fa-table mr-2"></i> View stock sheet</a>'
        )
    message = ""
    if vendor_id and vendor_id != viewer:
        message = (
            f'<a href="/api/vendor/messages?contact={vendor_id}&product={product_id}" class="btn btn-primary btn-block mb-3">'
            '<i class="fas fa-comment-dots mr-2"></i> Message Vendor</a>'
        )
    return f"""
<div class="jd-card mb-4" data-product-url="{detail}" role="link" tabindex="0">
<div class="card-body p-4"><div class="row">
<div class="col-lg-4">
<div class="image-wrapper">
<a href="{detail}" target="_blank" rel="noopener"><img src="{main}" class="jd-main-image" alt=""></a>
<div class="image-overlay">
<span class="shop-badge">{seller}{shop}{stamp}</span>
<span class="shop-badge1"><i class="fas fa-map-marker-alt"></i> {location}</span>
</div>
</div>
<div class="thumb-wrapper">{thumbs}</div>
</div>
<div class="col-lg-5">
<div class="product-title"><a href="{detail}" target="_blank" rel="noopener">{name}</a></div>
<div class="product-desc">{description}</div>
<div class="info-box">
<div class="info-item"><div class="info-label">Price</div><div class="info-value">Rs {price}</div></div>
<div class="info-item"><div class="info-label">Availability</div><div class="info-value">{html.escape(stock_text)}</div></div>
</div>
<a href="{detail}" target="_blank" rel="noopener" class="btn btn-outline-primary mt-4 view-btn"><i class="fas fa-eye mr-2"></i> View Full Details</a>
</div>
<div class="col-lg-3"><div class="right-panel">
{video}{call}{sheet}{message}
<div class="text-center mb-3"><span class="{stock_class}">{html.escape(stock_text)}</span></div>
<div class="text-center"><span class="status-active"><i class="fas fa-check-circle mr-1"></i> Active</span></div>
</div></div>
</div></div></div>
"""


def search_page(req) -> Response:
    session = vendor_session(req)
    if session is None:
        return _redirect("login")
    viewer = int(session.get("vendor_id") or 0)
    term = _text(req.args.get("search"))
    products, _matches = list_products(search=term, include_vendor=True, for_web=True)
    sheets = {int(doc.id) for doc in firestore.client().collection("stock_sheets").stream() if str(doc.id).isdigit()}
    cards = "".join(_card(row, viewer, sheets) for row in products)
    count = len(products)
    label = f"Showing {count} product" + ("" if count == 1 else "s")
    if term:
        label += f' for "<strong>{html.escape(term)}</strong>"'
    if count:
        results = f'<div class="mb-3 text-muted">{label}</div>{cards}'
    elif term:
        results = (
            '<div class="empty-box"><i class="fas fa-box-open"></i><h3>No Products Found</h3>'
            f'<p>We couldn\'t find any products matching "<strong>{html.escape(term)}</strong>".</p>'
            '<button onclick="clearSearch()" class="btn btn-primary mt-3">View All Products</button></div>'
        )
    else:
        results = (
            '<div class="empty-box"><i class="fas fa-box-open"></i><h3>No Products Available</h3>'
            "<p>There are no active products in the marketplace right now.</p></div>"
        )
    body = f"""
<style>
.search-card {{ border: none; border-radius: 22px; background: linear-gradient(135deg, #0f172a, #1e293b); box-shadow: 0 10px 35px rgba(0,0,0,.12); }}
.search-title {{ font-size: 34px; font-weight: 800; color: #fff; margin-bottom: 10px; }}
.search-sub {{ color: #cbd5e1; font-size: 15px; }}
.search-box {{ height: 58px; border-radius: 14px; border: none; padding-left: 20px; }}
.search-btn {{ height: 58px; border-radius: 14px; font-weight: 700; background: #ff6b00; border: none; color: #fff; }}
.search-btn:hover {{ background: #e85f00; color: #fff; }}
.jd-card {{ border: none; border-radius: 24px; overflow: hidden; background: #fff; box-shadow: 0 6px 30px rgba(15,23,42,.07); cursor: pointer; }}
.jd-card:hover {{ transform: translateY(-4px); box-shadow: 0 12px 40px rgba(0,0,0,.14); }}
.image-wrapper {{ position: relative; overflow: hidden; border-radius: 18px; }}
.jd-main-image {{ width: 100%; height: 320px; object-fit: cover; }}
.image-overlay {{ display: flex; position: absolute; bottom: 0; left: 0; right: 0; padding: 18px; background: linear-gradient(to top, rgba(0,0,0,.7), transparent); justify-content: space-between; gap: 8px; }}
.shop-badge, .shop-badge1 {{ background: #fff; color: #111; font-size: 13px; padding: 6px 14px; border-radius: 40px; font-weight: 700; }}
.thumb-wrapper {{ display: flex; flex-wrap: wrap; margin-top: 15px; }}
.jd-thumb {{ width: 72px; height: 72px; object-fit: cover; border-radius: 14px; margin: 0 10px 10px 0; border: 3px solid #fff; }}
.product-title {{ font-size: 28px; font-weight: 800; color: #111827; line-height: 1.3; }}
.product-title a {{ color: inherit; }}
.product-desc {{ color: #64748b; margin-top: 8px; }}
.info-box {{ display: flex; flex-wrap: wrap; gap: 12px; margin-top: 20px; }}
.info-item {{ background: #f8fafc; border-radius: 14px; padding: 14px 18px; min-width: 130px; border: 1px solid #e2e8f0; }}
.info-label {{ font-size: 12px; color: #64748b; }}
.info-value {{ font-size: 15px; font-weight: 700; }}
.right-panel {{ background: #f8fafc; border-radius: 20px; padding: 18px; border: 1px solid #e2e8f0; }}
.call-btn, .youtube-btn {{ height: 52px; border-radius: 14px; font-weight: 700; }}
.youtube-btn {{ background: #ff0000; color: #fff; border: none; }}
.stock-badge, .status-active {{ border-radius: 40px; padding: 10px 18px; display: inline-block; font-weight: 700; color: #fff; }}
.stock-badge {{ background: #2563eb; }}
.stock-badge.out-stock {{ background: #ef4444; }}
.status-active {{ background: #16a34a; }}
.empty-box {{ background: #fff; padding: 70px 20px; border-radius: 22px; text-align: center; }}
.empty-box i {{ font-size: 70px; color: #94a3b8; }}
@media (max-width: 768px) {{ .search-title {{ font-size: 26px; }} .product-title {{ font-size: 22px; }} .jd-main-image {{ height: 240px; }} }}
</style>
<div class="card search-card mb-4"><div class="card-body p-4 p-md-5"><div class="row align-items-center">
<div class="col-md-6 mb-4 mb-md-0">
<div class="search-title">Discover Best Vendor Products</div>
<div class="search-sub">Search products, explore images, videos and connect directly with vendors instantly.</div>
</div>
<div class="col-md-6">
<form class="adminlte-form" method="get" action="/api/vendor/search_products">
<div class="row">
<div class="col-md-8 mb-2"><input type="text" name="search" class="form-control search-box" placeholder="Search Product..." value="{html.escape(term)}"></div>
<div class="col-md-4 mb-2"><button type="submit" class="btn btn-block search-btn"><i class="fas fa-search mr-2"></i> Search</button></div>
</div>
</form>
</div>
</div></div></div>
{results}
<div class="modal fade" id="videoModal" tabindex="-1"><div class="modal-dialog modal-lg modal-dialog-centered"><div class="modal-content" style="background:transparent;border:none;"><div class="modal-body p-0">
<button type="button" class="close text-white" onclick="closeVideoModal()" style="position:absolute;right:8px;top:8px;z-index:2;font-size:32px;">&times;</button>
<div style="position:relative;padding-bottom:56.25%;height:0;"><iframe id="youtubePlayer" style="position:absolute;top:0;left:0;width:100%;height:100%;" src="" allow="autoplay; encrypted-media" allowfullscreen></iframe></div>
</div></div></div></div>
<script>
function playYouTubeVideo(videoId) {{
  if (!videoId) return;
  document.getElementById("youtubePlayer").src = "https://www.youtube.com/embed/" + videoId + "?autoplay=1&rel=0";
  var modal = document.getElementById("videoModal");
  if (window.jQuery) jQuery(modal).modal("show");
}}
function closeVideoModal() {{
  var modal = document.getElementById("videoModal");
  document.getElementById("youtubePlayer").src = "";
  if (window.jQuery) jQuery(modal).modal("hide");
}}
function clearSearch() {{ window.location.href = "/api/vendor/search_products"; }}
document.querySelectorAll(".jd-card[data-product-url]").forEach(function (card) {{
  card.addEventListener("click", function (event) {{
    if (event.target.closest("a, button, .modal")) return;
    var url = card.getAttribute("data-product-url");
    if (url) window.open(url, "_blank", "noopener");
  }});
}});
</script>
"""
    return _page("Search Products", body, wide=True)
