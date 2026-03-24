# Android Build Checklist v1 (Project Relay)

## 1) Unity 프로젝트 설정
- [ ] Build Settings > Android 선택 후 Switch Platform
- [ ] IL2CPP / ARM64 활성화
- [ ] Min SDK: Android 9 (API 28) 이상
- [ ] Scripting Backend: IL2CPP
- [ ] Target Architectures: ARM64 (필수), ARMv7(선택)

## 2) Player Settings
- [ ] Package Name 확정 (`com.yourstudio.projectrelay`)
- [ ] Version / Bundle Version Code 증가
- [ ] 앱 아이콘/스플래시 적용
- [ ] Internet Permission 필요 여부 확인

## 3) 성능/품질 기본
- [ ] Quality: Mobile 중간값 프리셋
- [ ] Target Frame Rate: 60 또는 30 고정
- [ ] 실시간 그림자 최소화
- [ ] 오디오 볼륨/믹서 클리핑 점검

## 4) 입력/UX
- [ ] 터치 조작 동작 확인
- [ ] 햅틱(진동) 기기 동작 확인
- [ ] 16:9 / 20:9 UI 앵커 확인

## 5) 빌드 산출물
- [ ] Debug APK 1회 생성
- [ ] Release AAB 생성
- [ ] 실제 기기 설치 테스트(최소 1대)

## 6) 출시 전 최소 QA
- [ ] 10분 플레이 시 크래시 없음
- [ ] 미션 시작/성공/실패 정상
- [ ] 슬립 사운드/경고 UI 정상
- [ ] 백그라운드 복귀 시 오디오 상태 정상
