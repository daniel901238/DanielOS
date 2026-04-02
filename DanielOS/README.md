# DanielOS

DanielOS는 Termux 대체를 목표로 하는 안드로이드 개발용 터미널 APK 프로젝트입니다.

## 목표 (v0.1 MVP)
- 터미널 화면(입력/출력)
- 로컬 셸 프로세스 실행
- 세션 시작/중지
- 기본 설정 화면
- OpenClaw 연동 준비 포인트

## 현재 상태 (v1.0.0-beta)
- 인터랙티브 셸 세션 동작
- 세션 제어(재시작/중단), 히스토리 탐색
- 로그 저장/복사, 상태 복구
- PTY 대응 스켈레톤 구현(현재 로컬 셸 폴백)
- 출시 문서 패키지(법무/정책/스토어) 포함

## 남은 우선순위
1. PTY 네이티브 브리지 실제 연결
2. v1.3 real terminal mode (단일 캔버스 직접 입력)
3. Android SDK 환경에서 릴리즈 AAB 빌드/서명 검증
4. Play Console 등록 정보 최종 반영
5. 실기기 안정화 테스트 확장

## OpenClaw 설치/테스트 (앱 터미널 명령)
앱 터미널에서 아래 내장 명령을 사용할 수 있습니다.

- `openclaw-install` : DanielOS 앱 내부에 OpenClaw 런처(`$HOME/.danielos/bin/openclaw-danielos`) 생성
- `openclaw-test` : 앱 내부 런처로 버전 확인 + `gateway --help` 동작 테스트

설치 후 직접 실행 예시:
- `~/.danielos/bin/openclaw-danielos --version`
- `~/.danielos/bin/openclaw-danielos gateway --help`

## 원격 빌드 (GitHub Actions)
로컬 SDK 없이 원격에서 APK/AAB 빌드할 수 있습니다.

### 기본 빌드
- 워크플로우: `.github/workflows/android-build.yml`
- 결과물:
  - `danielos-debug-apk`
  - `danielos-release-aab-unsigned`

### 서명 릴리즈 빌드
- 워크플로우: `.github/workflows/android-release-sign.yml`
- 필요한 GitHub Secrets:
  - `ANDROID_KEYSTORE_BASE64`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEYSTORE_PASSWORD`
  - `ANDROID_KEY_PASSWORD`
