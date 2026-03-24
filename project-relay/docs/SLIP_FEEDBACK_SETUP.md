# Slip Feedback Setup (위험도별 경고음 + 슬립 피드백)

## 1) Player 설정
- `Player` 오브젝트에 `PlayerSlipFeedback` 컴포넌트 추가
- `playerLocomotion` -> `PlayerLocomotion` 연결
- `terrainState` -> `PlayerTerrainState` 연결
- `cameraTransform` -> 메인 카메라 Transform 연결
- `sfxSource` -> Player 또는 Camera의 AudioSource 연결

## 2) 위험도별 오디오 클립 연결
### Slip SFX by Risk
- `slipSafeClip`: 안전 구간 슬립 음
- `slipCautionClip`: 주의 구간 슬립 음
- `slipDangerClip`: 위험 구간 슬립 음

### Zone Enter Warning SFX
- `cautionEnterClip`: 주의 구간 진입 경고음
- `dangerEnterClip`: 위험 구간 진입 경고음

## 3) 권장 파라미터
- shakeDuration: `0.18`
- shakeMagnitude: `0.08` (멀미 있으면 `0.04`)
- useHapticOnMobile: `true`

## 4) 동작 확인
1. Safe -> Caution 구간 진입: 주의 진입음 재생
2. Caution -> Dangerous 진입: 위험 진입음 재생
3. 각 구간에서 슬립 발생 시 다른 슬립 음 재생
4. 슬립 발생 시 카메라 흔들림 + 모바일 진동

## 5) 참고
- 현재는 RiskLevel 변경 시에만 진입음이 재생됨(같은 구간 내 반복 stay에서는 재생 안 됨)
- Cinemachine 사용 시 카메라 흔들림을 노이즈 채널 기반으로 바꾸면 더 자연스러움
