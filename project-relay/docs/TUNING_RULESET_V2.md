# TUNING RULESET v2 (성공률 기반 자동 리밸런싱 가이드)

## 입력 지표 (10판 기준)
- 미션 성공률 (%): `successRate`
- 평균 플레이 타임 (분): `avgTime`
- 평균 슬립 횟수: `avgSlip`
- 슬립 실패 비중 (%): `slipFailRate`

---

## 1) 난이도 자동 조정 규칙 (if-then)

### A. 너무 어려움
조건:
- `successRate < 50` 또는
- `slipFailRate > 45`

조치:
1. `Dangerous.SlipBonus` **-0.015**
2. `Caution.SlipBonus` **-0.008**
3. `Player.slipCargoDamage` **-1.0**
4. `Loadout.extraFallChanceAtHardLimit` **-0.02**

---

### B. 적정 난이도 (목표 구간)
조건:
- `successRate 60~80` AND
- `slipFailRate 20~35`

조치:
- 값 유지 (변경 없음)
- 사운드 가독성/튜토리얼 품질만 개선

---

### C. 너무 쉬움
조건:
- `successRate > 85` AND
- `avgSlip < 1.5`

조치:
1. `Dangerous.SlipBonus` **+0.01**
2. `Caution.SlipBonus` **+0.005**
3. `Loadout.extraFallChanceAtHardLimit` **+0.015**
4. `Player.slipStunSeconds` **+0.05**

---

## 2) 템포(속도감) 조정

### 플레이가 너무 느림
조건:
- `avgTime > 8.0`

조치:
1. `Loadout.maxMovePenalty` **-0.05**
2. `Player.baseMoveSpeed` **+0.2**
3. `SafeWeightKg` **+2kg**

### 플레이가 너무 빠름
조건:
- `avgTime < 3.5`

조치:
1. `Loadout.maxMovePenalty` **+0.03**
2. `Player.baseMoveSpeed` **-0.15**
3. `Dangerous.DrainMultiplier` **+0.1**

---

## 3) 적용 절차 (반드시 순서대로)
1. 10판 플레이 로그 수집
2. 위 규칙으로 **최대 3개 항목만** 조정
3. 다시 10판 측정
4. 목표 구간(성공률 60~80, 슬립 실패 20~35) 도달 시 고정

---

## 4) 안전장치 (과튜닝 방지)
- 한 사이클에서 같은 파라미터를 2번 이상 바꾸지 않기
- `Dangerous.SlipBonus` 상한: **0.10**, 하한: **0.03**
- `Player.slipCargoDamage` 하한: **2.0**, 상한: **8.0**
- 2사이클 연속 악화되면 v1 프리셋으로 롤백

---

## 5) 오늘 바로 쓰는 빠른 결론
- 성공률 50% 미만이면: **슬립 관련 수치 먼저 완화**
- 성공률 85% 초과면: **Dangerous 슬립 보너스만 소폭 강화**
- 체감이 답답하면: **속도/하중 페널티 먼저 완화**

작성: 소미 🐾
