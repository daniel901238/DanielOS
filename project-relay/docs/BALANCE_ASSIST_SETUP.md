# Balance Assist Setup (꾹 누르는 균형 보정 버튼)

## 추가 스크립트
- `UI/HoldButtonInput.cs`
- `Player/BalanceAssistController.cs`

## Unity 연결 순서
1. Canvas 오른쪽에 버튼 생성 (`BalanceHoldButton`)
2. 버튼 오브젝트에 `HoldButtonInput` 부착
3. Player 오브젝트에 `BalanceAssistController` 부착
4. `BalanceAssistController.holdButton`에 `BalanceHoldButton`의 `HoldButtonInput` 연결

## 동작
- 버튼을 누르고 있는 동안 `IsActive=true`
- 슬립 확률이 기본 대비 감소
- 대신 스태미나 소모가 더 빨라짐 (리스크-리워드)
- 에디터 테스트용: 스페이스바 길게 눌러도 동일 동작

## 권장 수치
- slipChanceReduction: `0.45` (45% 감소)
- staminaDrainMultiplierWhenActive: `1.35` (소모 35% 증가)
