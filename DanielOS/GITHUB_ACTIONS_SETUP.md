# DanielOS GitHub Actions Setup

## 1) 코드 업로드
```bash
git remote add origin <your-repo-url>
git push -u origin master
```

## 2) 기본 원격 빌드 실행
- GitHub → Actions → **DanielOS Android Build** → Run workflow
- 산출물:
  - `danielos-debug-apk`
  - `danielos-release-aab-unsigned`

## 3) 서명 릴리즈 빌드 준비 (Secrets)
Repo Settings → Secrets and variables → Actions → New repository secret

필수 시크릿:
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_PASSWORD`

## 4) 서명 릴리즈 실행
- GitHub → Actions → **DanielOS Android Release (Signed)** → Run workflow
- 산출물:
  - `danielos-release-aab-signed`

## 5) 출시 전 확인
- 실제 이메일/도메인 반영
- Privacy URL 공개 접근 확인
- Play Data Safety 입력값과 앱 동작 정합성 확인
