#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

ADMIN_API_BASE_URL="${ADMIN_API_BASE_URL:-http://host.docker.internal:8082}"
SHOP_API_BASE_URL="${SHOP_API_BASE_URL:-http://host.docker.internal:8081}"
VUS="${VUS:-50}"
DURATION="${DURATION:-30s}"
STOCK="${STOCK:-100000}"
SLEEP_SECONDS="${SLEEP_SECONDS:-0}"
PRODUCT_COUNT="${PRODUCT_COUNT:-1}"
RESULTS_DIR="${LOAD_TEST_RESULTS_DIR:-$ROOT/build/load-tests}"
RUN_ID="${LOAD_TEST_RUN_ID:-payment-before-${VUS}vus-${DURATION}-${PRODUCT_COUNT}products-$(date +%Y%m%d%H%M%S)}"
SUMMARY_PATH="$RESULTS_DIR/${RUN_ID}.summary.json"
LOG_PATH="$RESULTS_DIR/${RUN_ID}.log"

mkdir -p "$RESULTS_DIR"

echo "Writing k6 summary to $SUMMARY_PATH"
echo "Writing k6 console log to $LOG_PATH"

docker run --rm \
  -v "$ROOT:/work" \
  -w /work \
  -e ADMIN_API_BASE_URL="$ADMIN_API_BASE_URL" \
  -e SHOP_API_BASE_URL="$SHOP_API_BASE_URL" \
  -e VUS="$VUS" \
  -e DURATION="$DURATION" \
  -e STOCK="$STOCK" \
  -e SLEEP_SECONDS="$SLEEP_SECONDS" \
  -e PRODUCT_COUNT="$PRODUCT_COUNT" \
  grafana/k6:latest \
  run --summary-export "/work/build/load-tests/${RUN_ID}.summary.json" load-tests/payment-spike-sync-projection.js \
  | tee "$LOG_PATH"
