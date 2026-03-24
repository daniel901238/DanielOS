export const COMPAT_HINTS = {
  systemctl: [
    "DanielOS(안드로이드/Termux)에서는 systemd가 없어 systemctl을 직접 지원하지 않습니다.",
    "대신: openclaw gateway status|start|stop|restart 또는 service 스크립트 사용"
  ],
  reboot: [
    "모바일 환경에서는 reboot 권한이 제한될 수 있습니다.",
    "대신 기기 UI에서 재부팅하거나, 권한 있는 환경에서만 실행하세요."
  ],
  poweroff: [
    "모바일 환경에서는 poweroff 권한이 제한될 수 있습니다.",
    "대신 기기 UI 종료를 사용하세요."
  ],
  mount: [
    "mount는 루트/커널 권한이 필요해 DanielOS 기본 환경에서 제한됩니다."
  ],
  modprobe: [
    "커널 모듈 로드는 Android 사용자 공간에서 일반적으로 불가합니다."
  ],
  free: [
    "free 명령이 없으면 /proc/meminfo 기반으로 대체 출력합니다."
  ]
};

function compatMsg(lang, key) {
  const M = {
    ko: {
      ip_none: "ip/ifconfig 둘 다 없음",
      ss_none: "ss/netstat 둘 다 없음",
      netstat_none: "netstat/ss 둘 다 없음",
      grep_p: "grep -P는 환경에 따라 미지원일 수 있어. ripgrep( rg -P ) 또는 grep -E 사용 권장",
      find_printf: "find -printf는 일부 환경에서 미지원일 수 있음",
      find_regextype: "find -regextype는 환경별 지원 차이가 있음 (기본 regex로 재시도 권장)",
      awk_posix: "awk -W posix는 구현별 차이 있음",
      stat_c: "stat -c 미지원일 수 있어. stat 기본 출력으로 대체",
      tar_warning: "tar --warning 옵션은 구현별 차이 있음",
      rm_rf: "rm -rf는 위험. 경로 재확인 권장",
      cp_opt: "cp 옵션 충돌(-n/-i/-f) 시 마지막 옵션이 우선됨",
      mv_opt: "mv 옵션 충돌(-n/-i/-f) 시 마지막 옵션이 우선됨",
      ln_s: "ln -s는 대상 경로 기준(상대/절대) 확인 권장",
      install: "install 명령은 일부 옵션/권한이 환경에 따라 제한될 수 있음",
      touch_d: "touch -d 날짜 파싱은 구현별 차이가 있을 수 있음",
      chown: "chown은 Android/Termux 환경에서 권한 제한이 큼",
      chmod_x: "chmod +x는 정상이나 noexec 마운트에서는 실행 제한 가능",
      git_p: "git -p는 pager 사용. 비TTY 환경에서는 --no-pager 권장",
      free_fallback: "free 없음: /proc/meminfo 대체 출력",
      systemctl: "DanielOS(안드로이드/Termux)에서는 systemd가 없어 systemctl을 직접 지원하지 않습니다. 대신: openclaw gateway status|start|stop|restart 또는 service 스크립트 사용"
    },
    en: {
      ip_none: "neither ip nor ifconfig is available",
      ss_none: "neither ss nor netstat is available",
      netstat_none: "neither netstat nor ss is available",
      grep_p: "grep -P may be unsupported; use rg -P or grep -E",
      find_printf: "find -printf may be unsupported on this environment",
      find_regextype: "find -regextype support may vary across environments",
      awk_posix: "awk -W posix behavior may vary by implementation",
      stat_c: "stat -c may be unsupported; fallback to default stat output",
      tar_warning: "tar --warning behavior may vary by implementation",
      rm_rf: "rm -rf is dangerous; verify the path",
      cp_opt: "for cp, conflicting -n/-i/-f options: last one wins",
      mv_opt: "for mv, conflicting -n/-i/-f options: last one wins",
      ln_s: "for ln -s, verify target path context (relative/absolute)",
      install: "install options/permissions may be restricted in this environment",
      touch_d: "touch -d date parsing may differ by implementation",
      chown: "chown is heavily restricted on Android/Termux",
      chmod_x: "chmod +x is fine, but noexec mounts can still block execution",
      git_p: "git -p uses pager; prefer --no-pager in non-TTY contexts",
      free_fallback: "free is unavailable; showing /proc/meminfo fallback",
      systemctl: "systemctl is not supported (no systemd). Use openclaw gateway status|start|stop|restart or service scripts"
    }
  };
  const l = (lang || "ko").startsWith("en") ? "en" : "ko";
  return M[l][key] || M.ko[key] || key;
}

export function rewriteCommand(raw, lang = "ko") {
  const trimmed = raw.trim();
  if (!trimmed) return { cmd: trimmed, warning: null };

  // $$ 프롬프트 입력 보정 요청 반영
  const normalized = trimmed.replace(/^\$\$\s*/, "");
  const head = normalized.split(/\s+/)[0];

  // 자주 쓰는 Linux 습관 명령 호환
  if (/^ps\s+aux(\s|$)/.test(normalized)) {
    return {
      cmd: `if ps aux >/dev/null 2>&1; then ${normalized}; else ps -ef; fi`,
      warning: "ps-aux-fallback"
    };
  }

  if (/^ip\s+a(ddr)?(\s|$)/.test(normalized)) {
    return {
      cmd: `if command -v ip >/dev/null 2>&1; then ${normalized}; elif command -v ifconfig >/dev/null 2>&1; then ifconfig; else echo '[compat] ${compatMsg(lang, "ip_none")}' >&2; false; fi`,
      warning: "ip-a-fallback"
    };
  }

  if (/^df\s+-h(\s|$)/.test(normalized)) {
    return {
      cmd: `if df -h >/dev/null 2>&1; then ${normalized}; else df; fi`,
      warning: "df-h-fallback"
    };
  }

  if (/^du\s+-sh(\s|$)/.test(normalized)) {
    return {
      cmd: `if du -sh . >/dev/null 2>&1; then ${normalized}; else du -s ${normalized.replace(/^du\s+-sh\s*/, "")}; fi`,
      warning: "du-sh-fallback"
    };
  }

  if (/^ss(\s|$)/.test(normalized)) {
    return {
      cmd: `if command -v ss >/dev/null 2>&1 && ss -h >/dev/null 2>&1; then ${normalized}; elif command -v netstat >/dev/null 2>&1; then netstat -tunlp; else echo '[compat] ${compatMsg(lang, "ss_none")}' >&2; false; fi`,
      warning: "ss-fallback"
    };
  }

  if (/^netstat(\s|$)/.test(normalized)) {
    return {
      cmd: `if command -v netstat >/dev/null 2>&1; then ${normalized}; elif command -v ss >/dev/null 2>&1; then ss -tunlp; else echo '[compat] ${compatMsg(lang, "netstat_none")}' >&2; false; fi`,
      warning: "netstat-fallback"
    };
  }

  if (/^which\s+/.test(normalized)) {
    const target = normalized.replace(/^which\s+/, "").trim();
    return {
      cmd: `if command -v ${target} >/dev/null 2>&1; then command -v ${target}; else echo '${target} not found'; exit 1; fi`,
      warning: "which-fallback"
    };
  }

  if (/^whereis\s+/.test(normalized)) {
    const target = normalized.replace(/^whereis\s+/, "").trim();
    return {
      cmd: `if command -v whereis >/dev/null 2>&1; then ${normalized}; else p=$(command -v ${target} 2>/dev/null || true); if [ -n "$p" ]; then echo '${target}: '"$p"; else echo '${target}:'; fi; fi`,
      warning: "whereis-fallback"
    };
  }

  if (/^locate\s+/.test(normalized)) {
    const q = normalized.replace(/^locate\s+/, "").trim().replace(/'/g, "'\\''");
    return {
      cmd: `if command -v locate >/dev/null 2>&1; then ${normalized}; else find . -iname '*${q}*' 2>/dev/null | head -n 200; fi`,
      warning: "locate-fallback"
    };
  }

  if (/^grep\s+/.test(normalized) && /\s-P(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "grep_p")}' >&2; ${normalized}`,
      warning: "grep-p-warning"
    };
  }

  if (/^egrep\s+/.test(normalized)) {
    const rest = normalized.replace(/^egrep\s+/, "");
    return {
      cmd: `grep -E ${rest}`,
      warning: "egrep-alias"
    };
  }

  if (/^fgrep\s+/.test(normalized)) {
    const rest = normalized.replace(/^fgrep\s+/, "");
    return {
      cmd: `grep -F ${rest}`,
      warning: "fgrep-alias"
    };
  }

  if (/^sed\s+/.test(normalized) && /\s-r(\s|$)/.test(normalized)) {
    return {
      cmd: `${normalized.replace(/\s-r(\s|$)/g, ' -E$1')}`,
      warning: "sed-r-to-E"
    };
  }

  if (/^find\s+/.test(normalized) && /\s-printf(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "find_printf")}' >&2; ${normalized}`,
      warning: "find-printf-warning"
    };
  }

  if (/^find\s+/.test(normalized) && /\s-regextype(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "find_regextype")}' >&2; ${normalized}`,
      warning: "find-regextype-warning"
    };
  }

  if (/^awk\s+/.test(normalized) && /\s-W\s+posix(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "awk_posix")}' >&2; ${normalized}`,
      warning: "awk-posix-warning"
    };
  }

  if (/^awk\s+/.test(normalized) && /\s--posix(\s|$)/.test(normalized)) {
    return {
      cmd: `${normalized.replace(/\s--posix(\s|$)/g, ' ')}`,
      warning: "awk-posix-strip"
    };
  }

  if (/^xargs\s+/.test(normalized) && /\s-r(\s|$)/.test(normalized)) {
    return {
      cmd: `${normalized.replace(/\s-r(\s|$)/g, ' ')}`,
      warning: "xargs-r-strip"
    };
  }

  if (/^stat\s+/.test(normalized) && /\s-c(\s|$)/.test(normalized)) {
    return {
      cmd: `if stat -c '%n' / >/dev/null 2>&1; then ${normalized}; else echo '[compat] ${compatMsg(lang, "stat_c")}' >&2; target=$(echo ${JSON.stringify(normalized)} | sed -E 's/^stat\s+//'); stat $target; fi`,
      warning: "stat-c-fallback"
    };
  }

  if (/^readlink\s+-f(\s|$)/.test(normalized)) {
    const target = normalized.replace(/^readlink\s+-f\s+/, "").trim();
    return {
      cmd: `if readlink -f . >/dev/null 2>&1; then ${normalized}; else (cd '${target.replace(/'/g, "'\\''")}' 2>/dev/null && pwd -P) || echo '${target.replace(/'/g, "'\\''")}' ; fi`,
      warning: "readlink-f-fallback"
    };
  }

  if (/^realpath(\s|$)/.test(normalized)) {
    const target = normalized.replace(/^realpath\s+/, "").trim() || ".";
    return {
      cmd: `if command -v realpath >/dev/null 2>&1; then ${normalized}; else readlink -f '${target.replace(/'/g, "'\\''")}' 2>/dev/null || (cd '${target.replace(/'/g, "'\\''")}' 2>/dev/null && pwd -P) || echo '${target.replace(/'/g, "'\\''")}'; fi`,
      warning: "realpath-fallback"
    };
  }

  if (/^tar\s+/.test(normalized) && /\s--warning(=|\s)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "tar_warning")}' >&2; ${normalized}`,
      warning: "tar-warning-option"
    };
  }

  if (/^rm\s+/.test(normalized) && /\s-rf(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "rm_rf")}' >&2; ${normalized}`,
      warning: "rm-rf-warning"
    };
  }

  if (/^cp\s+/.test(normalized) && /\s(-n|-i|-f)(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "cp_opt")}' >&2; ${normalized}`,
      warning: "cp-option-precedence"
    };
  }

  if (/^mv\s+/.test(normalized) && /\s(-n|-i|-f)(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "mv_opt")}' >&2; ${normalized}`,
      warning: "mv-option-precedence"
    };
  }

  if (/^ln\s+-s(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "ln_s")}' >&2; ${normalized}`,
      warning: "ln-s-note"
    };
  }

  if (/^install(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "install")}' >&2; ${normalized}`,
      warning: "install-note"
    };
  }

  if (/^touch\s+/.test(normalized) && /\s-d(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "touch_d")}' >&2; ${normalized}`,
      warning: "touch-d-note"
    };
  }

  if (/^chown(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "chown")}' >&2; ${normalized}`,
      warning: "chown-warning"
    };
  }

  if (/^chmod(\s|$)/.test(normalized) && /\s\+x(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "chmod_x")}' >&2; ${normalized}`,
      warning: "chmod-x-note"
    };
  }

  if (/^git\s+/.test(normalized) && /\s-p(\s|$)/.test(normalized)) {
    return {
      cmd: `echo '[compat] ${compatMsg(lang, "git_p")}' >&2; ${normalized}`,
      warning: "git-pager-note"
    };
  }

  if (normalized === "ll") {
    return {
      cmd: "ls -alF",
      warning: "ll-alias"
    };
  }

  if (head === "systemctl") {
    return {
      cmd: `echo "[compat] ${compatMsg(lang, "systemctl")}" >&2; false`,
      warning: "systemctl-not-supported"
    };
  }

  if (head === "free") {
    return {
      cmd: `if command -v free >/dev/null 2>&1; then ${normalized}; else echo '[compat] ${compatMsg(lang, "free_fallback")}'; awk '/MemTotal|MemFree|MemAvailable|Buffers|Cached/ {print $1, $2 " kB"}' /proc/meminfo; fi`,
      warning: "free-fallback"
    };
  }

  if (["reboot", "poweroff", "mount", "modprobe"].includes(head)) {
    const hint = COMPAT_HINTS[head]?.join(" ") ?? "지원되지 않는 시스템 명령입니다.";
    return {
      cmd: `echo "[compat] ${hint}" >&2; ${normalized}`,
      warning: `${head}-restricted`
    };
  }

  return { cmd: normalized, warning: null };
}
