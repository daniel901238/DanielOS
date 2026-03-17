#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"

echo "== DanielOS release readiness check =="

check_file() {
  local f="$1"
  if [ -f "$f" ]; then
    echo "[OK] $f"
  else
    echo "[MISSING] $f"
  fi
}

check_file LICENSE
check_file THIRD_PARTY_NOTICES.md
check_file PRIVACY_POLICY.md
check_file PRIVACY_POLICY_KO.md
check_file PLAY_STORE_LISTING_DRAFT.md
check_file PLAY_STORE_LISTING_KO.md
check_file RELEASE_FINAL_CHECKLIST.md

if [ -f local.properties ]; then
  echo "[OK] local.properties exists"
else
  echo "[WARN] local.properties missing (Android SDK path required)"
fi

if grep -RIn "support@danielos.app\|https://danielos.app" *.md docs/*.md >/dev/null 2>&1; then
  echo "[WARN] placeholder contact/domain still present"
else
  echo "[OK] no placeholder contact/domain detected"
fi

echo "\nDone."
