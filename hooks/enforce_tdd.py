#!/usr/bin/env python3
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE_PREFIXES = ("modules/", "src/", "app/", "frontend/")
SOURCE_EXTENSIONS = (".kt", ".java", ".ts", ".tsx", ".js", ".jsx")
TEST_MARKERS = ("/src/test/", "/src/integrationTest/", "__tests__", ".spec.", ".test.")
DOC_PREFIXES = ("docs/", "harness/", ".codex/", "rules/")


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


def main() -> int:
    files = git_changed_files()
    source_changes = [path for path in files if is_source_file(path)]
    test_changes = [path for path in files if is_test_file(path)]

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
