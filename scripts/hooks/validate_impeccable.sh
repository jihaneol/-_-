#!/usr/bin/env bash
set -euo pipefail

node .agents/skills/impeccable/scripts/detect.mjs frontend/admin frontend/shop frontend/shared
