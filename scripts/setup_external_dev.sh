#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

HOME_DIR="$HOME"
BASHRC="$HOME_DIR/.bashrc"

find_writable_external_base() {
  # 1) Prefer Termux storage symlink if present (survives UUID changes via termux-setup-storage)
  if [ -L "$HOME_DIR/storage/external-1" ]; then
    local p
    p="$(readlink -f "$HOME_DIR/storage/external-1" || true)"
    if [ -n "$p" ] && [ -d "$p" ] && [ -w "$p" ]; then
      printf '%s\n' "$p"
      return 0
    fi
  fi

  # 2) Try scoped app dir on detected SD mount UUIDs
  for m in /storage/*; do
    [ -d "$m" ] || continue
    b="$(basename "$m")"
    case "$b" in emulated|self|enc_emulated) continue ;; esac
    p="$m/Android/data/com.termux/files"
    if [ -d "$p" ] && [ -w "$p" ]; then
      printf '%s\n' "$p"
      return 0
    fi
  done

  return 1
}

EXT_BASE="$(find_writable_external_base || true)"
if [ -z "$EXT_BASE" ]; then
  echo "[ERROR] 외장 저장소 쓰기 가능한 경로를 찾지 못했습니다."
  echo "- Termux 권한 확인 후 termux-setup-storage 실행 필요"
  exit 1
fi

EXT_ROOT="$EXT_BASE/openclaw-dev"
mkdir -p "$EXT_ROOT"/{projects,caches/npm,caches/pip,caches/gradle,tmp}

# Stable links inside HOME (UUID 바뀌어도 이 링크만 유지)
ln -sfn "$EXT_ROOT" "$HOME_DIR/.external-dev"
ln -sfn "$HOME_DIR/.external-dev/projects" "$HOME_DIR/projects"

# Existing npm cache migration (safe, one-time)
if [ -d "$HOME_DIR/.npm" ] && [ ! -L "$HOME_DIR/.npm" ]; then
  mkdir -p "$HOME_DIR/.external-dev/caches/npm"
  cp -a "$HOME_DIR/.npm/." "$HOME_DIR/.external-dev/caches/npm/" 2>/dev/null || true
  mv "$HOME_DIR/.npm" "$HOME_DIR/.npm.backup.$(date +%Y%m%d-%H%M%S)"
  ln -s "$HOME_DIR/.external-dev/caches/npm" "$HOME_DIR/.npm"
fi

START='# >>> external-dev-storage >>>'
if ! grep -Fq "$START" "$BASHRC"; then
  cat >> "$BASHRC" <<'BASHRC_BLOCK'

# >>> external-dev-storage >>>
# Keep stable external dev path at ~/.external-dev even if SD UUID changes
_ext_base=""
if [ -L "$HOME/storage/external-1" ]; then
  _cand="$(readlink -f "$HOME/storage/external-1" 2>/dev/null || true)"
  if [ -n "$_cand" ] && [ -d "$_cand" ] && [ -w "$_cand" ]; then
    _ext_base="$_cand"
  fi
fi

if [ -z "$_ext_base" ]; then
  for m in /storage/*; do
    [ -d "$m" ] || continue
    b="$(basename "$m")"
    case "$b" in emulated|self|enc_emulated) continue;; esac
    _cand="$m/Android/data/com.termux/files"
    if [ -d "$_cand" ] && [ -w "$_cand" ]; then
      _ext_base="$_cand"
      break
    fi
  done
fi

if [ -n "$_ext_base" ]; then
  mkdir -p "$_ext_base/openclaw-dev"/{projects,caches/npm,caches/pip,caches/gradle,tmp}
  ln -sfn "$_ext_base/openclaw-dev" "$HOME/.external-dev"
  ln -sfn "$HOME/.external-dev/projects" "$HOME/projects"

  export NPM_CONFIG_CACHE="$HOME/.external-dev/caches/npm"
  export PIP_CACHE_DIR="$HOME/.external-dev/caches/pip"
  export GRADLE_USER_HOME="$HOME/.external-dev/caches/gradle"
fi
unset _ext_base _cand
# <<< external-dev-storage <<<
BASHRC_BLOCK
fi

echo "[OK] External dev storage configured"
echo "- external base: $EXT_BASE"
echo "- stable root: $HOME_DIR/.external-dev"
echo "- projects link: $HOME_DIR/projects"