# Landing Page Redesign Port Plan

> **For agentic workers:** Use executing-plans. Inline execution approved by Faiz.

**Goal:** Replace `site/` home with the Vite redesign from `laptop-market-landing-page-redesign.zip`, keep Next.js static export publish path, real WhatsApp `919718199976`.

## Files

- Replace: `site/app/page.tsx`, `site/app/globals.css`, `site/app/layout.tsx`
- Add under `site/components/landing/`: Header, Hero, Marketplace, NetworkSection, Vendors, InsightsFaq, Footer, Dialogs, Icon, motion
- Add: `site/lib/landing/{data,assets,cn}.ts`
- Copy assets to `site/public/brand` and `site/public/images` (product + lifestyle)
- Deps: `clsx`, `tailwind-merge`; fonts via `next/font` (Jakarta + Bricolage)
- Remove unused old home components after wiring (or leave unused — prefer delete if only used by old home)

## Tasks

1. Copy public assets + install deps
2. Port lib + CSS + layout fonts
3. Port all components with `use client` where interactive; fix WhatsApp
4. Wire `page.tsx` as client landing shell
5. `npm run build` in site; commit + push origin main
