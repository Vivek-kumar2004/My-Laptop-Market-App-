import json
import re
import urllib.parse
import urllib.request

import bcrypt
from firebase_admin import firestore

from auth import _password_matches, vendor_payload
from products_write import _delete_sheet_blob, _delete_stored_image, _images_of, _save_image

STATES = [
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chhattisgarh",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
    "Andaman and Nicobar Islands",
    "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi",
    "Jammu and Kashmir",
    "Ladakh",
    "Lakshadweep",
    "Puducherry",
]
STATE_ALIASES = {
    "new delhi": "Delhi",
    "nct": "Delhi",
    "nct of delhi": "Delhi",
    "orissa": "Odisha",
    "tamilnadu": "Tamil Nadu",
    "up": "Uttar Pradesh",
    "u.p.": "Uttar Pradesh",
    "mp": "Madhya Pradesh",
    "m.p.": "Madhya Pradesh",
}
GST_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _field(req, name: str) -> str:
    if req is None:
        return ""
    if getattr(req, "form", None) and name in req.form:
        return _text(req.form.get(name))
    body = req.get_json(silent=True) or {}
    if name in body and body.get(name) is not None:
        return _text(body.get(name))
    return ""


def _body(req) -> dict:
    if req.form:
        return {key: req.form.get(key) for key in req.form}
    return req.get_json(silent=True) or {}


def _vendors():
    return list(firestore.client().collection("vendors").stream())


def _normalize_state(value: str) -> str:
    raw = _text(value).lower()
    if raw == "":
        return ""
    for name in STATES:
        if name.lower() == raw:
            return name
    return STATE_ALIASES.get(raw, "")


def _normalize_gstin(value: str) -> str:
    return re.sub(r"[^0-9A-Za-z]", "", value).upper()


def _gst_checksum(first14: str) -> str:
    total = 0
    for index, char in enumerate(first14):
        code = GST_CHARS.find(char)
        if code < 0:
            return ""
        product = code * (1 if index % 2 == 0 else 2)
        total += product // 36 + (product % 36)
    return GST_CHARS[(36 - (total % 36)) % 36]


def _valid_gstin(gstin: str) -> bool:
    if not re.fullmatch(r"[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]", gstin):
        return False
    state = int(gstin[:2])
    if state < 1 or state > 38:
        return False
    return gstin[14] == _gst_checksum(gstin[:14])


def _next_vendor_id() -> int:
    client = firestore.client()
    ref = client.collection("settings").document("counters")
    transaction = client.transaction()

    @firestore.transactional
    def allocate(transaction):
        snap = ref.get(transaction=transaction)
        current = int((snap.to_dict() or {}).get("vendors") or 0)
        if current <= 0:
            for doc in client.collection("vendors").stream():
                current = max(current, int((doc.to_dict() or {}).get("id") or 0))
        nxt = current + 1
        transaction.set(ref, {"vendors": nxt}, merge=True)
        return nxt

    return allocate(transaction)


def _mobile_key(value: str) -> str:
    digits = re.sub(r"\D", "", _text(value))
    if len(digits) >= 12 and digits.startswith("91"):
        digits = digits[-10:]
    elif len(digits) > 10:
        digits = digits[-10:]
    return digits


def _shop_key(value: str) -> str:
    return re.sub(r"\s+", " ", _text(value)).casefold()


def _taken(field: str, value: str, ignore_id: int = 0) -> bool:
    if field == "mobile":
        needle = _mobile_key(value)
        if needle == "":
            return False

        def matches(row: dict) -> bool:
            return _mobile_key(row.get("mobile")) == needle
    elif field == "shop_name":
        needle = _shop_key(value)
        if needle == "":
            return False

        def matches(row: dict) -> bool:
            return _shop_key(row.get("shop_name")) == needle
    else:
        folded = field in {"email", "username"}
        needle = _text(value).lower() if folded else _text(value)
        if needle == "":
            return False

        def matches(row: dict) -> bool:
            current = _text(row.get(field))
            if folded:
                current = current.lower()
            return current == needle

    for doc in _vendors():
        row = doc.to_dict() or {}
        if int(row.get("id") or 0) == ignore_id:
            continue
        if matches(row):
            return True
    return False


def _identity_keys(username, mobile, email, shop) -> list[tuple[str, str, str]]:
    keys = []
    if _text(username):
        keys.append(("username", _text(username).lower(), "The username has already been taken."))
    if _mobile_key(mobile):
        keys.append(("mobile", _mobile_key(mobile), "The mobile has already been taken."))
    if _text(email):
        keys.append(("email", _text(email).lower(), "The email has already been taken."))
    if _shop_key(shop):
        keys.append(("shop", _shop_key(shop), "This shop name is already registered."))
    return keys


def _unique_ref(kind: str, key: str):
    import hashlib

    digest = hashlib.sha256(f"{kind}\n{key}".encode("utf-8")).hexdigest()[:40]
    return firestore.client().collection("vendor_uniques").document(f"{kind}_{digest}")


def _claim_unique(kind: str, key: str, vendor_id: int) -> str:
    from google.api_core.exceptions import AlreadyExists

    ref = _unique_ref(kind, key)
    try:
        ref.create({"vendor_id": int(vendor_id), "kind": kind})
        return "created"
    except AlreadyExists:
        owner = int((ref.get().to_dict() or {}).get("vendor_id") or 0)
        return "mine" if owner == int(vendor_id) else "taken"


def _release_unique(kind: str, key: str, vendor_id: int) -> None:
    if not key:
        return
    ref = _unique_ref(kind, key)
    snap = ref.get()
    if snap.exists and int((snap.to_dict() or {}).get("vendor_id") or 0) == int(vendor_id):
        ref.delete()


def hold_vendor_keys(vendor_id: int, username, mobile, email, shop) -> str | None:
    created = []
    for kind, key, message in _identity_keys(username, mobile, email, shop):
        state = _claim_unique(kind, key, vendor_id)
        if state == "created":
            created.append((kind, key))
        elif state == "taken":
            for made_kind, made_key in created:
                _release_unique(made_kind, made_key, vendor_id)
            return message
    return None


def replace_vendor_keys(vendor_id: int, previous: dict, username, mobile, email, shop) -> str | None:
    old_items = _identity_keys(
        previous.get("username"),
        previous.get("mobile"),
        previous.get("email"),
        previous.get("shop_name"),
    )
    old_map = {kind: key for kind, key, _message in old_items}
    created = []
    for kind, key, message in _identity_keys(username, mobile, email, shop):
        state = _claim_unique(kind, key, vendor_id)
        if state == "created":
            created.append((kind, key))
        elif state == "taken":
            for made_kind, made_key in created:
                _release_unique(made_kind, made_key, vendor_id)
            return message
        elif old_map.get(kind) == key:
            continue
    new_map = {kind: key for kind, key, _message in _identity_keys(username, mobile, email, shop)}
    for kind, key, _message in old_items:
        if key and key != new_map.get(kind):
            _release_unique(kind, key, vendor_id)
    return None


def release_vendor_keys(vendor: dict) -> None:
    vendor_id = int(vendor.get("id") or 0)
    for kind, key, _message in _identity_keys(
        vendor.get("username"),
        vendor.get("mobile"),
        vendor.get("email"),
        vendor.get("shop_name"),
    ):
        _release_unique(kind, key, vendor_id)


def slug_username(shop: str) -> str:
    raw = re.sub(r"[^a-zA-Z0-9]+", "_", _text(shop).lower())
    raw = re.sub(r"_+", "_", raw).strip("._")
    if len(raw) < 4:
        raw = (raw + "_shop").strip("._")
    if not raw:
        raw = "vendor"
    return raw[:20].strip("._") or "vendor"


def username_available(username: str = "", shop: str = "") -> dict:
    requested = _text(username)
    base = requested or slug_username(shop)
    if not re.fullmatch(r"[a-zA-Z0-9._]{4,20}", base):
        return {
            "ok": False,
            "available": False,
            "username": base,
            "message": "Use 4–20 letters, numbers, dots or underscores.",
        }
    if not _taken("username", base):
        return {"ok": True, "available": True, "username": base, "suggestion": base, "message": "Username is available."}
    for index in range(2, 100):
        suffix = str(index)
        stem = base[: max(1, 20 - len(suffix))].rstrip("._")
        candidate = (stem + suffix)[:20]
        if re.fullmatch(r"[a-zA-Z0-9._]{4,20}", candidate) and not _taken("username", candidate):
            return {
                "ok": True,
                "available": False,
                "username": base,
                "suggestion": candidate,
                "message": "That username is taken. Try " + candidate + ".",
            }
    return {
        "ok": False,
        "available": False,
        "username": base,
        "message": "That username is taken. Pick another.",
    }


def check_gstin(gstin_raw: str) -> dict:
    raw = _text(gstin_raw)
    if raw == "":
        return {"ok": True, "empty": True, "verified": False, "message": ""}
    gstin = _normalize_gstin(raw)
    if not _valid_gstin(gstin):
        return {"ok": False, "empty": False, "verified": False, "gstin": gstin, "message": "Enter a valid 15-character GSTIN."}
    lookup = _lookup_gstin(gstin)
    if not lookup["ok"]:
        return {"ok": False, "empty": False, "verified": False, "gstin": gstin, "message": lookup["message"]}
    if not lookup["found"]:
        return {"ok": False, "empty": False, "verified": False, "gstin": gstin, "message": lookup["message"]}
    details = lookup["details"]
    active = _text(details.get("status")).lower() == "active"
    return {
        "ok": True,
        "empty": False,
        "verified": active,
        "gstin": gstin,
        "details": details,
        "message": "GST verified — builds dealer trust." if active else f"This GSTIN is {_text(details.get('status')) or 'inactive'}.",
    }


def create_google_vendor(email: str, firebase_uid: str, display_name: str = "") -> dict:
    email = _text(email).lower()
    if not re.fullmatch(r"[^@\s]+@[^@\s]+\.[^@\s]+", email):
        return {"success": False, "message": "This Google account has no email address."}
    if _taken("email", email):
        return {"success": False, "message": "The email has already been taken."}
    seed = _text(display_name) or email.split("@")[0] or "vendor"
    check = username_available("", seed)
    username = _text(check.get("suggestion") or check.get("username") or slug_username(seed))
    if not re.fullmatch(r"[a-zA-Z0-9._]{4,20}", username):
        username = (slug_username(seed) + "shop")[:20]
    vendor_id = _next_vendor_id()
    held = hold_vendor_keys(vendor_id, username, "", email, "")
    if held:
        return {"success": False, "message": held}
    vendor = {
        "id": vendor_id,
        "username": username,
        "password": "",
        "password_plain": "",
        "shop_name": "",
        "logo": None,
        "logo_url": None,
        "mobile": "",
        "mobile_verified": False,
        "email": email,
        "email_verified": True,
        "status": 0,
        "location": "",
        "subscription_start": None,
        "subscription_end": None,
        "state": "",
        "gstin": "",
        "gst_verified": 0,
        "gst_details": None,
        "shop_photo": None,
        "admin_warning": "",
        "firebase_uid": _text(firebase_uid),
        "auth_provider": "google",
        "profile_complete": False,
    }
    firestore.client().collection("vendors").document(str(vendor_id)).set(vendor)
    return {"success": True, "vendor_id": vendor_id, "vendor_row": vendor, "vendor": vendor_payload(vendor)}


def complete_google_profile(vendor_id: int, body: dict) -> dict:
    vendor = _load(int(vendor_id))
    if vendor is None:
        return {"success": False, "message": "Sign in with Google again."}
    if vendor.get("profile_complete") is True and _text(vendor.get("shop_name")) and _mobile_key(vendor.get("mobile")):
        return {"success": True, "message": "Profile already complete.", "vendor_id": vendor_id, "vendor": vendor_payload(vendor)}

    shop = _text(body.get("shop_name"))
    username = _text(body.get("username")) or _text(vendor.get("username"))
    city = _text(body.get("city"))
    state = _normalize_state(body.get("state"))
    mobile = _mobile_key(body.get("mobile"))
    password = body.get("password") or ""
    gstin_raw = _text(body.get("gstin"))
    if shop == "" or username == "" or city == "" or mobile == "":
        return {"success": False, "message": "Fill shop name, username, city, state and mobile."}
    if state == "":
        return {"success": False, "message": "Select a valid state."}
    if not re.fullmatch(r"[0-9]{10}", mobile):
        return {"success": False, "message": "Enter a 10-digit mobile number."}
    if not re.fullmatch(r"[a-zA-Z0-9._]{4,20}", username):
        return {"success": False, "message": "Username must be 4–20 letters, numbers, dots or underscores."}
    if password and len(password) < 6:
        return {"success": False, "message": "Password must be at least 6 characters."}
    if _taken("username", username, int(vendor_id)):
        return {"success": False, "message": "The username has already been taken."}
    if _taken("mobile", mobile, int(vendor_id)):
        return {"success": False, "message": "The mobile has already been taken."}
    if _taken("shop_name", shop, int(vendor_id)):
        return {"success": False, "message": "This shop name is already registered."}

    from feature_flags import SHOW_VENDOR_OTP

    if SHOW_VENDOR_OTP:
        from phone_verify import consume as consume_mobile
        from phone_verify import ready as mobile_ready

        mobile_ok = mobile_ready(mobile, _text(body.get("mobile_otp")))
        if not mobile_ok.get("ok"):
            return {"success": False, "message": mobile_ok.get("message") or "Enter the OTP sent to your mobile."}
        mobile_done = consume_mobile(mobile, _text(body.get("mobile_otp")))
        if not mobile_done.get("ok"):
            return {"success": False, "message": mobile_done.get("message") or "Enter the OTP sent to your mobile."}

    gst_probe: dict = {
        "gstin": vendor.get("gstin") or "",
        "gst_verified": int(vendor.get("gst_verified") or 0),
        "gst_details": vendor.get("gst_details"),
    }
    if gstin_raw:
        gst_error = _apply_gst(gst_probe, gstin_raw)
        if gst_error:
            return {"success": False, "message": gst_error}

    previous = {
        "username": vendor.get("username"),
        "mobile": vendor.get("mobile"),
        "email": vendor.get("email"),
        "shop_name": vendor.get("shop_name"),
    }
    replaced = replace_vendor_keys(int(vendor_id), previous, username, mobile, vendor.get("email"), shop)
    if replaced:
        return {"success": False, "message": replaced}

    vendor["username"] = username
    vendor["shop_name"] = shop
    vendor["mobile"] = mobile
    vendor["mobile_verified"] = bool(SHOW_VENDOR_OTP)
    vendor["location"] = city
    vendor["state"] = state
    vendor["gstin"] = gst_probe.get("gstin") or ""
    vendor["gst_verified"] = int(gst_probe.get("gst_verified") or 0)
    vendor["gst_details"] = gst_probe.get("gst_details")
    vendor["profile_complete"] = True
    vendor["email_verified"] = True
    if password:
        vendor["password"] = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
        vendor["password_plain"] = password
    _save_vendor(vendor)
    try:
        from push_apns import notify_admins

        notify_admins(
            "New vendor registered",
            f"{shop} just signed up with Google and is waiting for approval.",
            "vendor_register",
        )
    except Exception:
        pass
    return {
        "success": True,
        "message": "Profile saved. Activate subscription to go live.",
        "vendor_id": int(vendor_id),
        "vendor": vendor_payload(vendor),
    }


def register(body: dict) -> dict:
    shop = _text(body.get("shop_name"))
    username = _text(body.get("username"))
    city = _text(body.get("city"))
    state = _normalize_state(body.get("state"))
    mobile = _mobile_key(body.get("mobile"))
    email = _text(body.get("email")).lower()
    password = body.get("password") or ""
    gstin_raw = _text(body.get("gstin"))
    if shop == "" or username == "" or city == "" or mobile == "" or email == "" or password == "":
        return {"success": False, "message": "Fill shop name, username, city, state, mobile, email and password."}
    if state == "":
        return {"success": False, "message": "Select a valid state."}
    if not re.fullmatch(r"[0-9]{10}", mobile):
        return {"success": False, "message": "Enter a 10-digit mobile number."}
    if not re.fullmatch(r"[^@\s]+@[^@\s]+\.[^@\s]+", email):
        return {"success": False, "message": "Enter a valid email address."}
    if not re.fullmatch(r"[a-zA-Z0-9._]{4,20}", username):
        return {"success": False, "message": "Username must be 4–20 letters, numbers, dots or underscores."}
    if len(password) < 6:
        return {"success": False, "message": "Password must be at least 6 characters."}
    if _taken("username", username):
        return {"success": False, "message": "The username has already been taken."}
    if _taken("mobile", mobile):
        return {"success": False, "message": "The mobile has already been taken."}
    if _taken("email", email):
        return {"success": False, "message": "The email has already been taken."}
    if _taken("shop_name", shop):
        return {"success": False, "message": "This shop name is already registered."}

    gst_probe: dict = {"gstin": "", "gst_verified": 0, "gst_details": None}
    if gstin_raw:
        gst_error = _apply_gst(gst_probe, gstin_raw)
        if gst_error:
            return {"success": False, "message": gst_error}

    from feature_flags import SHOW_VENDOR_OTP

    if SHOW_VENDOR_OTP:
        from email_verify import consume as consume_email
        from email_verify import ready as email_ready
        from phone_verify import consume as consume_mobile
        from phone_verify import ready as mobile_ready

        email_ok = email_ready(email, _text(body.get("email_otp")))
        if not email_ok.get("ok"):
            return {"success": False, "message": email_ok.get("message") or "Enter the OTP sent to your email."}
        mobile_ok = mobile_ready(mobile, _text(body.get("mobile_otp")))
        if not mobile_ok.get("ok"):
            return {"success": False, "message": mobile_ok.get("message") or "Enter the OTP sent to your mobile."}
        email_done = consume_email(email, _text(body.get("email_otp")))
        if not email_done.get("ok"):
            return {"success": False, "message": email_done.get("message") or "Enter the OTP sent to your email."}
        mobile_done = consume_mobile(mobile, _text(body.get("mobile_otp")))
        if not mobile_done.get("ok"):
            return {"success": False, "message": mobile_done.get("message") or "Enter the OTP sent to your mobile."}

    vendor_id = _next_vendor_id()
    held = hold_vendor_keys(vendor_id, username, mobile, email, shop)
    if held:
        return {"success": False, "message": held}
    hashed = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
    vendor = {
        "id": vendor_id,
        "username": username,
        "password": hashed,
        "password_plain": password,
        "shop_name": shop,
        "logo": None,
        "logo_url": None,
        "mobile": mobile,
        "mobile_verified": SHOW_VENDOR_OTP,
        "email": email,
        "email_verified": SHOW_VENDOR_OTP,
        "status": 0,
        "location": city,
        "subscription_start": None,
        "subscription_end": None,
        "state": state,
        "gstin": gst_probe.get("gstin") or "",
        "gst_verified": int(gst_probe.get("gst_verified") or 0),
        "gst_details": gst_probe.get("gst_details"),
        "shop_photo": None,
        "admin_warning": "",
        "profile_complete": True,
        "auth_provider": "password",
    }
    firestore.client().collection("vendors").document(str(vendor_id)).set(vendor)
    try:
        from push_apns import notify_admins

        notify_admins(
            "New vendor registered",
            f"{shop} just signed up and is waiting for approval.",
            "vendor_register",
        )
    except Exception:
        pass
    return {
        "success": True,
        "message": "Account ban gaya! Ab subscription activate karke seller verify karein.",
        "vendor_id": vendor_id,
        "vendor": vendor_payload(vendor),
    }


def _load(vendor_id: int):
    doc = firestore.client().collection("vendors").document(str(vendor_id)).get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}
    data["_ref"] = doc.reference
    return data


def _lookup_gstin(gstin: str):
    url = "https://razorpay.com/api/gstin/" + urllib.parse.quote(gstin)
    request = urllib.request.Request(
        url,
        headers={
            "Accept": "application/json, text/plain, */*",
            "User-Agent": "Mozilla/5.0",
            "Referer": "https://razorpay.com/gst-number-search/",
        },
    )
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            payload = json.loads(response.read().decode())
    except Exception:
        return {"ok": False, "found": False, "message": "GST check is busy. Try again in a minute."}
    raw = (((payload or {}).get("enrichment_details") or {}).get("online_provider") or {}).get("details")
    if not isinstance(raw, dict):
        blob = json.dumps(payload).lower()
        if "not able to fetch" in blob or "invalid gstin" in blob:
            return {"ok": True, "found": False, "message": "This GSTIN is not registered."}
        return {"ok": False, "found": False, "message": "GST check is busy. Try again in a minute."}

    def field(key):
        value = raw.get(key) or ""
        if isinstance(value, dict):
            value = value.get("value") or ""
        return _text(value)

    details = {
        "gstin": field("gstin") or gstin,
        "legal_name": field("legal_name"),
        "trade_name": field("trade_name"),
        "status": field("status"),
        "constitution": field("constitution"),
        "taxpayer_type": field("tax_payer_type"),
        "registration_date": field("registration_date"),
        "state_jurisdiction": field("state_jurisdiction"),
        "central_jurisdiction": field("central_jurisdiction"),
        "address": field("primary_address"),
    }
    if details["legal_name"] == "" and details["status"] == "":
        return {"ok": True, "found": False, "message": "This GSTIN is not registered."}
    return {"ok": True, "found": True, "message": "", "details": details}


def _apply_gst(vendor: dict, gstin_raw: str) -> str | None:
    raw = _text(gstin_raw)
    if raw == "":
        vendor["gstin"] = ""
        vendor["gst_verified"] = 0
        vendor["gst_details"] = None
        return None
    gstin = _normalize_gstin(raw)
    if not _valid_gstin(gstin):
        return "Enter a valid 15-character GSTIN."
    current = _normalize_gstin(_text(vendor.get("gstin")))
    details = vendor.get("gst_details") if current == gstin and isinstance(vendor.get("gst_details"), dict) else None
    if not details or not _text(details.get("legal_name")) or not _text(details.get("trade_name")):
        lookup = _lookup_gstin(gstin)
        if not lookup["ok"] or not lookup["found"]:
            return lookup["message"]
        details = lookup["details"]
    vendor["gstin"] = gstin
    vendor["gst_verified"] = 1 if _text(details.get("status")).lower() == "active" else 0
    vendor["gst_details"] = details
    return None


def _trust_message(vendor: dict) -> str:
    details = vendor.get("gst_details") if isinstance(vendor.get("gst_details"), dict) else {}
    status = _text(details.get("status"))
    trusted = int(vendor.get("gst_verified") or 0) == 1 and _text(vendor.get("shop_photo")) != ""
    if status and status.lower() != "active":
        return f"This GSTIN is {status}. Details are saved. The verified stamp needs an active GSTIN and a shop photo."
    if trusted:
        return "Shop verified. Your profile now has the verified stamp."
    if int(vendor.get("gst_verified") or 0) == 1:
        return "GSTIN is active. Add a shop photo to get the verified stamp."
    return "Saved. Add an active GSTIN and a shop photo to get the verified stamp."


def _save_vendor(vendor: dict) -> None:
    data = {key: value for key, value in vendor.items() if key != "_ref"}
    vendor["_ref"].set(data)


def profile_update(req, vendor_id: int | None = None) -> dict:
    vendor_raw = str(vendor_id) if vendor_id else _field(req, "vendor_id")
    shop = _field(req, "shop_name")
    if not vendor_raw.isdigit() or shop == "":
        return {"success": False, "message": "Validation failed"}
    vendor = _load(int(vendor_raw))
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}
    previous = {
        "username": vendor.get("username"),
        "mobile": vendor.get("mobile"),
        "email": vendor.get("email"),
        "shop_name": vendor.get("shop_name"),
    }
    mobile = _field(req, "mobile")
    if "mobile" in _body(req):
        if mobile != "" and not re.fullmatch(r"[0-9]{10}", mobile):
            return {"success": False, "message": "The mobile format is invalid."}
        if mobile != "" and _taken("mobile", mobile, int(vendor_raw)):
            return {"success": False, "message": "The mobile has already been taken."}
        if mobile != "":
            vendor["mobile"] = _mobile_key(mobile)
    city = _field(req, "city") or _field(req, "location")
    if city:
        vendor["location"] = city
    if _text(_body(req).get("state")):
        state = _normalize_state(_body(req).get("state"))
        if state == "":
            return {"success": False, "message": "Select a valid state."}
        vendor["state"] = state
    if "email" in _body(req):
        email = _field(req, "email").lower()
        if email and not re.fullmatch(r"[^@\s]+@[^@\s]+\.[^@\s]+", email):
            return {"success": False, "message": "Enter a valid email address."}
        if email and _taken("email", email, int(vendor_raw)):
            return {"success": False, "message": "This email is already used by another account."}
        vendor["email"] = email
    if "gstin" in _body(req):
        error = _apply_gst(vendor, _body(req).get("gstin"))
        if error:
            return {"success": False, "message": error}
    photo = req.files.get("shop_photo") if req.files else None
    if photo and photo.filename:
        url, error = _save_image(photo, "vendors")
        if error:
            return {"success": False, "message": error}
        _delete_stored_image(_text(vendor.get("shop_photo")))
        vendor["shop_photo"] = url
    logo = req.files.get("logo") if req.files else None
    remove_logo = _text(_body(req).get("remove_logo")) in {"1", "true", "on"}
    if logo and logo.filename:
        url, error = _save_image(logo, "vendors")
        if error:
            return {"success": False, "message": error}
        _delete_stored_image(_text(vendor.get("logo")))
        vendor["logo"] = url
        vendor["logo_url"] = url
    elif remove_logo and vendor.get("logo"):
        _delete_stored_image(_text(vendor.get("logo")))
        vendor["logo"] = None
        vendor["logo_url"] = None
    if _shop_key(shop) != _shop_key(previous.get("shop_name")) and _taken("shop_name", shop, int(vendor_raw)):
        return {"success": False, "message": "This shop name is already registered."}
    held = replace_vendor_keys(
        int(vendor_raw),
        previous,
        vendor.get("username"),
        vendor.get("mobile"),
        vendor.get("email"),
        shop,
    )
    if held:
        return {"success": False, "message": held}
    vendor["shop_name"] = shop
    _save_vendor(vendor)
    return {"success": True, "message": "Profile updated successfully", "vendor": vendor_payload(vendor)}


def shop_trust(req, vendor_id: int | None = None) -> dict:
    vendor_raw = str(vendor_id) if vendor_id else _field(req, "vendor_id")
    if not vendor_raw.isdigit():
        return {"success": False, "message": "Vendor not found"}
    vendor = _load(int(vendor_raw))
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}
    error = _apply_gst(vendor, _body(req).get("gstin"))
    if error:
        return {"success": False, "message": error}
    photo = req.files.get("shop_photo") if req.files else None
    if photo and photo.filename:
        url, error = _save_image(photo, "vendors")
        if error:
            return {"success": False, "message": error}
        _delete_stored_image(_text(vendor.get("shop_photo")))
        vendor["shop_photo"] = url
    _save_vendor(vendor)
    return {"success": True, "message": _trust_message(vendor), "vendor": vendor_payload(vendor)}


def delete_account(body: dict) -> tuple[dict, int]:
    vendor_raw = _text(body.get("vendor_id"))
    password = body.get("password") or ""
    if not vendor_raw.isdigit() or password == "":
        return {"success": False, "message": "Validation failed"}, 200
    vendor = _load(int(vendor_raw))
    if vendor is None:
        return {"success": False, "message": "Vendor not found"}, 404
    if not _password_matches(password, _text(vendor.get("password"))):
        return {"success": False, "message": "Incorrect password. Account was not deleted."}, 200

    client = firestore.client()
    vendor_id = int(vendor_raw)
    for doc in client.collection("products").where("vendor_id", "==", vendor_id).stream():
        product = doc.to_dict() or {}
        for image in _images_of(product):
            _delete_stored_image(image)
        _delete_stored_image(_text(product.get("front_image")))
        _delete_sheet_blob(int(product.get("id") or doc.id))
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
    return {"success": True, "message": f'Account "{label}" and related data were permanently deleted.'}, 200
