# DanielOS Shell Error Codes

- `0`: 성공
- `1`: 일반 실행 실패 (명령 실행은 되었으나 비정상 종료)
- `2`: 사용법/입력 데이터 오류 (예: tar 대상 파일 형식 불일치)
- `127`: 명령을 찾지 못함 또는 spawn 실패
- `130`: SIGINT 등 시그널 인터럽트

## 출력 규약
- 호환 규칙 적용 시: `[compat:<rule>]`
- 프로세스 종료코드(0 아님): `[danielsh:exit] code=<n>`
- 실행기 자체 에러: `[danielsh:error] <message>`
