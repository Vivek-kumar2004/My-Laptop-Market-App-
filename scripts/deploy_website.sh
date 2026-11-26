#!/usr/bin/env bash
# Build the Vite marketing homepage (static export) and publish it together with
# the remaining static pages under website/ to the live Google Cloud bucket.
# Objects that are not in the publish folder are left in place.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
WEBSITE="$ROOT/website"
SITE_APP="$ROOT/site"
PUBLISH="$ROOT/.website-publish"
BUCKET="gs://mylaptopmarket-app-web"

if [[ ! -d "$WEBSITE" ]]; then
  echo "website folder is missing" >&2
  exit 1
fi

if [[ ! -d "$SITE_APP" ]]; then
  echo "site folder is missing" >&2
  exit 1
fi

# Refresh Google sitemap with current public product URLs before upload.
python3 "$ROOT/scripts/generate_sitemap.py"

# Build Vite static homepage (single-file index.html under site/out).
(
  cd "$SITE_APP"
  npm ci
  npm run build
)

if [[ ! -d "$SITE_APP/out" ]]; then
  echo "site/out missing after vite build" >&2
  exit 1
fi

rm -rf "$PUBLISH"
mkdir -p "$PUBLISH"

# Base: legacy static pages (about, products, etc.) and shared assets.
cp -a "$WEBSITE"/. "$PUBLISH"/

# Overlay: Vite homepage export replaces index.html and adds any public assets.
cp -a "$SITE_APP/out"/. "$PUBLISH"/

gcloud storage rsync "$PUBLISH" "$BUCKET" \
  --recursive \
  --cache-control="public, max-age=300"

while IFS= read -r -d '' file; do
  rel="${file#"$PUBLISH"/}"
  gcloud storage cp "$file" "$BUCKET/$rel" \
    --cache-control="no-cache, max-age=0, must-revalidate" \
    --content-type="text/html; charset=utf-8"
done < <(find "$PUBLISH" -type f -name '*.html' -print0)

gcloud compute url-maps invalidate-cdn-cache mlm-web-https \
  --path "/*" \
  --project=mylaptopmarket-app \
  --async

rm -rf "$PUBLISH"
