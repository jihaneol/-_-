#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
fi

mkdir -p build/local-stack

docker compose up -d mysql

admin_log="$ROOT/build/local-stack/admin-api.log"
shop_log="$ROOT/build/local-stack/shop-api.log"
admin_front_log="$ROOT/build/local-stack/admin-frontend.log"
shop_front_log="$ROOT/build/local-stack/shop-frontend.log"

pids=()

cleanup() {
  for pid in "${pids[@]:-}"; do
    if kill -0 "$pid" >/dev/null 2>&1; then
      kill "$pid" >/dev/null 2>&1 || true
    fi
  done
}
trap cleanup EXIT INT TERM

./gradlew :admin-api:bootRun --args='--server.port=8082' >"$admin_log" 2>&1 &
pids+=("$!")

./gradlew :shop-api:bootRun --args='--server.port=8081' >"$shop_log" 2>&1 &
pids+=("$!")

npm --prefix frontend run dev:admin >"$admin_front_log" 2>&1 &
pids+=("$!")

npm --prefix frontend run dev:shop >"$shop_front_log" 2>&1 &
pids+=("$!")

cat <<EOF
Local stack starting.

Admin frontend: http://127.0.0.1:5173/
Shop frontend:  http://127.0.0.1:5174/
Admin API:      http://127.0.0.1:8082/actuator/health
Shop API:       http://127.0.0.1:8081/actuator/health

Logs:
- $admin_log
- $shop_log
- $admin_front_log
- $shop_front_log

Press Ctrl-C to stop the local stack.
EOF

wait
