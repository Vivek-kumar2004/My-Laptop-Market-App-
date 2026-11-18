from __future__ import annotations

import json
import os
import re
import urllib.request

ACRONYMS = {"hp", "ibm", "ssd", "hdd", "ram", "tft", "lcd", "led"}


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def make_title_key(title: str) -> str:
    return re.sub(r"\s+", " ", title).strip().lower()


def _title_case(cluster_key: str, fallback: str) -> str:
    if cluster_key == "":
        return fallback.strip()
    parts = []
    for part in cluster_key.split(" "):
        if part == "":
            continue
        if re.fullmatch(r"(g|gen|i|ryzen|xeon)?\d+[a-z]*", part, flags=re.I) or part[:1].isdigit():
            parts.append(part.upper())
        elif part in ACRONYMS:
            parts.append(part.upper())
        else:
            parts.append(part[:1].upper() + part[1:])
    return " ".join(parts) or fallback.strip()


def resolve_rules(title: str, category: str = "", quantity: int | None = None) -> dict:
    raw = title.strip()
    normalized = raw.lower().replace("hewlett packard", "hp").replace("hewlett-packard", "hp")
    patterns = [
        r"\b(need|needs|needed|want|wanted|looking for|looking|require|required|requirement|buy|buying|purchase|urgent|asap)\b",
        r"\b(pcs?|pieces?|units?|nos?|quantity|qty)\b",
        r"\b\d+\s*(pcs?|pieces?|units?|nos?)?\b",
        r"\b(of|for|the|a|an|to|in|on|with|from|please|plz|kindly)\b",
        r"[^\w\s\-+/]",
    ]
    for pattern in patterns:
        normalized = re.sub(pattern, " ", normalized)
    normalized = normalized.replace("_", " ")
    if quantity is not None and quantity > 0:
        normalized = re.sub(rf"\b{quantity}\b", " ", normalized)
    normalized = re.sub(r"\s+", " ", normalized).strip()
    normalized = re.sub(r"\belite\s*book\b", "elitebook", normalized)
    normalized = re.sub(r"\bthink\s*pad\b", "thinkpad", normalized)
    normalized = re.sub(r"\blatit\s*ude\b", "latitude", normalized)
    if normalized == "":
        normalized = make_title_key(raw)
    return {
        "cluster_key": normalized,
        "canonical_title": _title_case(normalized, raw),
    }


def _gemini(title: str, category: str, quantity: int | None, description: str) -> dict | None:
    api_key = _text(os.environ.get("GEMINI_API_KEY"))
    if api_key == "":
        return None
    model = _text(os.environ.get("GEMINI_MODEL")) or "gemini-2.0-flash"
    prompt = (
        "You cluster B2B laptop/hardware purchase requirements by SAME PRODUCT IDENTITY only.\n\n"
        "Rules:\n"
        "- Same brand + series + model must share one cluster_key.\n"
        "- Different models MUST differ.\n"
        "- Ignore quantity words and punctuation in the key.\n"
        "- cluster_key: lowercase, spaces only, no punctuation.\n"
        "- canonical_title: clean display name.\n"
        "- confidence: 0 to 1.\n\n"
        'Return ONLY valid JSON: {"cluster_key":"...","canonical_title":"...","confidence":0.0}\n\n'
        f"title: {title}\ncategory: {category}\nquantity: {quantity}\ndescription: {description}\n"
    )
    url = "https://generativelanguage.googleapis.com/v1beta/models/" + urllib.request.quote(model) + ":generateContent"
    body = json.dumps(
        {
            "contents": [{"parts": [{"text": prompt}]}],
            "generationConfig": {"temperature": 0.1, "responseMimeType": "application/json"},
        }
    ).encode()
    request = urllib.request.Request(url, data=body, method="POST")
    request.add_header("x-goog-api-key", api_key)
    request.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            payload = json.loads(response.read().decode())
    except Exception:
        return None
    text = (((payload.get("candidates") or [{}])[0].get("content") or {}).get("parts") or [{}])[0].get("text")
    if not isinstance(text, str) or text.strip() == "":
        return None
    text = re.sub(r"^```(?:json)?\s*", "", text.strip(), flags=re.I)
    text = re.sub(r"\s*```$", "", text)
    try:
        parsed = json.loads(text)
    except json.JSONDecodeError:
        return None
    if not isinstance(parsed, dict) or not _text(parsed.get("cluster_key")):
        return None
    try:
        confidence = float(parsed.get("confidence") or 0.7)
    except (TypeError, ValueError):
        confidence = 0.7
    return {
        "cluster_key": _text(parsed.get("cluster_key")),
        "canonical_title": _text(parsed.get("canonical_title")),
        "confidence": confidence,
    }


def resolve(title: str, category: str = "", quantity: int | None = None, description: str = "") -> dict:
    rules = resolve_rules(title, category, quantity)
    ai = _gemini(title, category, quantity, description)
    if ai and ai["confidence"] >= 0.55 and ai["cluster_key"]:
        return {
            "cluster_key": make_title_key(ai["cluster_key"]),
            "canonical_title": ai["canonical_title"] or rules["canonical_title"],
            "source": "gemini",
            "confidence": ai["confidence"],
        }
    return {**rules, "source": "rules", "confidence": 1.0}


def group_key(row: dict) -> tuple[str, str]:
    raw_qty = row.get("quantity")
    quantity = int(raw_qty) if raw_qty not in (None, "") else None
    title = _text(row.get("title"))
    resolved = resolve_rules(title, _text(row.get("category")), quantity)
    key = resolved["cluster_key"] or _text(row.get("cluster_key")) or _text(row.get("title_key")) or make_title_key(title)
    display = resolved["canonical_title"] or _text(row.get("canonical_title")) or title
    return key, display
