#!/usr/bin/env node
import { spawn } from "node:child_process";
import process from "node:process";

const npmCmd = process.env.NPM_BIN || "npm";
const npxCmd = process.env.NPX_BIN || "npx";

function run(command, args, options = {}) {
  return new Promise((resolve) => {
    const child = spawn(command, args, {
      stdio: "pipe",
      ...options,
    });

    let stdout = "";
    let stderr = "";

    child.stdout?.on("data", (d) => {
      stdout += d.toString();
    });

    child.stderr?.on("data", (d) => {
      stderr += d.toString();
    });

    child.on("error", (error) => {
      resolve({ code: 127, stdout, stderr: `${stderr}\n${error.message}`.trim() });
    });

    child.on("close", (code) => {
      resolve({ code: code ?? 1, stdout, stderr });
    });
  });
}

async function check(command, args, label) {
  const result = await run(command, args);
  if (result.code !== 0) {
    console.error(`❌ ${label} 실패`);
    if (result.stderr.trim()) console.error(result.stderr.trim());
    process.exit(result.code);
  }
  console.log(`✅ ${label}`);
  if (result.stdout.trim()) console.log(result.stdout.trim());
}

async function main() {
  const installMode = process.argv.includes("--install") || process.env.OPENCLAW_SMOKE_INSTALL === "1";

  console.log("[DanielOS] OpenClaw 설치 스모크 테스트 시작");

  await check("node", ["--version"], "Node.js 실행 확인");
  await check(npmCmd, ["--version"], "npm 실행 확인");
  await check(npmCmd, ["view", "openclaw", "version", "--silent"], "npm 레지스트리에서 openclaw 조회");

  if (!installMode) {
    console.log("ℹ️ 설치 단계는 건너뜀 (--install 또는 OPENCLAW_SMOKE_INSTALL=1로 활성화)");
    return;
  }

  await check(npxCmd, ["-y", "openclaw", "--version"], "npx 기반 openclaw 설치/실행 확인");

  const status = await run(npxCmd, ["-y", "openclaw", "gateway", "status"]);
  if (status.code === 0) {
    console.log("✅ openclaw gateway status 확인 완료");
    if (status.stdout.trim()) console.log(status.stdout.trim());
  } else {
    console.log("⚠️ openclaw 설치/실행은 성공했지만 gateway status는 비정상 (환경 이슈 가능)");
    if (status.stderr.trim()) console.log(status.stderr.trim());
  }

  console.log("🎉 OpenClaw 설치 스모크 테스트 완료");
}

main();
