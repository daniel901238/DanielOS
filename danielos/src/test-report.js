import { mkdirSync, writeFileSync } from 'node:fs';

export function writeTestReport(report) {
  mkdirSync(new URL('../reports/', import.meta.url), { recursive: true });
  const latest = new URL('../reports/latest.json', import.meta.url);
  const bySuite = new URL(`../reports/latest-${report.suite}.json`, import.meta.url);
  const payload = JSON.stringify(report, null, 2);
  writeFileSync(latest, payload);
  writeFileSync(bySuite, payload);
}

export function printSummary(report) {
  const rate = report.total > 0 ? Math.round((report.pass / report.total) * 100) : 0;
  console.log(`\n[summary] pass=${report.pass}/${report.total} (${rate}%) fail=${report.fail} warnings=${report.warningCount}`);
  if (report.failedCommands.length) {
    console.log(`[summary] failed: ${report.failedCommands.join(' | ')}`);
  }
}
