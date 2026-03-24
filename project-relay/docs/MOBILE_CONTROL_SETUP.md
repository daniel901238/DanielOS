# Mobile Control Setup (터치 스틱 + 한손 모드)

## 추가 스크립트
- `Player/MobileInputBridge.cs`
- `UI/JoystickLikeInput.cs`

## 연결 순서
1. Canvas에 조이스틱 베이스 이미지 생성 (`JoystickBase`)
2. 자식으로 노브 이미지 생성 (`JoystickKnob`)
3. `JoystickBase`에 `JoystickLikeInput` 부착
   - knob에 `JoystickKnob` 연결
4. `Player`에 `MobileInputBridge` 부착
   - joystick에 `JoystickBase`의 `JoystickLikeInput` 연결
   - oneHandAutoForward 필요시 ON

## 참고
- 현재 `PlayerLocomotion`은 기본 Input축 사용 중
- 다음 단계에서 `MobileInputBridge.MoveInput`을 읽도록 연결하면 모바일 완전 대응
