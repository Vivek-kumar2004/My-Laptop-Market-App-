#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
XCODE_APP="/Applications/Xcode.app"
PROJECT="$ROOT/iosApp/iosApp.xcodeproj"

echo "== MyLaptopMarket iOS setup =="

if [[ ! -d "$XCODE_APP" ]]; then
  echo "ERROR: Full Xcode not found at $XCODE_APP"
  echo "1) Open Mac App Store → install Xcode"
  echo "2) Open Xcode once, accept license"
  echo "3) Re-run: $0"
  open "macappstores://apps.apple.com/app/xcode/id497799835" || true
  exit 1
fi

if [[ "$(xcode-select -p 2>/dev/null || true)" != "$XCODE_APP/Contents/Developer" ]]; then
  echo "Switching active developer dir to Xcode (needs sudo)..."
  sudo xcode-select -s "$XCODE_APP/Contents/Developer"
fi

sudo xcodebuild -license accept 2>/dev/null || true
xcodebuild -runFirstLaunch 2>/dev/null || true

echo "Opening $PROJECT"
open "$PROJECT"

cat <<'EOF'

Next in Xcode:
1. Select target "iosApp" → Signing & Capabilities
2. Team: apna Apple ID (Add Account if needed)
3. Connect iPhone (cable) → Trust → Developer Mode ON
4. Top bar: choose your iPhone → Run (▶)

First launch on phone:
Settings → General → VPN & Device Management → Trust developer
EOF
