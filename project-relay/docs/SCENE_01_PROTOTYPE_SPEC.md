# Scene 01 Prototype Spec (Project Relay)

## 씬 목표
플레이어가 아래 4가지를 5분 내 경험하도록 구성:
1. 기본 이동/회전
2. 하중 증가에 따른 이동 페널티
3. 위험 지형 진입 효과
4. 목적지 도착으로 배송 완료

---

## 씬 구성
- 씬 이름: `S01_Prototype`
- 지형:
  - 평지 구간 (Safe)
  - 진흙 구간 (Caution)
  - 급경사 구간 (Dangerous)
- 오브젝트:
  - Player (CharacterController)
  - Cargo Pickup 2개 (무게 +5kg / +10kg)
  - Destination Beacon
  - Audio Cue Source (목표 방향 사운드)

---

## 게임오브젝트 세팅
### Player
- 컴포넌트:
  - CharacterController
  - PlayerLocomotion
  - LoadoutWeightSystem
  - DeliveryMissionController
- DeliveryMissionController.destination = Destination Beacon

### Terrain Zones
- Collider isTrigger = true
- TerrainRiskZone:
  - Safe: drain x1.0, slip +0.0
  - Caution: drain x1.2, slip +0.03
  - Dangerous: drain x1.5, slip +0.08

### Destination Beacon
- 시각: emissive 기둥
- 청각: AudioNavigationCue.target 연결

---

## 테스트 체크리스트
- [ ] 입력 시 자연스럽게 이동/회전
- [ ] 하중 증가 후 속도 저하 체감
- [ ] 목적지 접근 시 사운드 큐 볼륨/피치 증가
- [ ] 목적지 반경 진입 시 미션 성공 로그 출력

---

## 디버그 로그 기준
- 미션 시작: `[Mission] Started: M1`
- 미션 성공: `[Mission] Success: M1`
- 미션 실패: `[Mission] Failed: M1`

---

## 다음 씬 확장 계획
- Scene 02: 우회 경로 선택(안전하지만 느림 vs 빠르지만 위험)
- Scene 03: 간접 협력 흔적(타 유저 표식 1차)
