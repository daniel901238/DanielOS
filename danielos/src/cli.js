#!/data/data/com.termux/files/usr/bin/node
import { spawn } from "node:child_process";
import readline from "node:readline";
import { rewriteCommand } from "./compat.js";

const LANG = (process.env.DANIELOSH_LANG || "ko").toLowerCase().startsWith("en") ? "en" : "ko";

const WARNING_TEXT = {
  ko: {
    "systemctl-not-supported": "systemctl은 이 환경에서 미지원",
    "grep-p-warning": "grep -P는 환경별 미지원 가능",
    "tar-warning-option": "tar --warning 옵션은 구현 차이 있음",
    "chown-warning": "chown은 권한 제한 가능",
    "rm-rf-warning": "rm -rf는 위험하니 경로 재확인",
    "ip-a-fallback": "ip/ifconfig 대체 경로 사용",
    "ss-fallback": "ss/netstat 대체 경로 사용",
    "netstat-fallback": "netstat/ss 대체 경로 사용",
    "which-fallback": "which 대체(command -v) 사용",
    "whereis-fallback": "whereis 대체 경로 사용",
    "locate-fallback": "locate 대체(find) 사용",
    "egrep-alias": "egrep → grep -E 변환",
    "fgrep-alias": "fgrep → grep -F 변환",
    "sed-r-to-E": "sed -r → sed -E 변환",
    "find-printf-warning": "find -printf 호환성 경고",
    "find-regextype-warning": "find -regextype 호환성 경고",
    "awk-posix-warning": "awk POSIX 옵션 호환성 경고",
    "awk-posix-strip": "awk --posix 옵션 제거",
    "xargs-r-strip": "xargs -r 옵션 제거",
    "stat-c-fallback": "stat -c 대체 경로 사용",
    "readlink-f-fallback": "readlink -f 대체 경로 사용",
    "realpath-fallback": "realpath 대체 경로 사용",
    "cp-option-precedence": "cp 옵션 우선순위 경고",
    "mv-option-precedence": "mv 옵션 우선순위 경고",
    "ln-s-note": "ln -s 경로 기준 안내",
    "install-note": "install 권한/옵션 제약 안내",
    "touch-d-note": "touch -d 파싱 차이 안내",
    "chmod-x-note": "chmod +x 실행 제약 안내",
    "git-pager-note": "git pager 사용 안내",
    "free-fallback": "free 대체 출력 사용",
    "ll-alias": "ll → ls -alF 변환",
    "df-h-fallback": "df -h 대체 경로 사용",
    "du-sh-fallback": "du -sh 대체 경로 사용",
    "ps-aux-fallback": "ps aux 대체 경로 사용"
  },
  en: {
    "systemctl-not-supported": "systemctl is not supported in this environment",
    "grep-p-warning": "grep -P may be unsupported depending on environment",
    "tar-warning-option": "tar --warning behavior may differ by implementation",
    "chown-warning": "chown may be restricted due to permissions",
    "rm-rf-warning": "rm -rf is dangerous; verify target path",
    "ip-a-fallback": "using ip/ifconfig fallback path",
    "ss-fallback": "using ss/netstat fallback path",
    "netstat-fallback": "using netstat/ss fallback path",
    "which-fallback": "using which fallback (command -v)",
    "whereis-fallback": "using whereis fallback path",
    "locate-fallback": "using locate fallback (find)",
    "egrep-alias": "egrep rewritten to grep -E",
    "fgrep-alias": "fgrep rewritten to grep -F",
    "sed-r-to-E": "sed -r rewritten to sed -E",
    "find-printf-warning": "find -printf compatibility warning",
    "find-regextype-warning": "find -regextype compatibility warning",
    "awk-posix-warning": "awk POSIX option compatibility warning",
    "awk-posix-strip": "awk --posix option removed",
    "xargs-r-strip": "xargs -r option removed",
    "stat-c-fallback": "using stat -c fallback path",
    "readlink-f-fallback": "using readlink -f fallback path",
    "realpath-fallback": "using realpath fallback path",
    "cp-option-precedence": "cp option precedence warning",
    "mv-option-precedence": "mv option precedence warning",
    "ln-s-note": "ln -s target path context note",
    "install-note": "install permission/option limitation note",
    "touch-d-note": "touch -d parsing variability note",
    "chmod-x-note": "chmod +x execution limitation note",
    "git-pager-note": "git pager usage note",
    "free-fallback": "using free fallback output",
    "ll-alias": "ll rewritten to ls -alF",
    "df-h-fallback": "using df -h fallback path",
    "du-sh-fallback": "using du -sh fallback path",
    "ps-aux-fallback": "using ps aux fallback path"
  }
};

function warningLabel(id) {
  return WARNING_TEXT[LANG][id] || id.replaceAll("-", " ");
}

function runThroughBash(input) {
  return new Promise((resolve) => {
    const { cmd, warning } = rewriteCommand(input, LANG);
    if (!cmd) return resolve(0);
    if (warning) {
      console.error(`[compat:${warning}] ${warningLabel(warning)}`);
    }

    const child = spawn("bash", ["-lc", cmd], {
      stdio: "inherit",
      env: {
        ...process.env,
        PATH: [
          "/data/data/com.termux/files/usr/bin",
          process.env.PATH || ""
        ].join(":"),
      },
    });

    child.on("error", (err) => {
      console.error(`[danielsh:error] ${err.message}`);
      resolve(127);
    });

    child.on("exit", (code, signal) => {
      if (signal) {
        console.error(`[danielsh] interrupted by signal: ${signal}`);
        return resolve(130);
      }
      const finalCode = code ?? 0;
      if (finalCode !== 0) {
        console.error(`[danielsh:exit] code=${finalCode}`);
      }
      resolve(finalCode);
    });
  });
}

async function main() {
  // one-shot mode: danielsh "ls -al"
  if (process.argv.length > 2) {
    const line = process.argv.slice(2).join(" ");
    const code = await runThroughBash(line);
    process.exit(code);
  }

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    prompt: "$ ",
  });

  console.log(LANG === "en" ? "DanielOS shell v0.1 (type 'exit' to quit)" : "DanielOS shell v0.1 (종료: exit)");
  rl.prompt();

  rl.on("line", async (line) => {
    const trimmed = line.trim();
    if (trimmed === "exit" || trimmed === "quit") {
      rl.close();
      return;
    }

    await runThroughBash(trimmed);
    rl.prompt();
  });

  rl.on("close", () => {
    process.exit(0);
  });
}

main();
