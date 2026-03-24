#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

DATA_FILE="$PROJECT_ROOT/data/playtest_runs.txt"
CALC_SCRIPT="$SCRIPT_DIR/tuning_calc.js"

if [ ! -f "$DATA_FILE" ]; then
  echo "[오류] 데이터 파일이 없음: $DATA_FILE"
  exit 1
fi

if [ ! -f "$CALC_SCRIPT" ]; then
  echo "[오류] 계산 스크립트가 없음: $CALC_SCRIPT"
  exit 1
fi

echo "=== Project Relay Tuning Runner ==="
echo "Data: $DATA_FILE"
echo "Script: $CALC_SCRIPT"
echo

node "$CALC_SCRIPT" "../data/playtest_runs.txt"
