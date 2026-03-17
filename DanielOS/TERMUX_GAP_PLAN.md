# DanielOS → Termux Gap Plan

## Goal
DanielOS를 "커스텀 UI" 수준에서 "실사용 가능한 터미널 플랫폼" 수준으로 끌어올린다.

## Priority 1 (core usability)
1. Session readiness gating
   - shell started 전 명령 입력 큐잉/차단
2. Command result UX
   - 마지막 exit code 표시/에러 강조
3. Working directory UX
   - 현재 경로 표시, cd 후 상태 표시 개선

## Priority 2 (runtime capability)
1. Linux userspace mode
   - proot 기반 rootfs 연결(선택형)
2. Toolchain bootstrap
   - 최소 패키지 세트(ssh/git/python/node) 자동 설치 스크립트
3. Profile switch
   - sandbox shell / linux userspace mode 전환

## Priority 3 (package and ecosystem)
1. Package abstraction layer
   - apt/pkg/proot apt wrapper
2. Repo mirror config UI
3. install/remove/search history

## Priority 4 (stability & release)
1. crash/ANR regression suite
2. background/foreground lifecycle hardening
3. signed AAB pipeline + Play preflight checks

## Immediate next implementation (v1.1)
- session started 전 run button 비활성화
- queued command 처리
- command 완료 시 exit code 명시
- status line 추가(current dir, mode)
