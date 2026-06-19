#!/usr/bin/env bash
set -euo pipefail

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
fi

guarded_run() {
  local key="$*"

  python3 hooks/guard_command.py -- "$@"
  python3 hooks/circuit_breaker.py check --key "$key"

  set +e
  "$@"
  local status=$?
  set -e

  if [[ "$status" -eq 0 ]]; then
    python3 hooks/circuit_breaker.py record --key "$key" --status passed
    return 0
  fi

  python3 hooks/circuit_breaker.py record --key "$key" --status failed --summary "exit code $status"
  return "$status"
}

guarded_run python3 hooks/enforce_tdd.py
guarded_run ./gradlew test
