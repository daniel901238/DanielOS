# TUNING PRESET v1 (Scene 01 기준)

## 1) PlayerLocomotion 권장값
- Base Move Speed: **4.6**
- Rotation Speed: **11**
- Gravity: **-22**
- Max Stamina: **100**
- Stamina Drain / sec: **11.5**
- Stamina Recover / sec: **10.5**
- Slip Stun Seconds: **0.55**
- Slip Cargo Damage: **4.0**

## 2) LoadoutWeightSystem 권장값
- Safe Weight Kg: **18**
- Hard Limit Weight Kg: **48**
- Max Move Penalty: **0.40**
- Base Fall Chance / sec: **0.012**
- Extra Fall Chance At HardLimit: **0.12**

## 3) TerrainRiskZone 권장값
### Safe
- Drain Multiplier: **1.0**
- Slip Bonus: **0.00**

### Caution
- Drain Multiplier: **1.25**
- Slip Bonus: **0.025**

### Dangerous
- Drain Multiplier: **1.55**
- Slip Bonus: **0.07**

## 4) 목표 체감
- Safe: 안정 이동, 슬립 거의 없음
- Caution: 긴장감 시작, 무게 많이 들면 실수 발생
- Dangerous: 우회 고민이 생기는 수준

## 5) 빠른 조정 규칙
- 너무 쉬움 → `Slip Bonus` +0.01 / `ExtraFall` +0.02
- 너무 어려움 → `Slip Bonus` -0.01 / `SlipCargoDamage` -1
- 플레이가 너무 느림 → `MaxMovePenalty` -0.05
