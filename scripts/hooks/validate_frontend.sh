#!/usr/bin/env bash
set -euo pipefail

guarded_run() {
  local key="$*"

  python3 scripts/hooks/guard_command.py -- "$@"
  python3 scripts/hooks/circuit_breaker.py --lane frontend check --key "$key"

  set +e
  "$@"
  local status=$?
  set -e

  if [[ "$status" -eq 0 ]]; then
    python3 scripts/hooks/circuit_breaker.py --lane frontend record --key "$key" --status passed
    return 0
  fi

  python3 scripts/hooks/circuit_breaker.py --lane frontend record --key "$key" --status failed --summary "exit code $status"
  return "$status"
}

guarded_run python3 scripts/hooks/enforce_tdd.py --lane frontend
guarded_run npm --prefix frontend test -- --run
guarded_run npm --prefix frontend run build
guarded_run bash scripts/hooks/validate_impeccable.sh
