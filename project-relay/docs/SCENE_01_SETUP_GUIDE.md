# Scene 01 Setup Guide (Unity Inspector 프리셋 + 배치 순서)

## 1) Inspector 프리셋 (권장 시작값)

### A. Player (GameObject: `Player`)
#### CharacterController
- Center: `(0, 0.9, 0)`
- Radius: `0.35`
- Height: `1.8`
- Slope Limit: `45`
- Step Offset: `0.3`

#### PlayerLocomotion
- Base Move Speed: `4.5`
- Rotation Speed: `10`
- Gravity: `-20`
- Max Stamina: `100`
- Stamina Drain / sec: `12`
- Stamina Recover / sec: `10`

#### LoadoutWeightSystem
- Carried Weight Kg: `0`
- Safe Weight Kg: `20`
- Hard Limit Weight Kg: `50`
- Max Move Penalty: `0.45`
- Base Fall Chance / sec: `0.01`
- Extra Fall Chance At HardLimit: `0.15`

#### DeliveryMissionController
- Mission Id: `M1`
- Cargo Health: `100`
- Destination: `Destination_Beacon` 할당
- Success Radius: `2.5`

---

### B. Terrain Zones

#### TerrainRiskZone (Safe)
- Risk Level: `Safe`
- Stamina Drain Multiplier: `1.0`
- Slip Chance Bonus: `0.0`

#### TerrainRiskZone (Caution)
- Risk Level: `Caution`
- Stamina Drain Multiplier: `1.2`
- Slip Chance Bonus: `0.03`

#### TerrainRiskZone (Dangerous)
- Risk Level: `Dangerous`
- Stamina Drain Multiplier: `1.5`
- Slip Chance Bonus: `0.08`

> 모든 RiskZone Collider는 `Is Trigger = true`

---

### C. AudioNavigationCue (GameObject: `AudioNavigator`)
- Player: `Player` 할당
- Target: `Destination_Beacon` 할당
- Cue Source: `AudioSource` 할당
- Max Distance: `40`
- Min Pitch: `0.8`
- Max Pitch: `1.3`

#### AudioSource
- Play On Awake: `true`
- Loop: `true`
- Spatial Blend: `0` (2D)
- Volume: `0.5`
- 작은 반복 톤 클립 할당 (임시 beep)

---

## 2) 씬 배치 순서 (1~10)

1. `Scenes` 폴더에 새 씬 생성: `S01_Prototype`
2. Plane 생성 → 이름 `Ground_Safe` (시작 지점)
3. 두 번째 Plane 생성 → 이름 `Ground_Caution` (진흙 구간)
4. 세 번째 Plane + 경사 배치 → 이름 `Ground_Danger`
5. 빈 오브젝트 생성 → `Player` (CharacterController + 스크립트 3종 부착)
6. Cylinder 생성 → `Destination_Beacon` (끝 지점 배치)
7. 빈 오브젝트 생성 → `AudioNavigator` + `AudioNavigationCue` + `AudioSource`
8. 위험 구간별 Trigger Collider 박스 생성 (`Zone_Safe`, `Zone_Caution`, `Zone_Danger`)
9. 각 Zone에 `TerrainRiskZone` 부착 + 프리셋 입력
10. `Player`의 `DeliveryMissionController.destination`에 `Destination_Beacon` 연결 후 Play 테스트

---

## 3) 빠른 테스트 플로우

1. 플레이 시작 후 Console에서 `StartMission()` 수동 호출(임시)
2. 이동하면서 목적지 사운드가 좌우/거리감으로 변하는지 확인
3. 목적지 반경 진입 시 `[Mission] Success: M1` 로그 확인

---

## 4) 다음 개선(바로 이어서)
- UI 스태미나 바 추가
- 하중 UI(kg 표시)
- 위험 구간 진입 시 햅틱/경고음 추가
- Cargo Pickup 오브젝트로 `AddWeight(5/10)` 연결

작성: 소미 🐾
