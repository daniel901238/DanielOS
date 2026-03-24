# Scene 01 Quick Wire (추가 스크립트 연결)

## 1) CargoPickup 연결
- 오브젝트 2개 생성: `Cargo_5kg`, `Cargo_10kg`
- Collider (IsTrigger=true)
- `CargoPickup` 스크립트 부착
  - Cargo_5kg: `addWeightKg=5`
  - Cargo_10kg: `addWeightKg=10`

## 2) MissionAutoStarter 연결
- `Player`에 `MissionAutoStarter` 부착
- `missionController`에 같은 오브젝트의 `DeliveryMissionController` 연결
- play 시작 후 자동으로 미션 시작 로그 확인

## 3) StaminaBarUI 연결
- Canvas > UI > Slider 생성 (이름: `StaminaSlider`)
- `StaminaBarUI` 스크립트를 Canvas 또는 별도 UI 오브젝트에 부착
- `playerLocomotion`에 Player의 `PlayerLocomotion` 연결
- `staminaSlider`에 `StaminaSlider` 연결
- Slider Min=0 / Max=1 / Value=1

## 4) 확인 로그
- 시작: `[Mission] Started: M1`
- 픽업: `[CargoPickup] +5kg` 또는 `+10kg`
- 도착: `[Mission] Success: M1`
