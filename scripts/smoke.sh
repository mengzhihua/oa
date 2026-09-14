#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8086}"
json() {
  curl -fsS "$@"
}
login() {
  json -H 'Content-Type: application/json' \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}" \
    "$BASE_URL/api/auth/login"
}
ok() {
  test "$(printf '%s' "$1" | jq -r '.code')" = "0"
}
token() {
  printf '%s' "$1" | jq -r '.data.token'
}

ADMIN="$(login admin admin123)"
AT="$(token "$ADMIN")"
AUTH=(-H "Authorization: Bearer $AT")
ok "$(json "${AUTH[@]}" "$BASE_URL/api/auth/me")"
ok "$(json "${AUTH[@]}" "$BASE_URL/api/hr/employees?page=1&size=20")"
ok "$(json "${AUTH[@]}" "$BASE_URL/api/org/depts/tree")"

ZHANG="$(login zhangsan emp123)"
ZT="$(token "$ZHANG")"
ZAUTH=(-H "Authorization: Bearer $ZT")
ok "$(json -X POST "${ZAUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"source":"WEB","remark":"smoke"}' "$BASE_URL/api/attendance/clock")"
ok "$(json "${ZAUTH[@]}" "$BASE_URL/api/attendance/clock/today")"

LEAVE="$(json -X POST "${ZAUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"leaveType":"ANNUAL","startTime":"2026-09-14T09:00:00","endTime":"2026-09-14T18:00:00","reason":"smoke"}' \
  "$BASE_URL/api/attendance/leaves")"
ok "$LEAVE"
LEAVE_ID="$(printf '%s' "$LEAVE" | jq -r '.data.id')"

MANAGER="$(login manager mgr123)"
MT="$(token "$MANAGER")"
MAUTH=(-H "Authorization: Bearer $MT")
TODO="$(json "${MAUTH[@]}" "$BASE_URL/api/workflow/tasks/todo")"
ok "$TODO"
TASK_ID="$(printf '%s' "$TODO" | jq -r '.data[-1].id')"
ok "$(json -X POST "${MAUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"comment":"smoke同意"}' "$BASE_URL/api/workflow/tasks/$TASK_ID/approve")"
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/daily/recalc?from=2026-09-14&to=2026-09-14")"
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/monthly/generate?yearMonth=2026-09&deptId=2")"
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/monthly/2026-09/confirm")"
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/monthly/2026-09/lock")"

PERIOD="$(json -X POST "${AUTH[@]}" \
  "$BASE_URL/api/payroll/periods/open?yearMonth=2026-09")"
ok "$PERIOD"
PERIOD_ID="$(printf '%s' "$PERIOD" | jq -r '.data.id')"
FINANCE="$(login finance fin123)"
FT="$(token "$FINANCE")"
FAUTH=(-H "Authorization: Bearer $FT")
ok "$(json -X POST "${FAUTH[@]}" "$BASE_URL/api/payroll/periods/$PERIOD_ID/calculate")"
ok "$(json -X POST "${FAUTH[@]}" "$BASE_URL/api/payroll/periods/$PERIOD_ID/approve")"
ok "$(json -X POST "${FAUTH[@]}" "$BASE_URL/api/payroll/periods/$PERIOD_ID/pay")"
ok "$(json "${ZAUTH[@]}" "$BASE_URL/api/payroll/slips/mine")"

NOTICE="$(json -X POST "${AUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"title":"smoke公告","content":"smoke","type":"NOTICE"}' \
  "$BASE_URL/api/notices")"
ok "$NOTICE"
NOTICE_ID="$(printf '%s' "$NOTICE" | jq -r '.data.id')"
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/notices/$NOTICE_ID/publish")"
ok "$(json "${ZAUTH[@]}" "$BASE_URL/api/messages/unread-count")"
ok "$(json "${ZAUTH[@]}" "$BASE_URL/api/dashboard")"

ROOM="$(json -X POST "${AUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"name":"smoke会议室","location":"A栋","capacity":8,"status":"ACTIVE"}' \
  "$BASE_URL/api/meetings/rooms")"
ok "$ROOM"
ROOM_ID="$(printf '%s' "$ROOM" | jq -r '.data.id')"
BOOKING='{"roomId":ROOM_ID,"title":"smoke","startTime":"2026-09-20T10:00:00","endTime":"2026-09-20T11:00:00"}'
BOOKING="${BOOKING/ROOM_ID/$ROOM_ID}"
ok "$(json -X POST "${AUTH[@]}" -H 'Content-Type: application/json' \
  -d "$BOOKING" "$BASE_URL/api/meetings/bookings")"
CONFLICT="$(json -X POST "${AUTH[@]}" -H 'Content-Type: application/json' \
  -d "$BOOKING" "$BASE_URL/api/meetings/bookings")"
if test "$(printf '%s' "$CONFLICT" | jq -r '.code')" = "0"; then
  echo "会议室冲突校验失败" >&2
  exit 1
fi

echo "第二阶段冒烟检查完成"
