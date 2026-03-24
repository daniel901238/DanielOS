import { readFileSync } from 'node:fs';

const cli = readFileSync(new URL('./cli.js', import.meta.url), 'utf8');
const compat = readFileSync(new URL('./compat.js', import.meta.url), 'utf8');

const warningIds = [...compat.matchAll(/warning:\s*"([a-z0-9-]+)"/g)].map(m => m[1]);
const uniqueWarningIds = [...new Set(warningIds)].sort();

const koBlock = cli.match(/ko:\s*\{([\s\S]*?)\}\s*,\s*en:/)?.[1] || '';
const enBlock = cli.match(/en:\s*\{([\s\S]*?)\}\s*\}\s*;/)?.[1] || '';

const koKeys = [...koBlock.matchAll(/"([a-z0-9-]+)"\s*:/g)].map(m => m[1]);
const enKeys = [...enBlock.matchAll(/"([a-z0-9-]+)"\s*:/g)].map(m => m[1]);

const missInKo = uniqueWarningIds.filter(k => !koKeys.includes(k));
const missInEn = uniqueWarningIds.filter(k => !enKeys.includes(k));

if (missInKo.length || missInEn.length) {
  console.error('[lint] warning key mismatch detected');
  if (missInKo.length) console.error(' missing in ko:', missInKo.join(', '));
  if (missInEn.length) console.error(' missing in en:', missInEn.join(', '));
  process.exit(1);
}

console.log(`[lint] ok: ${uniqueWarningIds.length} warning keys mapped in ko/en`);
