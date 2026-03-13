# DanielOS Quick Start - Next Steps

## 1) 문서 실값 반영 (한 줄 실행)
```bash
cd ~/.openclaw/workspace/DanielOS
bash scripts/prepare-release-metadata.sh <SUPPORT_EMAIL> <WEBSITE_URL> <PRIVACY_URL>
```

예시:
```bash
bash scripts/prepare-release-metadata.sh support@mydomain.com https://mydomain.com https://mydomain.com/privacy
```

## 2) GitHub 원격 연결/푸시
```bash
cd ~/.openclaw/workspace
# 이미 origin 있으면 set-url 사용
git remote add origin <YOUR_GITHUB_REPO_URL>
git push -u origin master
```

## 3) GitHub Actions 실행
- Actions > DanielOS Android Build > Run workflow
- 아티팩트 다운로드(debug apk / unsigned aab)

## 4) 서명 릴리즈(선택)
- Repo Secrets 등록 후
- Actions > DanielOS Android Release (Signed) > Run workflow
