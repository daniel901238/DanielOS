import { execSync } from "node:child_process";
import { readFileSync } from "node:fs";
import { cleanupTestArtifacts } from "./test-artifacts.js";
import { printSummary, writeTestReport } from "./test-report.js";

const lines = readFileSync(new URL("../tests/compat/cases.txt", import.meta.url), "utf8")
  .split("\n")
  .map((s) => s.trim())
  .filter((s) => s && !s.startsWith("#"));

function shellSingleQuote(s) {
  return `'${s.replace(/'/g, `'\\''`)}'`;
}

let pass = 0;
let fail = 0;
let warningCount = 0;
const failedCommands = [];

cleanupTestArtifacts();

for (const line of lines) {
  const expectFail = line.startsWith("!");
  const cmd = expectFail ? line.slice(1).trim() : line;
  const full = `node ./src/cli.js ${shellSingleQuote(cmd)}`;
  console.log(`\n$ ${full}`);
  try {
    const out = execSync(full, { cwd: process.cwd(), stdio: "pipe" }).toString();
    process.stdout.write(out);
    warningCount += (out.match(/\[compat:/g) || []).length;
    if (expectFail) {
      console.error("[test] expected failure but succeeded");
      fail++;
      failedCommands.push(cmd);
    } else {
      pass++;
    }
  } catch (e) {
    const so = (e.stdout || "").toString();
    const se = (e.stderr || "").toString();
    process.stdout.write(so);
    process.stderr.write(se);
    warningCount += ((so + se).match(/\[compat:/g) || []).length;
    if (expectFail) {
      pass++;
    } else {
      fail++;
      failedCommands.push(cmd);
    }
  }
}

cleanupTestArtifacts();
const report = {
  ts: new Date().toISOString(),
  suite: "compat",
  total: lines.length,
  pass,
  fail,
  warningCount,
  failedCommands
};
writeTestReport(report);
console.log(`\n[result] pass=${pass} fail=${fail} total=${lines.length}`);
printSummary(report);
process.exit(fail === 0 ? 0 : 1);

