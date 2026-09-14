#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8086}"
json() { curl -fsS "$@"; }
login() { json -H 'Content-Type: application/json' -d "{\"username\":\"$1\",\"password\":\"$2\"}" "$BASE_URL/api/auth/login"; }
ADMIN="$(login admin admin123)"
TOKEN="$(printf '%s' "$ADMIN" | jq -r '.data.token')"
test -n "$TOKEN" && test "$TOKEN" != null
AUTH=(-H "Authorization: Bearer $TOKEN")
json "${AUTH[@]}" "$BASE_URL/api/auth/me" | jq .
json "${AUTH[@]}" "$BASE_URL/api/hr/employees" | jq .
json "${AUTH[@]}" "$BASE_URL/api/org/depts/tree" | jq .
json "${AUTH[@]}" "$BASE_URL/api/workflow/definitions" | jq .
ZHANG="$(login zhangsan emp123)"
ZT="$(printf '%s' "$ZHANG" | jq -r '.data.token')"
INSTANCE="$(curl -fsS -H "Authorization: Bearer $ZT" -H 'Content-Type: application/json' -d '{"title":"通用申请演示","businessType":"GENERAL","form":{"reason":"阶段一验收"}}' "$BASE_URL/api/workflow/instances/start")"
printf '%s\n' "$INSTANCE" | jq .
INSTANCE_ID="$(printf '%s' "$INSTANCE" | jq -r '.data.id')"
MANAGER="$(login manager mgr123)"
MT="$(printf '%s' "$MANAGER" | jq -r '.data.token')"
curl -fsS -H "Authorization: Bearer $MT" "$BASE_URL/api/workflow/tasks/todo" | jq .
TASK_ID="$(curl -fsS -H "Authorization: Bearer $MT" "$BASE_URL/api/workflow/tasks/todo" | jq -r '.data[-1].id')"
curl -fsS -H "Authorization: Bearer $MT" -H 'Content-Type: application/json' -d '{"comment":"同意"}' "$BASE_URL/api/workflow/tasks/$TASK_ID/approve" | jq .
curl -fsS -H "Authorization: Bearer $ZT" "$BASE_URL/api/workflow/instances/$INSTANCE_ID" | jq '.data.status'
echo "阶段一冒烟检查完成"
