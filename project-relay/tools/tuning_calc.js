#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const file = process.argv[2] || '../data/playtest_runs.txt';
const raw = fs.readFileSync(path.resolve(__dirname, file), 'utf8');

const rows = raw
  .split('\n')
  .map(l => l.trim())
  .filter(l => l && !l.startsWith('#'))
  .map(line => line.split('|').map(s => s.trim()));

function toNum(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

const parsed = rows.map(cols => ({
  n: toNum(cols[0]),
  successRaw: (cols[1] || '').toUpperCase(),
  timeSec: toNum(cols[2]),
  slips: toNum(cols[3]),
  slipFailRaw: (cols[4] || '').toUpperCase(),
  cargo: toNum(cols[5]),
  note: cols[6] || ''
}));

const runs = parsed
  .filter(r => r.n !== null)
  .filter(r => r.successRaw === 'S' || r.successRaw === 'F')
  .filter(r => r.timeSec !== null && r.timeSec > 0)
  .filter(r => r.slips !== null && r.slips >= 0)
  .map(r => ({
    n: r.n,
    success: r.successRaw === 'S',
    timeSec: r.timeSec,
    slips: r.slips,
    slipFail: r.slipFailRaw === 'Y',
    cargo: r.cargo,
    note: r.note
  }));

if (!runs.length) {
  console.log('유효한 테스트 데이터가 없어.');
  console.log('playtest_runs.txt에 최소 1판 이상 채워줘.');
  console.log('예: 1 | S | 312 | 2 | N | 74 | 위험구간 우회');
  process.exit(1);
}

const count = runs.length;
const successCount = runs.filter(r => r.success).length;
const successRate = (successCount / count) * 100;
const avgTime = runs.reduce((a, b) => a + b.timeSec, 0) / count;
const avgSlip = runs.reduce((a, b) => a + b.slips, 0) / count;
const slipFailRate = (runs.filter(r => r.slipFail).length / count) * 100;

console.log('=== Playtest Summary ===');
console.log(`판수: ${count}`);
console.log(`성공률: ${successRate.toFixed(1)}%`);
console.log(`평균 시간: ${(avgTime / 60).toFixed(2)}분 (${avgTime.toFixed(1)}초)`);
console.log(`평균 슬립: ${avgSlip.toFixed(2)}회`);
console.log(`슬립 실패 비중: ${slipFailRate.toFixed(1)}%`);

console.log('\n=== v2 Rule Recommendation ===');
if (successRate < 50 || slipFailRate > 45) {
  console.log('- 난이도 완화 필요');
  console.log('  1) Dangerous.SlipBonus -0.015');
  console.log('  2) Caution.SlipBonus -0.008');
  console.log('  3) Player.slipCargoDamage -1.0');
  console.log('  4) Loadout.extraFallChanceAtHardLimit -0.02');
} else if (successRate > 85 && avgSlip < 1.5) {
  console.log('- 난이도 강화 필요');
  console.log('  1) Dangerous.SlipBonus +0.01');
  console.log('  2) Caution.SlipBonus +0.005');
  console.log('  3) Loadout.extraFallChanceAtHardLimit +0.015');
  console.log('  4) Player.slipStunSeconds +0.05');
} else {
  console.log('- 난이도 적정 구간 (핵심 수치 유지 권장)');
}

if (avgTime > 480) {
  console.log('- 템포 느림: maxMovePenalty -0.05, baseMoveSpeed +0.2, SafeWeight +2');
} else if (avgTime < 210) {
  console.log('- 템포 빠름: maxMovePenalty +0.03, baseMoveSpeed -0.15, DangerousDrain +0.1');
} else {
  console.log('- 템포 적정');
}
