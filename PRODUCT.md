# Product

<!-- impeccable:product-schema 1 -->

## Platform

adaptive

## Stack

- Marketing site: static HTML/CSS/JS in `website/` (CDN publish from that folder)
- Mobile: Kotlin Multiplatform Compose in `composeApp/` with Android (`app/`) and iOS (`iosApp/`) shells
- Backend/auth: existing API and Firebase — out of scope for UI redesign unless a screen requires a layout-only change

## Users

Primary: Indian B2B IT vendors, refurbished hardware dealers, distributors, wholesalers, and technology suppliers who need nationwide visibility, vendor discovery, and qualified leads.

Secondary: buyers/sourcing teams looking for stock, requirements, and trusted dealers — mainly through the apps and marketplace listings.

## Product Purpose

My Laptop Market is a B2B networking and lead-generation platform for India’s IT hardware trade — not a consumer retail storefront. Success means verified businesses can list stock, find partners, post requirements, and convert connections into deals faster than closed WhatsApp circles or general classifieds.

## Positioning

Purpose-built for refurbished and enterprise IT hardware vendors (laptops, desktops, servers, networking, accessories, ITAD). Credibility and verification matter more than flashy consumer e‑commerce patterns.

## Brand Commitments

- Name: **My Laptop Market**
- Logo: `website/images/mylaptoplogo.png` / app icon asset — keep recognizable; do not invent a new mark without approval
- Domain/public site: mylaptopmarket.com
- Tone: professional, direct, trade-floor clear — avoid hype, emoji clutter, and consumer “deal of the day” voice

## Constraints

- Do not change backend, API contracts, or auth flows for UI work
- Do not commit secrets (`.env`, keystore, `AIConfig`, Firebase private configs)
- Accessibility: readable contrast, focusable controls on web, Compose semantics, respect reduced motion
- Prefer extending existing tokens/components over inventing a parallel system per page

## Evidence (repo-backed; verify before marketing claims)

Homepage currently cites figures such as registered businesses, cities, and categories. Treat as **existing marketing copy** — do not invent new metrics; confirm with the owner before changing numbers.

## Open Decisions

- Exact accent hex to standardize on (proposal lives in DESIGN.md: unify on site blue `#0767D8`)
- First implementation surface after DESIGN.md approval: website homepage **or** app Home
