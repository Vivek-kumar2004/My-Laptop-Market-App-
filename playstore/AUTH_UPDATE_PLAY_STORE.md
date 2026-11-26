# Play Store — Vendor Authentication Update (Website → App)

**Date:** 2026-11-25  
**Base URL:** `https://mylaptopmarket.com/api`  
**Live backend:** Cloud Function `api` (already deployed for website)  
**App module:** `composeApp` (Kotlin Multiplatform)

Yeh file sirf authentication-related website updates ko Play Store app me port karne ke liye hai. Iske bahar ke UI (Fresh stock cards, landing copy) app me zaroori nahi.

---

## 1) Kya website pe live hai (summary)

| Feature | Website | App (1.1.5 AAB) |
|---|---|---|
| Password login (username / email / mobile) | Yes | Yes |
| Google Sign-In — existing dealer | Yes | Yes |
| Google Sign-In — **naya** dealer → complete profile | Yes | Yes (`CompleteProfileScreen`) |
| Register: optional GSTIN + blue trust badge | Yes | Yes |
| Username auto-suggest from shop name + availability | Yes | Yes |
| Email OTP + Mobile OTP on register | Yes (flag) | Yes (`SHOW_VENDOR_OTP`) |
| Incomplete Google vendor dashboard block | Yes | Yes → Complete Profile |

**Play upload file:** `playstore/MyLaptopMarket-1.1.5.aab` (versionCode **15**, versionName **1.1.5**)

---

## 2) Critical bug fix (Google new user)

Aaj app deep link ke baad:

`POST /api/vendor/firebase-code` `{ "code": "..." }`

Agar response:

```json
{
  "success": true,
  "code": "needs_profile",
  "message": "Finish a few business details to continue.",
  "vendor_id": 123,
  "vendor": { "id": 123, "profile_complete": false, "...": "..." }
}
```

to **Home mat kholo**. Complete Profile screen kholo.

### Code change (required)

`ApiAuthRepository.LoginResponse.asSessionResult` aur `App.kt` deep-link handler:

1. Agar `code == "needs_profile"` (success true ho to bhi) → **session mat set karo as verified seller**.  
2. `CompleteProfile` route pe jao with `vendor_id`.  
3. Vendor object me `profile_complete: false` bhi check karo as safety net.

Same check password / status refresh pe: agar `vendor.profile_complete == false` → Complete Profile.

`VendorData` me field add karo:

```kotlin
@SerialName("profile_complete") val profileComplete: Boolean? = null
```

---

## 3) New API — Complete Google Profile (app JSON)

**Endpoint:** `POST /api/vendor/complete-profile`  
**Content-Type:** `application/json` (required — form posts go to the website HTML handler)

### Request body

```json
{
  "vendor_id": 123,
  "shop_name": "ABC Laptops",
  "username": "abc_laptops",
  "city": "Delhi",
  "state": "Delhi",
  "mobile": "9876543210",
  "mobile_otp": "123456",
  "password": "optional6+",
  "gstin": ""
}
```

| Field | Required | Notes |
|---|---|---|
| `vendor_id` | Yes | Google stub vendor id |
| `shop_name` | Yes | Unique |
| `username` | Yes | `^[a-zA-Z0-9._]{4,20}$` |
| `city` | Yes | Saved as `location` |
| `state` | Yes | Valid Indian state name (same list as register) |
| `mobile` | Yes | 10 digits, `6–9` start |
| `mobile_otp` | If `SHOW_VENDOR_OTP` | Same mobile OTP flow as register |
| `password` | No | Min 6 if provided (email/password login later) |
| `gstin` | No | Empty OK; if filled must verify |

### Success

```json
{
  "success": true,
  "message": "Profile saved. Activate subscription to go live.",
  "vendor_id": 123,
  "vendor": { "...": "..." }
}
```

Uske baad existing Seller Verification / subscription flow same rakho (`pending_verification` / status 0).

### Failure

```json
{ "success": false, "message": "..." }
```

---

## 4) Username suggest + availability (new helpers)

### Suggest from shop name

`GET /api/vendor/username-available?shop=ABC%20Laptops`

Response:

```json
{
  "ok": true,
  "available": true,
  "username": "abc_laptops",
  "suggestion": "abc_laptops",
  "message": "Username is available."
}
```

Agar taken:

```json
{
  "ok": true,
  "available": false,
  "username": "abc_laptops",
  "suggestion": "abc_laptops2",
  "message": "That username is taken. Try abc_laptops2."
}
```

### Check typed username

`GET /api/vendor/username-available?u=abc_laptops`  
(POST JSON bhi chalega: `{ "username": "..." }` ya `{ "shop_name": "..." }`)

**UI rule (website jaisa):**
- Shop name type → auto-fill username from `suggestion` (user edit kar sakta hai)
- Username field pe live availability + suggestion chip
- Register / Complete Profile dono screens pe

Slug rules (server): lowercase, non-alnum → `_`, length 4–20, max 20 chars.

---

## 5) Optional GST on Register + Complete Profile

### Check GST

`POST /api/vendor/gst-check`  
Body: `{ "gstin": "07AABCU9603R1ZM" }`  
(GET `?gstin=` bhi OK)

Empty GST:

```json
{ "ok": true, "empty": true, "verified": false, "message": "" }
```

Valid active:

```json
{
  "ok": true,
  "empty": false,
  "verified": true,
  "gstin": "07AABCU9603R1ZM",
  "details": { "legal_name": "...", "trade_name": "...", "status": "Active", "...": "..." },
  "message": "GST verified — builds dealer trust."
}
```

Invalid / inactive → `ok: false`, show `message`. **Register mat block karo empty GST pe.** Sirf filled + invalid pe block.

### Register body me GST add karo

Existing: `POST /api/vendor/register`

```json
{
  "shop_name": "...",
  "username": "...",
  "city": "...",
  "state": "...",
  "mobile": "...",
  "email": "...",
  "password": "...",
  "email_otp": "...",
  "mobile_otp": "...",
  "gstin": ""
}
```

`RegisterRequest` me `gstin: String = ""` add karo aur multipart/profile paths pe pehle se jo GST hai woh alag rehne do.

**UI:** optional GST field + jab verified ho to blue “GST verified — builds dealer trust” badge (website jaisa trust signal). Cards / purple glow mat use karo — existing Material theme follow karo.

---

## 6) Google flow (app) — exact sequence

### Existing dealer (profile complete)

1. App opens in-app browser:  
   `https://mylaptopmarket.com/vendor/google-auth?handoff=app`
2. Google success → redirect  
   `mylaptopmarket://auth?code=XXXX`
3. App: `POST /api/vendor/firebase-code` `{ "code": "XXXX" }`
4. `success: true`, **no** `needs_profile` → `SessionManager.login` → Home  
   OR `pending_verification` / `expired` → Seller Verification (existing)

### New dealer OR incomplete profile

1–3 same  
4. Response `code: "needs_profile"` → **Complete Profile screen** (`vendor_id` + email read-only from `vendor.email`)  
5. Mobile OTP (agar flag on): existing  
   - `POST /api/vendor/register/send-mobile-otp` (ya jo current mobile OTP endpoints hain)  
   - verify token / OTP same as RegisterScreen  
6. `POST /api/vendor/complete-profile` with fields above  
7. Success → Seller Verification / subscription (same as normal register)

**Do not** treat `success: true` alone as “logged in fully” when `code == needs_profile`.

---

## 7) Password login improvement (parity)

Server already: login identifier = username **OR** email **OR** 10-digit mobile.

App UI label: “Username, email or mobile” (website copy).  
Same `POST /api/vendor/login` `{ "username": "<any>", "password": "..." }`.

Google-only stub vendors often have **empty password** until they set one on Complete Profile — unhone password set kiya ho tab hi email/password login.

---

## 8) Files to touch in `composeApp` (checklist)

- [ ] `repository/ApiAuthRepository.kt`  
  - `RegisterRequest.gstin`  
  - `VendorData.profileComplete`  
  - `asSessionResult`: handle `needs_profile`  
  - `usernameAvailable(shop/username)`  
  - `gstCheck(gstin)`  
  - `completeProfile(...)` → `POST .../complete-profile`
- [ ] `App.kt` deep link Google handler → route to Complete Profile when needed
- [ ] New `CompleteProfileScreen.kt` (shop, username, city, state, mobile+OTP, optional password, optional GST)
- [ ] `RegisterScreen.kt` — GST optional + username suggest/availability
- [ ] `LoginScreen.kt` — username/email/mobile hint
- [ ] `AuthViewModel.kt` — wire APIs + navigation signals
- [ ] Nav graph — `complete_profile/{vendorId}` route; block Home if incomplete
- [ ] Version bump for Play upload (versionName / versionCode)

---

## 9) What NOT to change in this Play update

- Homepage / marketplace Fresh-stock 3-column layout (website-only)
- Static Vite `site/` Register-as-Dealer redirect (website-only)
- Admin panel
- iOS-only APNs paths unless shipping iOS same release
- Do not commit secrets: `keystore.properties`, `*.jks`, `KEYSTORE_BACKUP.txt`, Firebase web keys in app if already via BuildConfig

---

## 10) Play Console release notes (copy-paste)

**English (short):**
> Dealer sign-in update: Google sign-up for new dealers with a short business profile step, optional GST verification for trust, smarter username suggestions, and login with username, email, or mobile.

**Hindi (short):**
> डीलर साइन-इन अपडेट: नए डीलरों के लिए Google साइन-अप + बिज़नेस डिटेल्स, वैकल्पिक GST वेरिफिकेशन, यूज़रनेम सुझाव, और यूज़रनेम/ईमेल/मोबाइल से लॉगिन।

---

## 11) Test plan (no mistakes)

1. **Existing Google dealer** → deep link → Home (or pending verification). No Complete Profile.  
2. **Brand-new Google account** → deep link → Complete Profile (not Home).  
3. Complete Profile without GST → success.  
4. Complete Profile with bad GST → error, form stays.  
5. Complete Profile with valid GST → blue trust / `gst_verified`.  
6. Username taken → suggestion offered; using suggestion works.  
7. Register with optional empty GST still works.  
8. Login with mobile number + password works.  
9. Incomplete vendor cannot stay on Home after kill/reopen (status / session restore).  
10. After complete-profile, subscription / Seller Verification still works.

---

## 12) Backend endpoints reference (final)

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/vendor/login` | Password login |
| POST | `/api/vendor/firebase-code` | App Google handoff code → session / needs_profile |
| POST | `/api/vendor/register` | Email+password register (+ optional `gstin`) |
| POST | `/api/vendor/complete-profile` | Finish Google stub (`vendor_id` + business fields) |
| GET/POST | `/api/vendor/username-available` | `u` / `shop` |
| GET/POST | `/api/vendor/gst-check` | Optional GST verify |
| (existing) | mobile/email OTP routes | Unchanged |

Website HTML routes (`/vendor/login`, `/vendor/register`, `/vendor/complete-profile`) app se call mat karo — sirf JSON `/api/vendor/...` use karo.

---

## 13) Deploy note

- Website auth UI + Functions already live.  
- App ke liye `POST /api/vendor/complete-profile` JSON route is file ke saath code me add hua; **Functions redeploy** karna zaroori hai pehle app release se.  
- Static site GitHub Action Functions deploy nahi karti — `firebase deploy --only functions:api` (ya project `functions.yaml` flow).
