#!/usr/bin/env python3
import argparse
import json
import re
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
LANES = {"backend", "frontend"}
RESPONSE_MAPPER = re.compile(
    r"fun\s+\w*Result\.toResponse\(\)\s*:\s*\w*Response\s*=\s*\w*Response\((?P<args>.*?)\)",
    re.MULTILINE | re.DOTALL,
)
PLAIN_ARGUMENT = re.compile(r"^(?:\w+\s*=\s*)?(?:this\.)?\w+$")
UNTYPED_API_RESPONSE = re.compile(r"\b(?:ResponseEntity<\s*)?ApiResponse<\s*Any\s*>")
TOP_LEVEL_LIST_API_RESPONSE = re.compile(r"\b(?:ResponseEntity<\s*)?ApiResponse<\s*List\s*<")


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


def load_json(path: Path) -> dict:
    if not path.exists():
        return {"current_phase": None, "phases": {}}
    return json.loads(path.read_text())


def audit_lane_state(lane: str, strict: bool) -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    phase_dir = ROOT / "workflow" / lane / "phases"
    archive_dir = ROOT / "workflow" / lane / "archive"
    state_file = ROOT / "workflow" / lane / "state" / "execute-state.json"
    state = load_json(state_file)
    phases = state.setdefault("phases", {})

    active_files = sorted(path.name for path in phase_dir.glob("*.md"))
    archived_files = {
        path.name: path
        for path in archive_dir.glob("**/*.md")
        if path.name.startswith("phase-")
    }

    current = state.get("current_phase")
    if current and current not in active_files and current not in archived_files:
        errors.append(f"{lane}: current_phase points to a missing phase: {current}")

    for name in active_files:
        item = phases.get(name)
        if not item:
            errors.append(f"{lane}: active phase is not registered in execute-state.json: {name}")
            continue
        if item.get("status") == "completed":
            errors.append(f"{lane}: completed phase still remains active: {name}")

    for name, item in phases.items():
        status = item.get("status")
        archive_path = item.get("archive_path")
        if status == "completed":
            if not archive_path:
                errors.append(f"{lane}: completed phase has no archive_path: {name}")
            elif not (ROOT / archive_path).exists():
                errors.append(f"{lane}: completed phase archive_path does not exist: {name} -> {archive_path}")
            validation = item.get("last_validation") or {}
            if validation.get("status") not in {"passed", "superseded", "deferred-backlog"}:
                errors.append(f"{lane}: completed phase has no accepted validation status: {name}")
        if strict and status == "missing" and name in archived_files:
            errors.append(f"{lane}: phase is archived but state still says missing: {name}")
        elif status == "missing" and name in archived_files:
            warnings.append(f"{lane}: phase is archived but state still says missing: {name}")

    if strict:
        for name, path in archived_files.items():
            item = phases.get(name)
            if not item:
                errors.append(f"{lane}: archived phase is missing from execute-state.json: {path.relative_to(ROOT)}")
            elif item.get("status") != "completed":
                errors.append(f"{lane}: archived phase is not completed in state: {name} ({item.get('status')})")
    else:
        missing_archives = sorted(name for name in archived_files if name not in phases)
        if missing_archives:
            warnings.append(f"{lane}: {len(missing_archives)} archived phase(s) are not recorded in state; run strict audit before final handoff")

    return errors, warnings


def audit_response_mappers(all_files: bool) -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    if all_files:
        paths = list((ROOT / "modules").glob("**/*.kt"))
    else:
        paths = [ROOT / path for path in git_changed_files() if path.endswith(".kt")]

    for path in paths:
        if not path.exists() or "/response/" not in str(path):
            continue
        text = path.read_text()
        if any(is_simple_copy_mapper(match.group("args")) for match in RESPONSE_MAPPER.finditer(text)):
            errors.append(
                f"{path.relative_to(ROOT)}: simple Result.toResponse() mapper found; "
                "return 1:1 Result directly or keep a real response mapper only when API shape differs"
            )
    return errors, warnings


def audit_controller_response_types() -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    for root in (ROOT / "modules" / "admin-api", ROOT / "modules" / "shop-api"):
        for path in root.glob("src/main/kotlin/**/*.kt"):
            if not path.exists():
                continue
            text = path.read_text()
            if UNTYPED_API_RESPONSE.search(text):
                errors.append(f"{path.relative_to(ROOT)}: use a concrete ApiResponse<T> type instead of ApiResponse<Any>")
            if TOP_LEVEL_LIST_API_RESPONSE.search(text):
                errors.append(f"{path.relative_to(ROOT)}: wrap top-level lists in a response object instead of ApiResponse<List<T>>")
    return errors, warnings


def is_simple_copy_mapper(args: str) -> bool:
    parts = [part.strip().rstrip(",") for part in args.replace("\n", " ").split(",")]
    parts = [part for part in parts if part]
    return bool(parts) and all(PLAIN_ARGUMENT.match(part) for part in parts)


def main() -> int:
    parser = argparse.ArgumentParser(description="Broad harness audit for workflow and backend controller rules.")
    parser.add_argument("--lane", choices=sorted(LANES), required=True)
    parser.add_argument("--strict-state", action="store_true", help="fail when archived phases and state are not fully aligned")
    parser.add_argument("--changed-only", action="store_true", help="deprecated; source rule patterns are checked in changed files by default")
    parser.add_argument("--all-response-mappers", action="store_true", help="scan all Kotlin response mapper files instead of changed files only")
    args = parser.parse_args()

    errors = []
    warnings = []

    lane_errors, lane_warnings = audit_lane_state(args.lane, args.strict_state)
    errors.extend(lane_errors)
    warnings.extend(lane_warnings)

    mapper_errors, mapper_warnings = audit_response_mappers(args.all_response_mappers)
    errors.extend(mapper_errors)
    warnings.extend(mapper_warnings)

    response_type_errors, response_type_warnings = audit_controller_response_types()
    errors.extend(response_type_errors)
    warnings.extend(response_type_warnings)

    for warning in warnings:
        print(f"WARN: {warning}")
    for error in errors:
        print(f"ERROR: {error}", file=sys.stderr)

    if errors:
        return 1
    print("Harness audit passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
