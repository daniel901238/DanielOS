# Compat Rule Priority

충돌 시 상단 규칙이 우선 적용됩니다.

1. 입력 정규화 (`$$` 제거)
2. 명시적 alias/rewrite (`egrep`, `fgrep`, `sed -r`, `awk --posix` 등)
3. 명령 fallback (`ss/netstat`, `realpath`, `which/whereis/locate` 등)
4. 경고성 래핑 (`grep -P`, `rm -rf`, `git -p` 등)
5. 제한 명령 처리 (`systemctl`, `reboot/poweroff/mount/modprobe`)
6. 매칭 없음 -> 원명령 실행

참고: 같은 명령에서 다중 패턴이 걸리면 파일 상의 첫 매칭이 적용됩니다.
