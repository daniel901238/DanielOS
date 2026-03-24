# DanielOS Compat Rules (v0.5)

## 입력 정규화
- `^$$\s*` -> 제거 (프롬프트 중복 입력 보정)

## Alias / Rewrite
- `ll` -> `ls -alF`
- `egrep ...` -> `grep -E ...`
- `fgrep ...` -> `grep -F ...`
- `sed -r` -> `sed -E`
- `awk --posix` -> `--posix` 제거 후 실행
- `xargs -r` -> `-r` 제거 후 실행

## Fallback
- `ps aux` 실패 -> `ps -ef`
- `ip a|ip addr`에서 `ip` 없으면 `ifconfig`
- `df -h` 실패 -> `df`
- `du -sh` 실패 -> `du -s`
- `ss` 없으면 `netstat -tunlp`
- `netstat` 없으면 `ss -tunlp`
- `free` 없으면 `/proc/meminfo` 출력
- `which <cmd>` -> `command -v`
- `whereis <cmd>` -> `command -v` 기반 경로 출력
- `locate <query>` 없으면 `find . -iname '*query*' | head -n 200`
- `stat -c ...` 미지원 시 `stat` 기본 출력으로 대체
- `readlink -f <path>` 미지원 시 `cd <path> && pwd -P` 대체
- `realpath <path>` 미지원 시 `readlink -f`/`pwd -P` 대체

## Warning only
- `grep -P`
- `find -printf`
- `find -regextype`
- `awk -W posix`
- `tar --warning`
- `rm -rf`
- `cp/mv` 옵션 충돌 (`-n/-i/-f`)
- `ln -s`
- `install`
- `touch -d`
- `chown`
- `chmod +x`
- `git -p`

## 제한 명령
- `systemctl` -> 미지원 안내 + 실패 코드(1)
- `reboot/poweroff/mount/modprobe` -> 제약 안내 후 원명령 실행
