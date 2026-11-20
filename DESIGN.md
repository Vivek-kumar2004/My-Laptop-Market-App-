# Design

<!-- Direction: Trustworthy marketplace (approved) -->

## Intent

One calm, credible B2B brand across marketing site and apps. Product photography and listings lead; chrome stays quiet. Persuade on `website/`; Operate in Compose.

## Audience feel

A verified trade floor: clear, durable, easy to scan at desk or on the phone. Not a neon SaaS landing page, not a flashy consumer deal site, not an AI template.

## Color

| Role | Token | Value | Notes |
|------|--------|--------|--------|
| Brand blue | `--brand` / `BrandBlue` | `#0767D8` | Unify web + app (replace app `#2563EB`) |
| Brand blue dark | `--brand-dark` | `#0554B3` | Pressed / emphasis |
| Brand blue soft | `--brand-soft` | `#E8F1FC` | Soft fills, selected chips |
| Ink | `--ink` | `#0F172A` | Primary text |
| Ink muted | `--ink-muted` | `#475569` | Secondary text |
| Ink faint | `--ink-faint` | `#94A3B8` | Hints, placeholders |
| Canvas | `--canvas` | `#F7FAFC` | App + site page background |
| Surface | `--surface` | `#FFFFFF` | Cards, bars, sheets |
| Line | `--line` | `#E2E8F0` | Borders, dividers |
| Success | | `#059669` | Verified / in stock |
| Warning | | `#D97706` | Pending |
| Danger | | `#DC2626` | Errors |

**Do not:** purple/indigo accents, mint-aqua rainbow backgrounds, multi-hue “quick action” rainbows, neon glows, glassmorphism as the default card language.

**Aqua theme:** retire as primary identity. Teal may appear only as a rare data/status accent, not as page atmosphere.

## Typography

### Website

- Display / headings: **Outfit** (already loaded) — keep for continuity
- Body / UI: replace **Inter** with **Source Sans 3** (or **DM Sans** if Source Sans is blocked) — readable, neutral, not “AI default Inter”
- Hero brand lockup: wordmark or logo large enough that removing the nav still reads as My Laptop Market
- Scale (marketing): display ~40–56 / h2 ~28–36 / body 16–18 / small 13–14
- Line length: ~60–75ch for prose; avoid SEO paragraph walls in the first two screens

### Apps (Compose)

- Prefer a single custom font family when assets are added; until then tighten Material 3 type with clear weights (title ExtraBold, body regular) and do not introduce a second decorative face
- Avoid rainbow category tint systems; use brand blue + neutral + one status color

## Layout & spacing

- Base unit: **8px**; comfortable gaps 12 / 16 / 24 / 40
- Equal left/right content edges; max content width ~1120–1200px on marketing pages
- One job per section; first viewport on marketing: brand + one headline + one sentence + one CTA group + one dominant real product/atmosphere image
- App Home: greeting → primary actions (restrained) → listings; no decorative promo noise above the fold beyond one short trust line

## Components

- Buttons: solid brand blue, 10–12px radius (not endless pills everywhere); secondary = outline on `--line`
- Cards: white surface, 1px `--line`, soft shadow or none — not glass
- Chips/filters: soft brand fill when selected; neutral otherwise
- Icons: monochrome or brand-tinted; full-color only for real brand/store badges
- Imagery: real laptop/hardware photos and listing images; no abstract purple gradients as hero substitutes

## Motion

- Short, useful only: hover lift ~2px, press opacity, page enter fade/slide ≤300ms
- Respect `prefers-reduced-motion` / system reduced motion
- No auto-rotating hero copy wars; prefer one stable hero message (optional slow crossfade of photo only if it stays calm)

## Platform notes

- **Web (Persuade):** brand-first hero; strip glass/stat-card clutter; prove with live listings and vendors, not icon grids
- **Android (Operate):** Material 3 with brand color roles; edge-to-edge / nav bars consistent with existing shell
- **iOS (Operate):** same tokens via Compose; honor safe areas; keep native-feeling nav density; no web-landing patterns inside app chrome

## Do

- Match logo + `#0767D8` + Outfit headings across surfaces
- Lead with real listings, vendors, and hardware photos
- Keep copy short and trade-specific
- Extend `Color.kt` / shared CSS variables instead of one-off hexes

## Don’t

- Inter / Roboto / Arial as primary marketing body (app system UI font OK until custom font ships)
- Purple-on-white or purple→indigo gradients
- Generic card grids of features, pill clusters, floating badges, hero overlays/stickers
- Separate “aqua site” vs “electric app” identities
- Invent metrics, testimonials, or new product claims

## First surfaces (after this doc is approved)

1. `website/index.html` (+ related home CSS) — establish the public brand
2. App `HomeScreen` + `Color.kt` — align Operate UI to the same tokens
