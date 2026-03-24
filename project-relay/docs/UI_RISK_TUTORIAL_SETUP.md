# UI Risk + Tutorial Setup

## 추가된 스크립트
- `UI/RiskWarningUI.cs`
- `UI/TutorialHintPopup.cs`

## Unity 연결 순서
1. Canvas 아래 Panel 생성 (`RiskPanel`)
2. Panel 안에 Text 생성 (`RiskLabel`)
3. Canvas 또는 빈 UI 오브젝트에 `RiskWarningUI` 부착
   - terrainState: Player의 `PlayerTerrainState`
   - panel: `RiskPanel` Image
   - label: `RiskLabel` Text

4. Canvas 아래 작은 팝업 Panel 생성 (`TutorialPopupRoot`)
5. 그 안에 Text 생성 (`TutorialPopupText`)
6. Canvas에 `TutorialHintPopup` 부착
   - terrainState 연결
   - root: `TutorialPopupRoot`
   - hintText: `TutorialPopupText`

## 동작
- 위험도 표시: SAFE / CAUTION / DANGER
- 첫 Caution 진입 시 1회 팁
- 첫 Dangerous 진입 시 1회 팁

## 권장
- TextMeshPro로 전환 시 Text 타입을 TMP_Text로 변경
- 모바일 해상도별 앵커 설정(상단 중앙) 필수
