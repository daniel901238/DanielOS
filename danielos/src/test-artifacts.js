import { rmSync } from "node:fs";

export const TEST_ARTIFACTS = [
  "./readme.copy",
  "./readme.copy2",
  "./readme.link",
  "./readme.install",
  "./readme.touch"
];

export function cleanupTestArtifacts() {
  for (const p of TEST_ARTIFACTS) {
    try {
      rmSync(p, { force: true, recursive: false });
    } catch {
      // ignore
    }
  }
}
