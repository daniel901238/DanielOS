#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
node "$DIR/generate_v3_report.js"
cat "$DIR/../docs/TUNING_REPORT_V3.md"
