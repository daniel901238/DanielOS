#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const input = path.resolve(__dirname, '../data/playtest_runs.txt');
const output = path.resolve(__dirname, '../docs/TUNING_REPORT_V3.md');

const raw = fs.readFileSync(input, 'utf8');
const rows = raw.split('\n').map(l=>l.trim()).filter(l=>l && !l.startsWith('#'));

function num(v){ const n=Number(v); return Number.isFinite(n)?n:null; }

const runs = rows.map(r=>r.split('|').map(s=>s.trim())).map(c=>(
  {ok:(c[1]||'').toUpperCase()==='S',t:num(c[2]),s:num(c[3]),sf:(c[4]||'').toUpperCase()==='Y'}
)).filter(r=>r.t && r.s!==null);

if(!runs.length){
  console.error('No valid runs.');
  process.exit(1);
}

const n=runs.length;
const successRate= runs.filter(r=>r.ok).length/n*100;
const avgTime = runs.reduce((a,b)=>a+b.t,0)/n;
const avgSlip = runs.reduce((a,b)=>a+b.s,0)/n;
const slipFailRate = runs.filter(r=>r.sf).length/n*100;

let diff='적정';
let rec=[];
if(successRate<50 || slipFailRate>45){
  diff='완화 필요';
  rec=['Dangerous.SlipBonus -0.015','Caution.SlipBonus -0.008','Player.slipCargoDamage -1.0','Loadout.extraFallChanceAtHardLimit -0.02'];
}else if(successRate>85 && avgSlip<1.5){
  diff='강화 필요';
  rec=['Dangerous.SlipBonus +0.01','Caution.SlipBonus +0.005','Loadout.extraFallChanceAtHardLimit +0.015','Player.slipStunSeconds +0.05'];
}else{
  rec=['핵심 난이도 수치 유지','튜토리얼/사운드 가독성 개선 우선'];
}

const md = `# Tuning Report v3\n\n- 판수: ${n}\n- 성공률: ${successRate.toFixed(1)}%\n- 평균 시간: ${(avgTime/60).toFixed(2)}분 (${avgTime.toFixed(1)}초)\n- 평균 슬립: ${avgSlip.toFixed(2)}회\n- 슬립 실패 비중: ${slipFailRate.toFixed(1)}%\n\n## 판정\n- 난이도: **${diff}**\n\n## 추천 조정안\n${rec.map((x,i)=>`${i+1}. ${x}`).join('\n')}\n`;

fs.writeFileSync(output, md);
console.log('Generated:', output);
