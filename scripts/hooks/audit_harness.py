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
    r"fun\s+\w*Response\.toApiResponse\(\)\s*:\s*\w*ApiResponse\s*=\s*\w*ApiResponse\((?P<args>.*?)\)",
    re.MULTILINE | re.DOTALL,
)
PLAIN_ARGUMENT = re.compile(r"^(?:\w+\s*=\s*)?(?:this\.)?\w+$")
UNTYPED_API_RESPONSE = re.compile(r"\b(?:ResponseEntity<\s*)?ApiResponse<\s*Any\s*>")
TOP_LEVEL_LIST_API_RESPONSE = re.compile(r"\b(?:ResponseEntity<\s*)?ApiResponse<\s*List\s*<")
READ_ONLY_TRANSACTION = re.compile(r"@Transactional\s*\(\s*readOnly\s*=\s*true\s*\)")
WRITE_TRANSACTION = re.compile(r"@Transactional(?!\s*\(\s*readOnly\s*=\s*true\s*\))")
SAVE_CALL = re.compile(r"\.\s*save(?:All)?\s*\(")
QUERYDSL_PATH_BUILDER = re.compile(r"\bPathBuilder\s*<|\bPathBuilder\s*\(")
QUERYDSL_OPAQUE_PREDICATE_PARAMETER = re.compile(
    r"fun\s+\w+\s*\([^)]*\b(?:where|condition|predicate)\s*:\s*(?:com\.querydsl\.core\.types\.)?Predicate",
    re.MULTILINE | re.DOTALL,
)
QUERYDSL_PROJECTION_HELPER = re.compile(r"fun\s+\w*Row\s*\([^)]*\)\s*:\s*Q\w+Row", re.MULTILINE)
QUERYDSL_PROJECTION_IN_ADAPTER = re.compile(r"@QueryProjection\s+constructor")
FEATURE_PAGE_QUERY_CLASS = re.compile(r"data\s+class\s+\w+PageQuery\s*\(")
TOP_LEVEL_INTERFACE = re.compile(r"^interface\s+\w+", re.MULTILINE)


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
                f"{path.relative_to(ROOT)}: simple Response.toApiResponse() mapper found; "
                "return 1:1 Response directly or keep a real response mapper only when API shape differs"
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


def audit_transaction_cqrs_boundary() -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    for path in (ROOT / "modules" / "application" / "src" / "main" / "kotlin").glob("**/*.kt"):
        if not path.exists():
            continue
        if not (path.name.endswith("Service.kt") or path.name.endswith("Facade.kt")):
            continue
        text = path.read_text()
        has_read_only = bool(READ_ONLY_TRANSACTION.search(text))
        has_write_transaction = bool(WRITE_TRANSACTION.search(text))
        has_save = bool(SAVE_CALL.search(text))
        if has_read_only and (has_write_transaction or has_save):
            errors.append(
                f"{path.relative_to(ROOT)}: split CQRS transaction boundaries; "
                "do not mix readOnly query flow with write/save flow in one service/facade"
            )
        if has_read_only and "Query" not in path.stem:
            errors.append(f"{path.relative_to(ROOT)}: readOnly service/facade must be named QueryService or QueryFacade")
    return errors, warnings


def audit_domain_model_files() -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    domain_root = ROOT / "modules" / "domain" / "src" / "main" / "kotlin"
    for suffix in ("*Models.kt", "*Contracts.kt"):
        for path in domain_root.glob(f"**/{suffix}"):
            errors.append(
                f"{path.relative_to(ROOT)}: split bundled domain files into entity-group files under domain/<entity-group>"
            )
    return errors, warnings


def audit_pagination_models() -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    app_root = ROOT / "modules" / "application" / "src" / "main" / "kotlin"
    for path in app_root.glob("**/*.kt"):
        if not path.exists():
            continue
        text = path.read_text()
        if FEATURE_PAGE_QUERY_CLASS.search(text):
            errors.append(
                f"{path.relative_to(ROOT)}: use common Pagination for page/size/sort and pass target ids separately"
            )
    return errors, warnings


def audit_provided_port_files() -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    app_root = ROOT / "modules" / "application" / "src" / "main" / "kotlin"
    for path in app_root.glob("**/provided/*.kt"):
        if not path.exists():
            continue
        text = path.read_text()
        interfaces = TOP_LEVEL_INTERFACE.findall(text)
        if len(interfaces) > 1:
            errors.append(
                f"{path.relative_to(ROOT)}: keep one provided port/repository interface per file"
            )
        if path.name.endswith("Repositories.kt"):
            errors.append(
                f"{path.relative_to(ROOT)}: split bundled repository files by aggregate or port responsibility"
            )
    return errors, warnings


def audit_querydsl_adapters() -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    infra_root = ROOT / "modules" / "infra" / "src" / "main" / "kotlin"
    for path in infra_root.glob("**/QueryDsl*QueryAdapter.kt"):
        if not path.exists():
            continue
        text = path.read_text()
        if QUERYDSL_PATH_BUILDER.search(text):
            errors.append(f"{path.relative_to(ROOT)}: use generated Q types instead of QueryDSL PathBuilder")
        if QUERYDSL_OPAQUE_PREDICATE_PARAMETER.search(text):
            errors.append(
                f"{path.relative_to(ROOT)}: avoid opaque QueryDSL Predicate parameters; "
                "write explicit query methods for each business condition"
            )
        if QUERYDSL_PROJECTION_HELPER.search(text):
            errors.append(
                f"{path.relative_to(ROOT)}: do not hide QueryDSL select fields behind projection helper methods"
            )
        if QUERYDSL_PROJECTION_IN_ADAPTER.search(text):
            errors.append(
                f"{path.relative_to(ROOT)}: move @QueryProjection row DTOs to a separate projection file"
            )
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

    tx_errors, tx_warnings = audit_transaction_cqrs_boundary()
    errors.extend(tx_errors)
    warnings.extend(tx_warnings)

    domain_model_errors, domain_model_warnings = audit_domain_model_files()
    errors.extend(domain_model_errors)
    warnings.extend(domain_model_warnings)

    pagination_errors, pagination_warnings = audit_pagination_models()
    errors.extend(pagination_errors)
    warnings.extend(pagination_warnings)

    provided_errors, provided_warnings = audit_provided_port_files()
    errors.extend(provided_errors)
    warnings.extend(provided_warnings)

    querydsl_errors, querydsl_warnings = audit_querydsl_adapters()
    errors.extend(querydsl_errors)
    warnings.extend(querydsl_warnings)

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
