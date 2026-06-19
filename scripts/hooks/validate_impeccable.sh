#!/usr/bin/env bash
set -euo pipefail

node .agents/skills/impeccable/scripts/detect.mjs frontend/apps frontend/src
