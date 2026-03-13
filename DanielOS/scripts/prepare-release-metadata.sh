#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"

if [ $# -lt 3 ]; then
  echo "Usage: bash scripts/prepare-release-metadata.sh <support_email> <website_url> <privacy_url>"
  echo "Example: bash scripts/prepare-release-metadata.sh support@example.com https://example.com https://example.com/privacy"
  exit 1
fi

SUPPORT_EMAIL="$1"
WEBSITE_URL="$2"
PRIVACY_URL="$3"

FILES=(
  "PRIVACY_POLICY.md"
  "PRIVACY_POLICY_KO.md"
  "PLAY_STORE_LISTING_DRAFT.md"
  "PLAY_STORE_LISTING_KO.md"
  "RELEASE_METADATA.md"
  "RELEASE_FINAL_CHECKLIST.md"
)

for f in "${FILES[@]}"; do
  [ -f "$f" ] || continue
  sed -i "s|support@danielos.app|$SUPPORT_EMAIL|g" "$f"
  sed -i "s|https://danielos.app/privacy|$PRIVACY_URL|g" "$f"
  sed -i "s|https://danielos.app|$WEBSITE_URL|g" "$f"
done

echo "[OK] Release metadata placeholders replaced"
echo "- support: $SUPPORT_EMAIL"
echo "- website: $WEBSITE_URL"
echo "- privacy: $PRIVACY_URL"
