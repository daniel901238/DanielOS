# DanielOS Shell (초기 프로토타입)

목표: Android/Termux 기반 DanielOS에서 Linux 기본 명령어 체감을 최대화.

## 현재 구현
- `danielsh` 인터랙티브 프롬프트 (`$ `)
- one-shot 실행: `danielsh "ls -al"`
- 파이프/리다이렉션/체이닝은 bash 위임으로 동작
- 호환 레이어:
  - `$$` 입력 시작 시 `$`로 정규화
  - `systemctl` 실행 시 환경 제약 안내
  - `free` 미존재 시 `/proc/meminfo` 대체
  - `ps aux`, `ip a`, `df -h` fallback
  - `which`, `whereis`, `locate` fallback
  - `grep -P` 사용 시 호환 경고
  - `xargs -r` 정규화, `stat -c` fallback, `readlink -f`/`realpath` fallback
  - `du -sh`, `ss`, `netstat` fallback
  - `tar --warning`, `rm -rf`, `chown`, `chmod +x`, `git -p` 사용 시 호환 경고

## 실행
```bash
cd /data/data/com.termux/files/home/.openclaw/workspace/danielos
node ./src/cli.js
# 또는
node ./src/cli.js "uname -a"
# 영어 경고/메시지 모드
DANIELOSH_LANG=en node ./src/cli.js "systemctl status"
```

## 테스트
```bash
npm run test:smoke
npm run test:compat
npm run test:en
npm run lint:warnings
npm run test:all
```

테스트 리포트는 `reports/latest.json`, `reports/latest-compat.json`, `reports/latest-en-snapshot.json`에 저장됨.

## 다음 단계
1. 내장 명령(alias/wrapper) 확장 (`ps aux`, `df -h`, `ip a` 등)
2. 명령별 옵션 호환 테이블 추가
3. 회귀 테스트 케이스 200개 구축
