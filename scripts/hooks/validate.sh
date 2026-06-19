#!/usr/bin/env bash
set -euo pipefail

lane="${1:-backend}"

case "$lane" in
  backend)
    scripts/hooks/validate_backend.sh
    ;;
  frontend)
    scripts/hooks/validate_frontend.sh
    ;;
  *)
    echo "Unknown validation lane: $lane" >&2
    exit 1
    ;;
esac
