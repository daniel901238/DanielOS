#!/data/data/com.termux/files/usr/bin/bash
set -u
OUT_DIR="/data/data/com.termux/files/home/.openclaw/memory/reboot-evidence"
mkdir -p "$OUT_DIR"
TS="$(date +%Y%m%d-%H%M%S)"
OUT="$OUT_DIR/$TS.txt"

{
  echo "=== reboot evidence snapshot ==="
  echo "timestamp: $(date '+%F %T %Z')"
  echo "uname: $(uname -a)"
  echo "uptime: $(uptime 2>/dev/null || true)"
  echo "proc_uptime: $(cat /proc/uptime 2>/dev/null || true)"
  echo "bootreason(ro.boot.bootreason): $(getprop ro.boot.bootreason 2>/dev/null || true)"
  echo "bootreason(sys.boot.reason): $(getprop sys.boot.reason 2>/dev/null || true)"
  echo "firstboot(ro.runtime.firstboot): $(getprop ro.runtime.firstboot 2>/dev/null || true)"
  echo
  echo "--- dmesg tail (120) ---"
  dmesg 2>/dev/null | tail -n 120 || true
  echo
  echo "--- logcat reboot/shutdown related (tail 200) ---"
  logcat -d 2>/dev/null | grep -Ei 'shutdown|reboot|poweroff|watchdog|thermal|low battery|battery' | tail -n 200 || true
  echo
  echo "--- shell history tail ---"
  tail -n 100 /data/data/com.termux/files/home/.bash_history 2>/dev/null || true
} > "$OUT"

echo "saved: $OUT"
