import { execSync } from "node:child_process";

const tests = [
  "node ./src/cli.js 'echo hello'",
  "node ./src/cli.js 'uname -a | head -n 1'",
  "node ./src/cli.js '$$ echo prompt-fix-ok'",
  "node ./src/cli.js 'll | head -n 2'",
  "node ./src/cli.js 'ps aux | head -n 2'",
  "node ./src/cli.js 'df -h | head -n 3'",
  "node ./src/cli.js 'ip a | head -n 8' || true",
  "node ./src/cli.js 'which node'",
  "node ./src/cli.js 'whereis node'",
  "node ./src/cli.js 'locate package.json | head -n 3' || true",
  "node ./src/cli.js " + '"grep -P \'hello\' README.md"' + " || true",
  "node ./src/cli.js " + '"egrep \'DanielOS\' README.md"' + " || true",
  "node ./src/cli.js " + '"fgrep \'DanielOS\' README.md"' + " || true",
  "node ./src/cli.js " + '"sed -r \'s/(DanielOS)/[\\1]/\' README.md | head -n 2"' + " || true",
  "node ./src/cli.js " + '"find . -maxdepth 2 -printf \'%f\\n\' | head -n 3"' + " || true",
  "node ./src/cli.js " + '"find . -regextype posix-egrep -regex \'./src/.*\' | head -n 3"' + " || true",
  "node ./src/cli.js " + '"awk --posix \'{print NR \":\" $0}\' README.md | head -n 2"' + " || true",
  "node ./src/cli.js " + '"printf \'a\\nb\\n\' | xargs -r -n1 echo"' + " || true",
  "node ./src/cli.js " + '"stat -c \'%n %s\' README.md"' + " || true",
  "node ./src/cli.js " + '"readlink -f ."' + " || true",
  "node ./src/cli.js " + '"tar --warning=no-unknown-keyword -tf package.json"' + " || true",
  "node ./src/cli.js 'systemctl status' || true"
];

for (const t of tests) {
  console.log(`\n$ ${t}`);
  try {
    const out = execSync(t, { cwd: process.cwd(), stdio: "pipe" }).toString();
    process.stdout.write(out);
  } catch (e) {
    process.stdout.write((e.stdout || "").toString());
    process.stderr.write((e.stderr || "").toString());
  }
}

console.log("\n[smoke] done");
