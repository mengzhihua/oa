#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8086}"
json() { curl -fsS "$@"; }
login() { json -H 'Content-Type: application/json' -d "{\"username\":\"$1\",\"password\":\"$2\"}" "$BASE_URL/api/auth/login"; }
assert_success() { test "$(printf '%s' "$1" | jq -r '.code')" = 0; }
oauth_basic() {
  printf '%s' "sap-client:sap-client-secret" | base64
}
ADMIN="$(login admin admin123)"
TOKEN="$(printf '%s' "$ADMIN" | jq -r '.data.token')"
test -n "$TOKEN" && test "$TOKEN" != null
AUTH=(-H "Authorization: Bearer $TOKEN")
ME="$(json "${AUTH[@]}" "$BASE_URL/api/auth/me")"
assert_success "$ME"
printf '%s\n' "$ME" | jq .
EMPLOYEES="$(json "${AUTH[@]}" "$BASE_URL/api/hr/employees")"
assert_success "$EMPLOYEES"
printf '%s\n' "$EMPLOYEES" | jq .
DEPTS="$(json "${AUTH[@]}" "$BASE_URL/api/org/depts/tree")"
assert_success "$DEPTS"
printf '%s\n' "$DEPTS" | jq .
DEFINITIONS="$(json "${AUTH[@]}" "$BASE_URL/api/workflow/definitions")"
assert_success "$DEFINITIONS"
printf '%s\n' "$DEFINITIONS" | jq .
ZHANG="$(login zhangsan emp123)"
ZT="$(printf '%s' "$ZHANG" | jq -r '.data.token')"
test -n "$ZT" && test "$ZT" != null
INSTANCE="$(curl -fsS -H "Authorization: Bearer $ZT" -H 'Content-Type: application/json' -d '{"definitionCode":"GENERAL","title":"通用申请演示","businessType":"GENERAL","form":{"reason":"阶段一验收"}}' "$BASE_URL/api/workflow/instances/start")"
assert_success "$INSTANCE"
printf '%s\n' "$INSTANCE" | jq .
INSTANCE_ID="$(printf '%s' "$INSTANCE" | jq -r '.data.id')"
test -n "$INSTANCE_ID" && test "$INSTANCE_ID" != null
MANAGER="$(login manager mgr123)"
MT="$(printf '%s' "$MANAGER" | jq -r '.data.token')"
test -n "$MT" && test "$MT" != null
TODO="$(curl -fsS -H "Authorization: Bearer $MT" "$BASE_URL/api/workflow/tasks/todo")"
assert_success "$TODO"
printf '%s\n' "$TODO" | jq .
TASK_ID="$(printf '%s' "$TODO" | jq -r '.data[-1].id')"
test -n "$TASK_ID" && test "$TASK_ID" != null
APPROVE="$(curl -fsS -H "Authorization: Bearer $MT" -H 'Content-Type: application/json' -d '{"comment":"同意"}' "$BASE_URL/api/workflow/tasks/$TASK_ID/approve")"
assert_success "$APPROVE"
printf '%s\n' "$APPROVE" | jq .
INSTANCE_DETAIL="$(curl -fsS -H "Authorization: Bearer $ZT" "$BASE_URL/api/workflow/instances/$INSTANCE_ID")"
assert_success "$INSTANCE_DETAIL"
test "$(printf '%s' "$INSTANCE_DETAIL" | jq -r '.data.status')" = APPROVED
printf '%s\n' "$INSTANCE_DETAIL" | jq '.data.status'

AUTHORIZE="$(json -H "Authorization: Bearer $ZT" "$BASE_URL/api/oauth/authorize?response_type=code&client_id=sap-client&redirect_uri=http%3A%2F%2Flocalhost%3A5175%2Fsso%2Fcallback&scope=openid%20profile%20email%20phone%20roles")"
printf '%s\n' "$AUTHORIZE" | jq .
REDIRECT_URL="$(printf '%s' "$AUTHORIZE" | jq -r '.redirectUrl')"
CODE="${REDIRECT_URL#*code=}"
OAUTH_TOKEN_RESPONSE="$(json -X POST \
  -d "grant_type=authorization_code&client_id=sap-client&client_secret=sap-client-secret&code=$CODE&redirect_uri=http%3A%2F%2Flocalhost%3A5175%2Fsso%2Fcallback" \
  "$BASE_URL/api/oauth/token")"
printf '%s\n' "$OAUTH_TOKEN_RESPONSE" | jq .
OAUTH_ACCESS="$(printf '%s' "$OAUTH_TOKEN_RESPONSE" | jq -r '.access_token')"
OAUTH_REFRESH="$(printf '%s' "$OAUTH_TOKEN_RESPONSE" | jq -r '.refresh_token')"
json -H "Authorization: Bearer $OAUTH_ACCESS" "$BASE_URL/api/oauth/userinfo" | jq .
INTROSPECT="$(json -X POST -H "Authorization: Basic $(oauth_basic)" \
  -d "token=$OAUTH_ACCESS" "$BASE_URL/api/oauth/introspect")"
printf '%s\n' "$INTROSPECT" | jq .
test "$(printf '%s' "$INTROSPECT" | jq -r '.active')" = true
ROTATED="$(json -X POST \
  -d "grant_type=refresh_token&client_id=sap-client&client_secret=sap-client-secret&refresh_token=$OAUTH_REFRESH" \
  "$BASE_URL/api/oauth/token")"
printf '%s\n' "$ROTATED" | jq .
OLD_REFRESH="$(curl -sS -X POST \
  -d "grant_type=refresh_token&client_id=sap-client&client_secret=sap-client-secret&refresh_token=$OAUTH_REFRESH" \
  "$BASE_URL/api/oauth/token")"
printf '%s\n' "$OLD_REFRESH" | jq .
test "$(printf '%s' "$OLD_REFRESH" | jq -r '.error')" = invalid_grant
ROTATED_ACCESS="$(printf '%s' "$ROTATED" | jq -r '.access_token')"
json -X POST -H "Authorization: Basic $(oauth_basic)" \
  -d "token=$ROTATED_ACCESS" "$BASE_URL/api/oauth/revoke" | jq .
REVOKED="$(json -X POST -H "Authorization: Basic $(oauth_basic)" \
  -d "token=$ROTATED_ACCESS" "$BASE_URL/api/oauth/introspect")"
printf '%s\n' "$REVOKED" | jq .
test "$(printf '%s' "$REVOKED" | jq -r '.active')" = false
echo "阶段一冒烟检查完成"
