#!/usr/bin/env bash
set -euo pipefail

VUS="${VUS:-100}"
DURATION="${DURATION:-30s}"
PRODUCT_COUNT="${PRODUCT_COUNT:-1}"

export VUS
export DURATION
export PRODUCT_COUNT
export LOAD_TEST_RUN_ID="${LOAD_TEST_RUN_ID:-payment-after-kafka-${VUS}vus-${DURATION}-${PRODUCT_COUNT}products-$(date +%Y%m%d%H%M%S)}"

scripts/load-test-payment-before.sh
