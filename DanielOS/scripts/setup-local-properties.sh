#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"

if [ $# -ge 1 ]; then
  SDK_DIR="$1"
else
  CANDIDATES=(
    "$HOME/Android/Sdk"
    "$HOME/android-sdk"
    "/sdcard/Android/Sdk"
    "/storage/emulated/0/Android/Sdk"
    "$HOME/.android-sdk"
  )
  SDK_DIR=""
  for p in "${CANDIDATES[@]}"; do
    if [ -d "$p/platforms" ] || [ -d "$p/build-tools" ]; then
      SDK_DIR="$p"
      break
    fi
  done
fi

if [ -z "${SDK_DIR:-}" ]; then
  echo "[ERROR] Android SDK 경로를 찾지 못했습니다."
  echo "사용법: bash scripts/setup-local-properties.sh /path/to/Android/Sdk"
  exit 1
fi

cat > local.properties <<EOF
sdk.dir=$SDK_DIR
EOF

echo "[OK] local.properties 생성 완료: $PROJECT_DIR/local.properties"
echo "sdk.dir=$SDK_DIR"
