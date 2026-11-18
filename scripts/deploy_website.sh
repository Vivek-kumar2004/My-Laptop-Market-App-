#!/usr/bin/env bash
# Publish website/ to the live Google Cloud bucket. Objects that are not in
# this folder are left in place, so an old file is not removed by a deploy.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SITE="$ROOT/website"
BUCKET="gs://mylaptopmarket-app-web"

if [[ ! -d "$SITE" ]]; then
  echo "website folder is missing" >&2
  exit 1
fi

gcloud storage rsync "$SITE" "$BUCKET" \
  --recursive \
  --cache-control="public, max-age=300"

while IFS= read -r -d '' file; do
  rel="${file#"$SITE"/}"
  gcloud storage cp "$file" "$BUCKET/$rel" \
    --cache-control="no-cache, max-age=0, must-revalidate" \
    --content-type="text/html; charset=utf-8"
done < <(find "$SITE" -type f -name '*.html' -print0)

gcloud compute url-maps invalidate-cdn-cache mlm-web-https \
  --path "/*" \
  --project=mylaptopmarket-app \
  --async
