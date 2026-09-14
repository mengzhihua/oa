#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8086}"
json() {
  curl -fsS "$@"
}
login() {
  echo "[步骤] 登录用户 $1" >&2
  json -H 'Content-Type: application/json' \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}" \
    "$BASE_URL/api/auth/login"
}
ok() {
  local response="$1"
  local code
  local message
  code="$(printf '%s' "$response" | jq -r '.code')"
  message="$(printf '%s' "$response" | jq -r '.msg')"
  echo "[返回] code=$code msg=$message" >&2
  test "$code" = "0"
}
token() {
  printf '%s' "$1" | jq -r '.data.token'
}

ADMIN="$(login admin admin123)"
AT="$(token "$ADMIN")"
AUTH=(-H "Authorization: Bearer $AT")
echo "[步骤] 查询当前用户" >&2
ok "$(json "${AUTH[@]}" "$BASE_URL/api/auth/me")"
echo "[步骤] 查询员工分页" >&2
ok "$(json "${AUTH[@]}" "$BASE_URL/api/hr/employees?page=1&size=20")"
echo "[步骤] 查询组织树" >&2
ok "$(json "${AUTH[@]}" "$BASE_URL/api/org/depts/tree")"

ZHANG="$(login zhangsan emp123)"
ZT="$(token "$ZHANG")"
ZAUTH=(-H "Authorization: Bearer $ZT")
echo "[步骤] 员工自动打卡" >&2
ok "$(json -X POST "${ZAUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"source":"WEB","remark":"smoke"}' "$BASE_URL/api/attendance/clock")"
echo "[步骤] 查询今日打卡" >&2
ok "$(json "${ZAUTH[@]}" "$BASE_URL/api/attendance/clock/today")"

echo "[步骤] 员工提交请假" >&2
LEAVE="$(json -X POST "${ZAUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"leaveType":"ANNUAL","startTime":"2026-09-14T09:00:00","endTime":"2026-09-14T18:00:00","reason":"smoke"}' \
  "$BASE_URL/api/attendance/leaves")"
ok "$LEAVE"
LEAVE_ID="$(printf '%s' "$LEAVE" | jq -r '.data.id')"

echo "[步骤] 查询主管待办" >&2
MANAGER="$(login manager mgr123)"
MT="$(token "$MANAGER")"
MAUTH=(-H "Authorization: Bearer $MT")
TODO="$(json "${MAUTH[@]}" "$BASE_URL/api/workflow/tasks/todo")"
ok "$TODO"
TASK_ID="$(printf '%s' "$TODO" | jq -r '.data[-1].id')"
echo "[步骤] 主管审批请假" >&2
ok "$(json -X POST "${MAUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"comment":"smoke同意"}' "$BASE_URL/api/workflow/tasks/$TASK_ID/approve")"
echo "[步骤] HR 重算日结" >&2
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/daily/recalc?from=2026-09-14&to=2026-09-14")"
echo "[步骤] 生成月度考勤" >&2
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/monthly/generate?yearMonth=2026-09")"
echo "[步骤] 确认月度考勤" >&2
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/monthly/2026-09/confirm")"
echo "[步骤] 锁定月度考勤" >&2
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/monthly/2026-09/lock")"

echo "[步骤] 开启工资期间" >&2
PERIOD="$(json -X POST "${AUTH[@]}" \
  "$BASE_URL/api/payroll/periods/open?yearMonth=2026-09")"
ok "$PERIOD"
PERIOD_ID="$(printf '%s' "$PERIOD" | jq -r '.data.id')"
echo "[步骤] 同步工资期间考勤锁定标记" >&2
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/attendance/monthly/2026-09/lock")"
FINANCE="$(login finance fin123)"
FT="$(token "$FINANCE")"
FAUTH=(-H "Authorization: Bearer $FT")
echo "[步骤] FINANCE 计算工资" >&2
ok "$(json -X POST "${FAUTH[@]}" "$BASE_URL/api/payroll/periods/$PERIOD_ID/calculate")"
echo "[步骤] FINANCE 审核工资" >&2
ok "$(json -X POST "${FAUTH[@]}" "$BASE_URL/api/payroll/periods/$PERIOD_ID/approve")"
echo "[步骤] FINANCE 发放工资" >&2
ok "$(json -X POST "${FAUTH[@]}" "$BASE_URL/api/payroll/periods/$PERIOD_ID/pay")"
echo "[步骤] 员工查询工资单" >&2
ok "$(json "${ZAUTH[@]}" "$BASE_URL/api/payroll/slips/mine")"

echo "[步骤] 创建公告" >&2
NOTICE="$(json -X POST "${AUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"title":"smoke公告","content":"smoke","type":"NOTICE"}' \
  "$BASE_URL/api/notices")"
ok "$NOTICE"
NOTICE_ID="$(printf '%s' "$NOTICE" | jq -r '.data.id')"
echo "[步骤] 发布公告" >&2
ok "$(json -X POST "${AUTH[@]}" "$BASE_URL/api/notices/$NOTICE_ID/publish")"
echo "[步骤] 查询未读消息" >&2
ok "$(json "${ZAUTH[@]}" "$BASE_URL/api/messages/unread-count")"
echo "[步骤] 查询工作台" >&2
ok "$(json "${ZAUTH[@]}" "$BASE_URL/api/dashboard")"

echo "[步骤] 创建会议室" >&2
ROOM="$(json -X POST "${AUTH[@]}" -H 'Content-Type: application/json' \
  -d '{"name":"smoke会议室","location":"A栋","capacity":8,"status":"ACTIVE"}' \
  "$BASE_URL/api/meetings/rooms")"
ok "$ROOM"
ROOM_ID="$(printf '%s' "$ROOM" | jq -r '.data.id')"
BOOKING='{"roomId":ROOM_ID,"title":"smoke","startTime":"2026-09-20T10:00:00","endTime":"2026-09-20T11:00:00"}'
BOOKING="${BOOKING/ROOM_ID/$ROOM_ID}"
echo "[步骤] 首次预约会议室" >&2
ok "$(json -X POST "${AUTH[@]}" -H 'Content-Type: application/json' \
  -d "$BOOKING" "$BASE_URL/api/meetings/bookings")"
echo "[步骤] 重复预约应返回冲突" >&2
CONFLICT="$(json -X POST "${AUTH[@]}" -H 'Content-Type: application/json' \
  -d "$BOOKING" "$BASE_URL/api/meetings/bookings")"
if test "$(printf '%s' "$CONFLICT" | jq -r '.code')" = "0"; then
  echo "会议室冲突校验失败" >&2
  exit 1
fi
echo "[返回] code=$(printf '%s' "$CONFLICT" | jq -r '.code') msg=$(printf '%s' "$CONFLICT" | jq -r '.msg')" >&2

echo "第二阶段冒烟检查完成"
