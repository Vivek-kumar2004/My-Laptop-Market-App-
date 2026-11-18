#!/usr/bin/env python3
"""Build website/sitemap.xml from public pages + live product listings."""

from __future__ import annotations

import json
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SITE = ROOT / "website"
OUT = SITE / "sitemap.xml"
PRODUCTS_URL = (
    "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api/web/products"
)
BASE = "https://mylaptopmarket.com"

# Public marketing / catalog pages. product.html is only listed with ?id=.
STATIC_PAGES = [
    ("/", "daily", "1.0"),
    ("/products.html", "daily", "0.9"),
    ("/market.html", "daily", "0.9"),
    ("/services.html", "weekly", "0.8"),
    ("/why-us.html", "weekly", "0.7"),
    ("/why-register.html", "weekly", "0.7"),
    ("/about.html", "monthly", "0.6"),
    ("/contact.html", "monthly", "0.6"),
    ("/blogs.html", "weekly", "0.6"),
    ("/privacy-policy.html", "yearly", "0.3"),
]


def fetch_product_ids() -> list[int]:
    req = urllib.request.Request(
        PRODUCTS_URL,
        headers={"User-Agent": "MyLaptopMarket-Sitemap/1.0"},
    )
    with urllib.request.urlopen(req, timeout=60) as resp:
        payload = json.loads(resp.read().decode("utf-8"))
    if not payload.get("status"):
        raise RuntimeError(f"products API returned status=false: {payload!r}")
    ids: list[int] = []
    for row in payload.get("data") or []:
        try:
            pid = int(row.get("id"))
        except (TypeError, ValueError):
            continue
        if pid > 0:
            ids.append(pid)
    # Newest first from API; keep stable unique order for diffs.
    seen: set[int] = set()
    unique: list[int] = []
    for pid in ids:
        if pid in seen:
            continue
        seen.add(pid)
        unique.append(pid)
    return unique


def add_url(
    parent: ET.Element,
    loc: str,
    *,
    lastmod: str,
    changefreq: str,
    priority: str,
) -> None:
    url = ET.SubElement(parent, "url")
    ET.SubElement(url, "loc").text = loc
    ET.SubElement(url, "lastmod").text = lastmod
    ET.SubElement(url, "changefreq").text = changefreq
    ET.SubElement(url, "priority").text = priority


def build_sitemap(product_ids: list[int]) -> bytes:
    today = date.today().isoformat()
    urlset = ET.Element("urlset")
    urlset.set("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9")

    for path, changefreq, priority in STATIC_PAGES:
        loc = BASE.rstrip("/") + path
        add_url(
            urlset,
            loc,
            lastmod=today,
            changefreq=changefreq,
            priority=priority,
        )

    for pid in product_ids:
        add_url(
            urlset,
            f"{BASE}/product.html?id={pid}",
            lastmod=today,
            changefreq="daily",
            priority="0.7",
        )

    tree = ET.ElementTree(urlset)
    ET.indent(tree, space="  ")
    # ElementTree writes encoding declaration when requested.
    from io import BytesIO

    buf = BytesIO()
    tree.write(buf, encoding="utf-8", xml_declaration=True)
    return buf.getvalue() + b"\n"


def main() -> int:
    if not SITE.is_dir():
        print("website folder is missing", file=sys.stderr)
        return 1
    try:
        product_ids = fetch_product_ids()
    except (urllib.error.URLError, TimeoutError, json.JSONDecodeError, RuntimeError) as exc:
        print(f"failed to fetch products for sitemap: {exc}", file=sys.stderr)
        return 1

    OUT.write_bytes(build_sitemap(product_ids))
    print(f"wrote {OUT} ({len(STATIC_PAGES)} pages + {len(product_ids)} products)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
