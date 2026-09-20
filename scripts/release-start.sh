#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"
PORT="${SERVER_PORT:-8086}"
echo "Starting OA 协同办公 on http://127.0.0.1:$PORT"
exec java ${JAVA_OPTS:-} -jar oa-backend-1.0.0.jar --server.port="$PORT" 
