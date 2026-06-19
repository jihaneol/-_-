#!/usr/bin/env bash
set -euo pipefail

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
fi

python3 hooks/enforce_tdd.py
./gradlew test
