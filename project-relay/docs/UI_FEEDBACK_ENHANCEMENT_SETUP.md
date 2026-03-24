# UI Feedback Enhancement Setup

## 추가 스크립트
- `UI/BalanceButtonVisual.cs`
- `UI/StaminaWarningBlink.cs`

## 1) 보정 버튼 색상 변화
1. `BalanceHoldButton` 오브젝트에 `BalanceButtonVisual` 부착
2. `balanceAssist` -> Player의 `BalanceAssistController` 연결
3. `targetImage` -> 버튼의 Image 연결
4. 색상 조정
   - idleColor: 기본
   - activeColor: 눌림/활성 상태

## 2) 스태미나 경고 깜빡임
1. `StaminaSlider Fill`(또는 경고 아이콘)에 `StaminaWarningBlink` 부착
2. `playerLocomotion` -> Player의 `PlayerLocomotion` 연결
3. `targetGraphic` -> 깜빡일 UI Graphic 지정
4. 권장값
   - warningThreshold: 0.25
   - blinkSpeed: 8

## 동작
- 균형 보정 버튼 누르는 동안 버튼 색상 활성 컬러로 변경
- 스태미나 25% 이하에서 UI 경고 깜빡임 시작
