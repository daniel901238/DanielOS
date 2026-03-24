# DanielOS Linux 기본 명령어 호환 개발 계획

## 목표
- DanielOS에서 사용자가 체감하는 `기본 Linux 명령어`를 가능한 한 그대로 실행
- Android/Termux 제약으로 100% 커널/시스템 명령 호환은 불가하므로, **사용자 공간 명령어 1차 완전 호환**을 목표로 함

## 1차 범위 (MVP)
- 파일/텍스트: `ls`, `cp`, `mv`, `rm`, `mkdir`, `cat`, `grep`, `sed`, `awk`, `find`, `head`, `tail`, `sort`, `uniq`, `cut`, `xargs`
- 프로세스: `ps`, `kill`, `pkill`, `top`(가벼운 모드)
- 시스템/네트워크: `uname`, `whoami`, `id`, `df`, `du`, `free`, `ping`, `curl`, `wget`
- 압축: `tar`, `gzip`, `unzip`

## 아키텍처
1. **명령 해석기(파서)**
   - 공백/인용부호/파이프/리다이렉션 처리
   - `&&`, `||`, `;` 체이닝
2. **실행 엔진**
   - 우선순위: 내장(alias/wrapper) → PATH 바이너리
   - PATH에 coreutils/toybox/termux 패키지 경로 포함
3. **호환 레이어(wrapper)**
   - Linux와 Android 차이 나는 옵션 보정
   - 예: `ps aux` → Android 가능한 형태로 변환
4. **에러 매핑**
   - 표준 bash 스타일 오류 메시지로 정규화

## 제약/예외
- `systemctl`, `modprobe`, `mount` 같은 루트/커널 제어 명령은 완전 호환 불가
- 해당 명령은 대체 안내 메시지 또는 no-op wrapper 제공

## 개발 단계
- P1: 명령 파서 + 단일 명령 실행 + PATH 탐색
- P2: 파이프/리다이렉션/체이닝 구현
- P3: 호환 wrapper 20개 추가
- P4: 회귀 테스트(명령 200케이스)

## 완료 기준
- 1차 범위 명령 중 90% 이상 성공
- 대표 시나리오(파일탐색/로그검색/네트워크확인) 모두 동작
