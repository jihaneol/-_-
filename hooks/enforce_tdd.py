#!/usr/bin/env python3
import subprocess
import sys
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
STATE_FILE = ROOT / "harness" / "state" / "execute-state.json"
PHASE_DIR = ROOT / "harness" / "phases"
ARCHIVE_DIR = ROOT / "harness" / "archive"
SOURCE_PREFIXES = ("modules/", "src/", "app/", "frontend/")
SOURCE_EXTENSIONS = (".kt", ".java", ".ts", ".tsx", ".js", ".jsx")
TEST_MARKERS = ("/src/test/", "/src/integrationTest/", "__tests__", ".spec.", ".test.")
DOC_PREFIXES = ("docs/", "harness/", ".codex/", "rules/", "hooks/")


def git_changed_files() -> list[str]:
    result = subprocess.run(
        ["git", "status", "--short"],
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    files = []
    for line in result.stdout.splitlines():
        if not line.strip():
            continue
        path = line[3:].strip()
        if " -> " in path:
            path = path.split(" -> ", 1)[1]
        files.append(path)
    return files


def is_test_file(path: str) -> bool:
    return any(marker in path for marker in TEST_MARKERS)


def is_source_file(path: str) -> bool:
    if path.startswith(DOC_PREFIXES):
        return False
    return path.startswith(SOURCE_PREFIXES) and path.endswith(SOURCE_EXTENSIONS) and not is_test_file(path)


def load_state() -> dict:
    if not STATE_FILE.exists():
        return {"current_phase": None, "phases": {}}
    return json.loads(STATE_FILE.read_text())


def find_phase_path(phase_name: str) -> Path | None:
    active = PHASE_DIR / phase_name
    if active.exists():
        return active
    matches = sorted(ARCHIVE_DIR.glob(f"**/{phase_name}")) if ARCHIVE_DIR.exists() else []
    return matches[0] if matches else None


def section_text(text: str, heading: str) -> str:
    pattern = re.compile(rf"^## {re.escape(heading)}\s*$", re.MULTILINE)
    match = pattern.search(text)
    if not match:
        return ""
    start = match.end()
    next_match = re.search(r"^##\s+", text[start:], re.MULTILINE)
    end = start + next_match.start() if next_match else len(text)
    return text[start:end].strip()


def meaningful_section(text: str, heading: str) -> bool:
    content = section_text(text, heading).strip()
    return bool(content and content not in {"-", "None", "N/A"})


def main() -> int:
    files = git_changed_files()
    source_changes = [path for path in files if is_source_file(path)]
    test_changes = [path for path in files if is_test_file(path)]

    if source_changes:
        state = load_state()
        current_phase = state.get("current_phase")
        if not current_phase:
            print("TDD guard failed: source files changed without an active phase.", file=sys.stderr)
            print("Start a phase with `python3 execute.py start` before production edits.", file=sys.stderr)
            return 1

        phase_path = find_phase_path(current_phase)
        if not phase_path:
            print(f"TDD guard failed: active phase file not found: {current_phase}", file=sys.stderr)
            return 1

        phase_text = phase_path.read_text()
        if not meaningful_section(phase_text, "Test First") and not meaningful_section(phase_text, "TDD Exception"):
            print("TDD guard failed: active source phase has no Test First plan.", file=sys.stderr)
            print(f"Phase: {current_phase}", file=sys.stderr)
            print("Add `## Test First` or an explicit `## TDD Exception` before production edits.", file=sys.stderr)
            return 1

    if source_changes and not test_changes:
        print("TDD guard failed: source files changed without test changes.", file=sys.stderr)
        print("Write or update a failing test first, then change production code.", file=sys.stderr)
        print("", file=sys.stderr)
        print("Source changes:", file=sys.stderr)
        for path in source_changes:
            print(f"- {path}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
