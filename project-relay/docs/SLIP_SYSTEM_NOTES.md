# Slip System 적용 노트

## 추가된 스크립트
- `PlayerTerrainState.cs`
  - RiskZone 트리거를 받아 현재 지형 상태 저장
  - `StaminaDrainMultiplier`, `SlipChanceBonus` 제공

- `PlayerLocomotion.cs` 업데이트
  - 하중 이동 페널티 반영 (`LoadoutWeightSystem`)
  - 지형 스태미나 소모 배율 반영 (`PlayerTerrainState`)
  - 프레임 단위 슬립 확률 계산 후 `TriggerSlip()` 실행
  - 슬립 시 짧은 기절 + 화물 내구도 감소

## Unity 연결
1. `Player` 오브젝트에 `PlayerTerrainState` 추가
2. RiskZone 콜라이더 `Is Trigger=true` 확인
3. 테스트하면서 `Slip Stun Seconds`, `Slip Cargo Damage` 튜닝

## 콘솔 확인
- 슬립 발생: `[Player] Slipped!`
- 화물 파손 누적 시 미션 실패 로그
