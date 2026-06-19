#!/usr/bin/env python3
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
ALLOWED_PREFIXES = ("docs/", "back/", "front/", "common/")
DOC_PREFIXES = ("docs/", "workflow/", "rules/")
FRONT_PREFIXES = ("frontend/", "workflow/frontend/")
BACK_PREFIXES = ("modules/", "sql/", "workflow/backend/")
COMMON_ALLOWED_PREFIXES = (
    ".agents/",
    ".githooks/",
    ".codex/hooks.json",
    ".impeccable/",
    "gradle/",
    "scripts/",
    "AGENT.md",
    "README.md",
    "docker-compose.yml",
    "gradlew",
    "gradlew.bat",
    "settings.gradle.kts",
    "build.gradle.kts",
)
COMMON_BLOCKED_PREFIXES = DOC_PREFIXES + FRONT_PREFIXES + BACK_PREFIXES


def git(args: list[str]) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["git", *args],
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )


def current_branch() -> str:
    result = git(["symbolic-ref", "--quiet", "--short", "HEAD"])
    return result.stdout.strip()


def staged_files() -> list[str]:
    result = git(["diff", "--cached", "--name-only", "--diff-filter=ACMR"])
    return [line.strip() for line in result.stdout.splitlines() if line.strip()]


def starts_with_any(path: str, prefixes: tuple[str, ...]) -> bool:
    return path.startswith(prefixes) or path in prefixes


def branch_kind(branch: str) -> str | None:
    for prefix in ALLOWED_PREFIXES:
        if branch.startswith(prefix):
            return prefix.removesuffix("/")
    return None


def invalid_files_for(kind: str, files: list[str]) -> list[str]:
    if kind == "docs":
        return [path for path in files if not starts_with_any(path, DOC_PREFIXES)]
    if kind == "back":
        return [path for path in files if starts_with_any(path, FRONT_PREFIXES)]
    if kind == "front":
        return [path for path in files if starts_with_any(path, BACK_PREFIXES)]
    if kind == "common":
        return [
            path for path in files
            if starts_with_any(path, COMMON_BLOCKED_PREFIXES) or not starts_with_any(path, COMMON_ALLOWED_PREFIXES)
        ]
    return files


def main() -> int:
    branch = current_branch()
    if not branch:
        return 0
    if branch == "main":
        print("Branch guard failed: commit on main is not allowed.", file=sys.stderr)
        print("Use docs/*, back/*, front/*, or common/*.", file=sys.stderr)
        return 1

    kind = branch_kind(branch)
    if kind is None:
        print(f"Branch guard failed: invalid branch name `{branch}`.", file=sys.stderr)
        print("Allowed prefixes: docs/*, back/*, front/*, common/*", file=sys.stderr)
        return 1

    files = staged_files()
    invalid = invalid_files_for(kind, files)
    if invalid:
        print(f"Branch guard failed: `{branch}` cannot commit these paths:", file=sys.stderr)
        for path in invalid:
            print(f"- {path}", file=sys.stderr)
        print("", file=sys.stderr)
        print("Branch path rules:", file=sys.stderr)
        print("- docs/*: docs, workflow phase files, and rules only", file=sys.stderr)
        print("- back/*: backend work; frontend paths are blocked", file=sys.stderr)
        print("- front/*: frontend work; backend paths are blocked", file=sys.stderr)
        print("- common/*: non-docs, non-backend, non-frontend tooling and root config only", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
