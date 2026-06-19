#!/usr/bin/env python3
import argparse
import json
import re
import shlex
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from zoneinfo import ZoneInfo


ROOT = Path(__file__).resolve().parents[1]
SCRIPT_DIR = ROOT / "scripts"
HOOK_DIR = SCRIPT_DIR / "hooks"
COMMAND_GUARD = HOOK_DIR / "guard_command.py"
CIRCUIT_BREAKER = HOOK_DIR / "circuit_breaker.py"
PROJECT_NAME = "card-service"
OBSIDIAN_ROOT = Path("/Users/bigs/Documents/Obsidian Vault/02. Area/03. Idea Lab")
OBSIDIAN_BUILD_DIR = OBSIDIAN_ROOT / "07.Build Logs" / PROJECT_NAME
OBSIDIAN_DAYS_DIR = OBSIDIAN_BUILD_DIR / "days"
OBSIDIAN_INDEX_FILE = OBSIDIAN_BUILD_DIR / "작업기록.md"
OBSIDIAN_ACTIVE_FILE = OBSIDIAN_ROOT / "09.Context Handoffs" / "01.Active Work" / PROJECT_NAME / "현재작업.md"
LOCAL_HANDOFF_FILE = ROOT / ".codex" / "context" / "active-handoff.md"
SEOUL = ZoneInfo("Asia/Seoul")
LANE = "backend"
PHASE_DIR = ROOT / "workflow" / LANE / "phases"
ARCHIVE_DIR = ROOT / "workflow" / LANE / "archive"
STATE_FILE = ROOT / "workflow" / LANE / "state" / "execute-state.json"
RUN_STATE_FILE = ROOT / "workflow" / LANE / "state" / "run-state.md"
VALIDATE_HOOK = HOOK_DIR / f"validate_{LANE}.sh"
LANE_VALIDATE_HOOKS = {
    "backend": HOOK_DIR / "validate_backend.sh",
    "frontend": HOOK_DIR / "validate_frontend.sh",
}
REQUIRED_PHASE_SECTIONS = [
    "Goal",
    "Docs Read",
    "Scope",
    "Out Of Scope",
    "Files To Touch",
    "Implementation Steps",
    "Done Criteria",
    "Validation",
    "Review Focus",
]
SOURCE_PREFIXES = ("modules/", "src/", "app/", "frontend/")
SOURCE_EXTENSIONS = (".kt", ".java", ".ts", ".tsx", ".js", ".jsx")
TEST_MARKERS = ("/src/test/", "/src/integrationTest/", "__tests__", ".spec.", ".test.")
ALWAYS_ALLOWED_PREFIXES = ("docs/", "workflow/", ".codex/", "rules/", "scripts/hooks/", "scripts/execute.py", "README.md", "AGENT.md")
AUTO_COMMIT_ON_COMPLETE = True


def configure_lane(lane: str) -> None:
    global LANE, PHASE_DIR, ARCHIVE_DIR, STATE_FILE, RUN_STATE_FILE, VALIDATE_HOOK
    if lane not in LANE_VALIDATE_HOOKS:
        raise ValueError(f"Unknown lane: {lane}")
    LANE = lane
    PHASE_DIR = ROOT / "workflow" / lane / "phases"
    ARCHIVE_DIR = ROOT / "workflow" / lane / "archive"
    STATE_FILE = ROOT / "workflow" / lane / "state" / "execute-state.json"
    RUN_STATE_FILE = ROOT / "workflow" / lane / "state" / "run-state.md"
    VALIDATE_HOOK = LANE_VALIDATE_HOOKS[lane]


def load_state() -> dict:
    if not STATE_FILE.exists():
        return {"current_phase": None, "phases": {}}
    return json.loads(STATE_FILE.read_text())


def save_state(state: dict) -> None:
    STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
    STATE_FILE.write_text(json.dumps(state, indent=2, ensure_ascii=False) + "\n")


def now_utc() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def now_local() -> datetime:
    return datetime.now(SEOUL)


def today_text() -> str:
    return now_local().date().isoformat()


def phase_files() -> list[Path]:
    return sorted(PHASE_DIR.glob("*.md"))


def find_phase_path(phase_name: str) -> Path | None:
    active_path = PHASE_DIR / phase_name
    if active_path.exists():
        return active_path
    matches = sorted(ARCHIVE_DIR.glob(f"**/{phase_name}")) if ARCHIVE_DIR.exists() else []
    return matches[0] if matches else None


def ensure_known_phases(state: dict) -> dict:
    phases = state.setdefault("phases", {})
    for phase in phase_files():
        defaults = {
            "status": "pending",
            "started_at": None,
            "completed_at": None,
            "last_validation": None,
            "last_checkpoint": None,
        }
        item = phases.setdefault(phase.name, defaults.copy())
        for key, value in defaults.items():
            item.setdefault(key, value)
    known = {phase.name for phase in phase_files()}
    for name in list(phases):
        item = phases[name]
        if name not in known and item.get("status") != "completed" and not item.get("archive_path"):
            phases[name]["status"] = "missing"
    return state


def next_pending(state: dict) -> str | None:
    for phase in phase_files():
        item = state["phases"].get(phase.name, {})
        if item.get("status") in {"pending", "failed"}:
            return phase.name
    return None


def current_or_next_phase(state: dict) -> str | None:
    return state.get("current_phase") or next_pending(state)


def append_run_state(message: str) -> None:
    timestamp = now_utc()
    RUN_STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
    with RUN_STATE_FILE.open("a") as file:
        file.write(f"\n## scripts/execute.py {timestamp}\n\n{message}\n")


def read_optional_text(path: Path, limit: int | None = None) -> str:
    if not path.exists():
        return ""
    text = path.read_text()
    return text if limit is None else text[:limit]


def section_text(text: str, heading: str) -> str:
    pattern = re.compile(rf"^## {re.escape(heading)}\s*$", re.MULTILINE)
    match = pattern.search(text)
    if not match:
        return ""
    start = match.end()
    next_match = re.search(r"^##\s+", text[start:], re.MULTILINE)
    end = start + next_match.start() if next_match else len(text)
    return text[start:end].strip()


def run_text(command: list[str]) -> str:
    result = subprocess.run(command, cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
    return result.stdout.strip() if result.returncode == 0 else "-"


def command_key(command: list[str]) -> str:
    return " ".join(shlex.quote(part) for part in command)


def guard_command(command: list[str]) -> int:
    result = subprocess.run(["python3", str(COMMAND_GUARD), "--", *command], cwd=ROOT)
    return result.returncode


def circuit_check(key: str) -> int:
    result = subprocess.run(["python3", str(CIRCUIT_BREAKER), "--lane", LANE, "check", "--key", key], cwd=ROOT)
    return result.returncode


def circuit_record(key: str, status: str, summary: str = "-") -> int:
    result = subprocess.run(
        ["python3", str(CIRCUIT_BREAKER), "--lane", LANE, "record", "--key", key, "--status", status, "--summary", summary],
        cwd=ROOT,
    )
    return result.returncode


def run_guarded_command(command: list[str]) -> int:
    key = command_key(command)
    guard_result = guard_command(command)
    if guard_result != 0:
        circuit_record(key, "failed", f"command guard blocked: {guard_result}")
        return guard_result

    circuit_result = circuit_check(key)
    if circuit_result != 0:
        return circuit_result

    result = subprocess.run(command, cwd=ROOT)
    if result.returncode == 0:
        circuit_record(key, "passed")
        return 0

    record_result = circuit_record(key, "failed", f"exit code {result.returncode}")
    return record_result if record_result != 0 else result.returncode


def git_summary() -> dict:
    return {
        "branch": run_text(["git", "branch", "--show-current"]),
        "latest_commit": run_text(["git", "log", "-1", "--pretty=%h %s"]),
        "status": run_text(["git", "status", "--short"]) or "clean",
    }


def git_has_changes() -> bool:
    return bool(changed_files())


def git_commit_message(phase_name: str) -> str:
    title = phase_title(phase_name)
    clean_title = re.sub(r"^Phase\s+\d+:\s*", "", title).strip()
    lowered = clean_title.lower()
    if "test" in lowered or "테스트" in clean_title:
        prefix = "test"
    elif "docs" in lowered or "문서" in clean_title or "workflow" in lowered:
        prefix = "docs"
    elif "fix" in lowered or "bug" in lowered or "수정" in clean_title:
        prefix = "fix"
    elif "refactor" in lowered or "리팩터" in clean_title:
        prefix = "refactor"
    else:
        prefix = "feat"
    return f"{prefix}: {clean_title}"


def git_auto_commit(phase_name: str) -> tuple[bool, str]:
    if not git_has_changes():
        return False, "no changes to commit"

    message = git_commit_message(phase_name)
    subprocess.run(["git", "add", "-A"], cwd=ROOT, check=True)
    result = subprocess.run(["git", "commit", "-m", message], cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if result.returncode != 0:
        return False, result.stdout.strip()
    short_hash = run_text(["git", "rev-parse", "--short", "HEAD"])
    return True, f"{short_hash} {message}"


def phase_title(phase_name: str | None) -> str:
    if not phase_name:
        return "-"
    phase_path = find_phase_path(phase_name)
    if not phase_path or not phase_path.exists():
        return phase_name
    for line in phase_path.read_text().splitlines():
        if line.startswith("# "):
            return line.removeprefix("# ").strip()
    return phase_name


def phase_text(phase_name: str) -> str:
    phase_path = find_phase_path(phase_name)
    return phase_path.read_text() if phase_path and phase_path.exists() else ""


def phase_section(phase_name: str, heading: str) -> str:
    return section_text(phase_text(phase_name), heading)


def phase_requires_review(phase_name: str) -> bool:
    review_focus = phase_section(phase_name, "Review Focus")
    return bool(review_focus and review_focus.strip() not in {"-", "None", "N/A"})


def phase_validation_commands(phase_name: str) -> list[str]:
    section = phase_section(phase_name, "Validation")
    commands = re.findall(r"`([^`]+)`", section)
    return [command for command in commands if not command.startswith("python3 scripts/execute.py")]


def phase_test_plan(phase_name: str) -> str:
    return phase_section(phase_name, "Test First")


def phase_has_test_plan(phase_name: str) -> bool:
    plan = phase_test_plan(phase_name).strip()
    return bool(plan and plan not in {"-", "None", "N/A"})


def phase_tdd_enforcement_needed(phase_name: str) -> bool:
    files = phase_section(phase_name, "Files To Touch")
    return "modules/" in files or "src/" in files or "frontend/" in files


def phase_has_tdd_exception(phase_name: str) -> bool:
    exception = phase_section(phase_name, "TDD Exception").strip()
    return bool(exception and exception not in {"-", "None", "N/A"})


def done_criteria_complete(phase_name: str) -> bool:
    section = phase_section(phase_name, "Done Criteria")
    checkboxes = re.findall(r"^\s*-\s+\[([ xX])\]", section, re.MULTILINE)
    return bool(checkboxes) and all(value.lower() == "x" for value in checkboxes)


def changed_files() -> list[str]:
    result = subprocess.run(
        ["git", "status", "--short"],
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.DEVNULL,
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
    return path.startswith(SOURCE_PREFIXES) and path.endswith(SOURCE_EXTENSIONS) and not is_test_file(path)


def phase_allowed_paths(phase_name: str) -> list[str]:
    section = phase_section(phase_name, "Files To Touch")
    paths = re.findall(r"`([^`]+)`", section)
    return [path.strip().rstrip("/") for path in paths if path.strip()]


def path_allowed_by_phase(path: str, allowed_paths: list[str]) -> bool:
    if path.startswith(ALWAYS_ALLOWED_PREFIXES):
        return True
    for allowed in allowed_paths:
        if path == allowed or path.startswith(f"{allowed}/"):
            return True
    return False


def out_of_scope_source_changes(phase_name: str) -> list[str]:
    allowed = phase_allowed_paths(phase_name)
    return [
        path
        for path in changed_files()
        if is_source_file(path) and not path_allowed_by_phase(path, allowed)
    ]


def lint_phase(phase_path: Path) -> tuple[list[str], list[str]]:
    errors = []
    warnings = []
    phase_name = phase_path.name
    text = phase_path.read_text()

    for section in REQUIRED_PHASE_SECTIONS:
        if not phase_section(phase_name, section):
            errors.append(f"{phase_name}: missing required section `{section}`")

    if phase_tdd_enforcement_needed(phase_name) and not phase_has_test_plan(phase_name) and not phase_has_tdd_exception(phase_name):
        errors.append(f"{phase_name}: production phase requires `Test First` or `TDD Exception`")

    if not phase_validation_commands(phase_name):
        warnings.append(f"{phase_name}: no executable validation command; fallback hook will run")

    step_count = len(re.findall(r"^\s*-\s+\[[ xX]\]", phase_section(phase_name, "Implementation Steps"), re.MULTILINE))
    if step_count > 5:
        warnings.append(f"{phase_name}: has {step_count} implementation steps; consider splitting")

    touch_lines = [line for line in phase_section(phase_name, "Files To Touch").splitlines() if line.strip().startswith("-")]
    if len(touch_lines) > 5:
        warnings.append(f"{phase_name}: touches {len(touch_lines)} areas; consider narrowing scope")

    if "frontend/" in text and "modules/" in text:
        warnings.append(f"{phase_name}: mentions backend and frontend paths together; verify one done criterion covers both")

    return errors, warnings


def enforce_tdd_guard() -> int:
    return run_guarded_command(["python3", "scripts/hooks/enforce_tdd.py", "--lane", LANE])


def scope_check(phase_name: str) -> list[str]:
    return out_of_scope_source_changes(phase_name)


def format_phase_summary(phase_name: str) -> str:
    sections = [
        "Goal",
        "Docs Read",
        "Scope",
        "Out Of Scope",
        "Files To Touch",
        "Test First",
        "Implementation Steps",
        "Done Criteria",
        "Validation",
        "Review Focus",
    ]
    phase_path = find_phase_path(phase_name)
    path_text = str(phase_path.relative_to(ROOT)) if phase_path else f"workflow/phases/{phase_name}"
    lines = [f"# {phase_title(phase_name)}", "", f"Path: {path_text}"]
    for section in sections:
        content = phase_section(phase_name, section)
        if content:
            lines.extend(["", f"## {section}", "", content])
    return "\n".join(lines)


def phase_path_text(phase_name: str | None) -> str:
    if not phase_name:
        return "-"
    path = find_phase_path(phase_name)
    return f"`{path.relative_to(ROOT)}`" if path else f"`workflow/phases/{phase_name}`"


def validation_text(phase: dict | None) -> str:
    if not phase:
        return "-"
    validation = phase.get("last_validation")
    if not validation:
        return "not run"
    return f"{validation.get('status', '-')} at {validation.get('at', '-')}"


def ensure_obsidian_dirs() -> None:
    OBSIDIAN_DAYS_DIR.mkdir(parents=True, exist_ok=True)
    OBSIDIAN_ACTIVE_FILE.parent.mkdir(parents=True, exist_ok=True)
    LOCAL_HANDOFF_FILE.parent.mkdir(parents=True, exist_ok=True)


def day_number_for_today() -> int:
    ensure_obsidian_dirs()
    today = today_text()
    existing = sorted(OBSIDIAN_DAYS_DIR.glob(f"{today}-*일차.md"))
    if existing:
        name = existing[0].stem
        suffix = name.removeprefix(f"{today}-").removesuffix("일차")
        if suffix.isdigit():
            return int(suffix)

    max_day = 0
    for path in OBSIDIAN_DAYS_DIR.glob("*일차.md"):
        suffix = path.stem.split("-")[-1].removesuffix("일차")
        if suffix.isdigit():
            max_day = max(max_day, int(suffix))
    return max_day + 1


def day_file() -> Path:
    number = day_number_for_today()
    return OBSIDIAN_DAYS_DIR / f"{today_text()}-{number}일차.md"


def ensure_day_file() -> Path:
    path = day_file()
    if not path.exists():
        path.write_text(
            f"# {day_number_for_today()}일차 작업 정리\n\n"
            "## 완료한 작업\n\n"
            "## 오늘 만든 최종 결과\n\n"
            "## 주요 결정과 이유\n\n"
            "## 자동 실행 기록\n\n"
            "## 다음에 이어볼 지점\n\n"
            "## 확인 필요\n",
        )
    return path


def ensure_index_day_row() -> None:
    ensure_obsidian_dirs()
    day = day_file()
    date = today_text()
    day_number = day_number_for_today()
    row = f"| {day_number}일차 | {date} | [[days/{day.stem}|{day.stem}]] | 자동 phase 실행 기록 |"
    if not OBSIDIAN_INDEX_FILE.exists():
        OBSIDIAN_INDEX_FILE.write_text(
            "---\n"
            "type: build-log-index\n"
            "status: active\n"
            f"created: {date}\n"
            f"updated: {date}\n"
            "---\n\n"
            "# 카드 서비스 작업기록\n\n"
            "## 일차별 기록\n\n"
            "| 일차 | 날짜 | 상세 기록 | 핵심 결과 |\n"
            "|---|---|---|---|\n"
            f"{row}\n",
        )
        return

    text = OBSIDIAN_INDEX_FILE.read_text()
    if f"[[days/{day.stem}|{day.stem}]]" in text:
        return
    if "| 일차 | 날짜 | 상세 기록 | 핵심 결과 |" in text:
        OBSIDIAN_INDEX_FILE.write_text(text.rstrip() + f"\n{row}\n")
    else:
        OBSIDIAN_INDEX_FILE.write_text(
            text.rstrip()
            + "\n\n## 일차별 기록\n\n"
            "| 일차 | 날짜 | 상세 기록 | 핵심 결과 |\n"
            "|---|---|---|---|\n"
            f"{row}\n",
        )


def append_day_event(event: str, phase_name: str | None, details: str = "") -> None:
    ensure_index_day_row()
    path = ensure_day_file()
    timestamp = now_local().isoformat(timespec="seconds")
    message = (
        f"\n### {timestamp} - {event}\n\n"
        f"- Phase: {phase_path_text(phase_name)}\n"
        f"- Title: {phase_title(phase_name)}\n"
    )
    if details:
        message += f"- Details: {details}\n"
    with path.open("a") as file:
        file.write(message)


def active_work_content(state: dict, event: str = "sync") -> str:
    current = state.get("current_phase")
    phases = state.get("phases", {})
    current_item = phases.get(current) if current else None
    next_phase = current or next_pending(state)
    git = git_summary()
    phase_lines = "\n".join(
        f"- `{name}`: {item.get('status', '-')}, validation: {validation_text(item)}, checkpoint: {item.get('last_checkpoint') or '-'}"
        for name, item in sorted(phases.items())
    ) or "- No phases registered."

    return f"""---
type: active-work
status: active
updated: {today_text()}
tags:
  - idea-lab
  - card-service
  - payment
---

# card-service 현재작업

## Latest Event

- Event: {event}
- Updated: {now_local().isoformat(timespec="seconds")}

## Repo

`{ROOT}`

## Current Phase

- Current phase: {phase_path_text(current)}
- Current title: {phase_title(current)}
- Current status: {current_item.get("status", "-") if current_item else "-"}
- Last validation: {validation_text(current_item)}

## Next Phase

- Next phase: {phase_path_text(next_phase)}
- Next title: {phase_title(next_phase)}

## Phase Status

{phase_lines}

## Git

- Branch: `{git["branch"]}`
- Latest commit: `{git["latest_commit"]}`
- Dirty files:

```text
{git["status"]}
```

## Local Source Of Truth

- Constitution: `{ROOT / "AGENT.md"}`
- Workflow skill: `{ROOT / ".codex/skills/workflow.md"}`
- Review skill: `{ROOT / ".codex/skills/review.md"}`
- Project brain: `{ROOT / "docs"}`
- Active phase files: `{ROOT / "workflow/phases"}`
- Archived phase files: `{ROOT / "workflow/archive"}`
- Execute state: `{STATE_FILE}`
- Run state: `{RUN_STATE_FILE}`
- Local handoff: `{LOCAL_HANDOFF_FILE}`

## Obsidian Records

- Build log index: `{OBSIDIAN_INDEX_FILE}`
- Today detail: `{day_file()}`
- Active handoff: `{OBSIDIAN_ACTIVE_FILE}`

## Resume Instruction

1. Run `python3 scripts/execute.py resume` first after context compression, thread resume, or handoff.
2. Read `AGENT.md` and `.codex/skills/workflow.md` only if the resume output is not enough.
3. If a phase is in progress, continue that phase.
4. If no phase is in progress, inspect the next pending phase with `python3 scripts/execute.py show`.
5. Do not edit production code before following the phase `Test First` section.
"""


def sync_handoff(state: dict, event: str) -> None:
    ensure_obsidian_dirs()
    ensure_index_day_row()
    content = active_work_content(state, event=event)
    OBSIDIAN_ACTIVE_FILE.write_text(content)
    LOCAL_HANDOFF_FILE.write_text(content)


def cmd_status(_: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    save_state(state)
    print(f"Current phase: {state.get('current_phase') or '-'}")
    print("Active phases:")
    for phase in phase_files():
        item = state["phases"][phase.name]
        print(f"{item['status']:>10}  {phase.name}")
    archived = [
        (name, item)
        for name, item in sorted(state["phases"].items())
        if item.get("status") == "completed" and item.get("archive_path")
    ]
    if archived:
        print("Archived phases:")
        for name, item in archived:
            print(f"{item['status']:>10}  {name} -> {item['archive_path']}")
    return 0


def cmd_show(args: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    save_state(state)
    phase = args.phase or current_or_next_phase(state)
    if not phase:
        print("No phase to show.")
        return 1
    print(format_phase_summary(phase))
    return 0


def cmd_sync(_: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    save_state(state)
    sync_handoff(state, event="manual sync")
    print("Synced Obsidian active work and local handoff.")
    return 0


def cmd_run(args: argparse.Namespace) -> int:
    command = args.command
    if command and command[0] == "--":
        command = command[1:]
    if not command:
        print("No command provided. Use `python3 scripts/execute.py run -- <command>`.")
        return 1
    return run_guarded_command(command)


def cmd_resume(_: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    save_state(state)
    sync_handoff(state, event="resume")

    current = state.get("current_phase")
    next_phase = current or next_pending(state)
    current_item = state["phases"].get(current) if current else None
    next_item = state["phases"].get(next_phase) if next_phase else None
    local_handoff = read_optional_text(LOCAL_HANDOFF_FILE)
    obsidian_handoff = read_optional_text(OBSIDIAN_ACTIVE_FILE)
    git = git_summary()

    append_run_state(
        f"- Resume context loaded\n"
        f"- Current phase: {phase_path_text(current)}\n"
        f"- Next phase: {phase_path_text(next_phase)}\n"
        f"- Git: {git['latest_commit']}; {git['status']}"
    )
    append_day_event("resume context loaded", next_phase, details=f"git: {git['latest_commit']}; status: {git['status']}")

    print("# Resume Context")
    print()
    print(f"Repo: {ROOT}")
    print(f"Branch: {git['branch']}")
    print(f"Latest commit: {git['latest_commit']}")
    print(f"Dirty files: {git['status']}")
    print()
    print("## Handoff Sources")
    print(f"- Local handoff: {LOCAL_HANDOFF_FILE} ({'found' if local_handoff else 'missing'})")
    print(f"- Obsidian active handoff: {OBSIDIAN_ACTIVE_FILE} ({'found' if obsidian_handoff else 'missing'})")
    print(f"- Run state: {RUN_STATE_FILE} ({'found' if RUN_STATE_FILE.exists() else 'missing'})")
    print()
    print("## Current")
    print(f"- Phase: {phase_path_text(current)}")
    print(f"- Title: {phase_title(current)}")
    print(f"- Status: {current_item.get('status', '-') if current_item else '-'}")
    print(f"- Last validation: {validation_text(current_item)}")
    print()
    print("## Next")
    print(f"- Phase: {phase_path_text(next_phase)}")
    print(f"- Title: {phase_title(next_phase)}")
    print(f"- Status: {next_item.get('status', '-') if next_item else '-'}")
    if next_phase:
        print()
        print("## Next Phase Goal")
        print(section_text(format_phase_summary(next_phase), "Goal") or "-")
        print()
        print("## Required Docs")
        print(section_text(format_phase_summary(next_phase), "Docs Read") or "-")
    print()
    print("## Resume Instruction")
    if current:
        print("1. Continue the current phase.")
        print("2. Run `python3 scripts/execute.py show` before editing if context is unclear.")
        print("3. Use `python3 scripts/execute.py checkpoint \"message\"` before a risky or long edit.")
    elif next_phase:
        print("1. Run `python3 scripts/execute.py show` to inspect the next phase.")
        print("2. If the phase is still correctly scoped, run `python3 scripts/execute.py start`.")
        print("3. Follow the `Test First` section before production edits.")
    else:
        print("1. No pending phase. Update `docs/` and create new phase files before implementation.")
    return 0


def cmd_lint_phases(_: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    save_state(state)
    errors = []
    warnings = []
    seen_numbers = {}

    for phase in phase_files():
        match = re.match(r"phase-(\d+)-", phase.name)
        if not match:
            errors.append(f"{phase.name}: filename must start with `phase-NNN-`")
        else:
            number = match.group(1)
            if number in seen_numbers:
                errors.append(f"{phase.name}: duplicate phase number with {seen_numbers[number]}")
            seen_numbers[number] = phase.name

        phase_errors, phase_warnings = lint_phase(phase)
        errors.extend(phase_errors)
        warnings.extend(phase_warnings)

    for warning in warnings:
        print(f"WARN: {warning}")
    for error in errors:
        print(f"ERROR: {error}")

    if errors:
        return 1
    print("Phase lint passed.")
    return 0


def cmd_start(args: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    current = state.get("current_phase")
    if current and state["phases"].get(current, {}).get("status") in {"in_progress", "review_required"}:
        print(f"Already in progress: {current}")
        return 1

    phase = next_pending(state)
    if not phase:
        print("No pending phase.")
        return 0

    dirty_files = changed_files()
    if dirty_files and not args.allow_dirty:
        print("Worktree has existing changes. Commit, stash, or run with `--allow-dirty` before starting a phase.")
        for path in dirty_files:
            print(f"- {path}")
        return 1

    phase_errors, phase_warnings = lint_phase(PHASE_DIR / phase)
    for warning in phase_warnings:
        print(f"WARN: {warning}")
    if phase_errors:
        print("Phase lint failed:")
        for error in phase_errors:
            print(f"- {error}")
        return 1

    if phase_tdd_enforcement_needed(phase) and not phase_has_test_plan(phase) and not phase_has_tdd_exception(phase):
        print(f"TDD test plan is missing in {phase}.")
        print("Add a `## Test First` section before starting implementation.")
        return 1

    state["current_phase"] = phase
    state["phases"][phase]["status"] = "in_progress"
    state["phases"][phase]["started_at"] = now_utc()
    state["phases"][phase]["baseline_dirty_files"] = dirty_files
    save_state(state)
    append_run_state(f"- Started phase: `{phase}`")
    append_day_event("phase started", phase)
    sync_handoff(state, event="phase started")
    print(f"Started: {phase}")
    return 0


def cmd_validate(_: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    current = state.get("current_phase")
    if not current:
        print("No current phase. Run `python3 scripts/execute.py start` first.")
        return 1

    tdd_result = enforce_tdd_guard()
    if tdd_result != 0:
        state["phases"][current]["status"] = "failed"
        save_state(state)
        append_run_state(f"- TDD guard failed for `{current}`")
        append_day_event("phase TDD guard failed", current)
        sync_handoff(state, event="TDD guard failed")
        return tdd_result

    out_of_scope = scope_check(current)
    if out_of_scope:
        state["phases"][current]["status"] = "failed"
        save_state(state)
        details = "out-of-scope source changes: " + ", ".join(out_of_scope)
        append_run_state(f"- Scope check failed for `{current}`\n- {details}")
        append_day_event("phase scope check failed", current, details=details)
        sync_handoff(state, event="scope check failed")
        print("Scope check failed: source changes outside `Files To Touch`.")
        for path in out_of_scope:
            print(f"- {path}")
        return 1

    commands = phase_validation_commands(current)
    if commands:
        returncode = 0
        executed = []
        for command in commands:
            executed.append(command)
            returncode = run_guarded_command(shlex.split(command))
            if returncode != 0:
                break
    else:
        executed = [str(VALIDATE_HOOK)]
        returncode = run_guarded_command([str(VALIDATE_HOOK)])
    status = "passed" if returncode == 0 else "failed"
    state["phases"][current]["last_validation"] = {
        "status": status,
        "at": now_utc(),
        "commands": executed,
    }
    if returncode != 0:
        state["phases"][current]["status"] = "failed"
    save_state(state)
    append_run_state(f"- Validation for `{current}`: {status}\n- Commands: {', '.join(executed)}")
    append_day_event("phase validation", current, details=f"{status}; commands: {', '.join(executed)}")
    sync_handoff(state, event=f"validation {status}")
    return returncode


def cmd_checkpoint(args: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    current = state.get("current_phase")
    if not current:
        print("No current phase. Run `python3 scripts/execute.py start` first.")
        return 1

    message = " ".join(args.message).strip()
    if not message:
        print("Checkpoint message is required.")
        return 1

    state["phases"][current]["last_checkpoint"] = f"{now_utc()} {message}"
    save_state(state)
    append_run_state(f"- Checkpoint for `{current}`: {message}")
    append_day_event("phase checkpoint", current, details=message)
    sync_handoff(state, event="checkpoint")
    print(f"Checkpoint saved: {message}")
    return 0


def cmd_review(args: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    current = state.get("current_phase")
    if not current:
        print("No current phase.")
        return 1
    item = state["phases"][current]
    if item.get("status") != "review_required":
        print(f"Current phase is not review_required: {item.get('status')}")
        return 1

    note = " ".join(args.note).strip() or "review accepted"
    item["status"] = "validated"
    item["reviewed_at"] = now_utc()
    item["review_note"] = note
    save_state(state)
    append_run_state(f"- Review accepted for `{current}`: {note}")
    append_day_event("phase review accepted", current, details=note)
    sync_handoff(state, event="review accepted")
    print(f"Review accepted: {current}")
    return 0


def archive_phase_file(phase_name: str) -> str | None:
    source = PHASE_DIR / phase_name
    if not source.exists():
        archived = find_phase_path(phase_name)
        return str(archived.relative_to(ROOT)) if archived else None

    target_dir = ARCHIVE_DIR / today_text()
    target_dir.mkdir(parents=True, exist_ok=True)
    target = target_dir / phase_name
    if target.exists():
        suffix = now_local().strftime("%H%M%S")
        target = target_dir / f"{source.stem}-{suffix}{source.suffix}"
    source.rename(target)
    return str(target.relative_to(ROOT))


def cmd_complete(_: argparse.Namespace) -> int:
    state = ensure_known_phases(load_state())
    current = state.get("current_phase")
    if not current:
        print("No current phase.")
        return 1

    validation = state["phases"][current].get("last_validation")
    if not validation or validation.get("status") != "passed":
        print("Current phase has no passing validation. Run `python3 scripts/execute.py validate` first.")
        return 1

    if not done_criteria_complete(current):
        print("Done Criteria are not all checked. Mark completed criteria with `[x]` before completing.")
        return 1

    if phase_requires_review(current) and state["phases"][current].get("status") != "validated":
        state["phases"][current]["status"] = "review_required"
        save_state(state)
        append_run_state(f"- Review required before completing `{current}`")
        append_day_event("phase review required", current)
        sync_handoff(state, event="review required")
        print(f"Review required before completion: {current}")
        return 1

    baseline_dirty = set(state["phases"][current].get("baseline_dirty_files") or [])
    remaining_baseline_dirty = sorted(baseline_dirty.intersection(changed_files()))
    if remaining_baseline_dirty:
        print("Auto commit refused: files dirty before phase start are still changed.")
        print("Commit or remove pre-existing changes separately, then complete this phase.")
        for path in remaining_baseline_dirty:
            print(f"- {path}")
        return 1

    state["phases"][current]["status"] = "completed"
    state["phases"][current]["completed_at"] = now_utc()
    archive_path = archive_phase_file(current)
    if archive_path:
        state["phases"][current]["archive_path"] = archive_path
    state["current_phase"] = None
    save_state(state)
    append_run_state(f"- Completed phase: `{current}`\n- Archive: `{archive_path or '-'}`\n- Commit: pending")
    append_day_event("phase completed", current, details="commit: pending")
    sync_handoff(state, event="phase completed")

    commit_result = "auto commit disabled"
    if AUTO_COMMIT_ON_COMPLETE:
        committed, commit_result = git_auto_commit(current)
        if not committed and commit_result != "no changes to commit":
            print(f"Auto commit failed: {commit_result}")
            append_run_state(f"- Auto commit failed for `{current}`\n- {commit_result}")
            append_day_event("phase auto commit failed", current, details=commit_result)
            sync_handoff(state, event="auto commit failed")
            return 1

    append_day_event("phase commit recorded", current, details=f"commit: {commit_result}")
    sync_handoff(state, event=f"phase completed; commit {commit_result}")
    print(f"Completed: {current}")
    print(f"Commit: {commit_result}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Phase runner and state manager.")
    parser.add_argument("--lane", choices=sorted(LANE_VALIDATE_HOOKS), default="backend")
    subparsers = parser.add_subparsers(dest="command", required=True)

    subparsers.add_parser("status").set_defaults(func=cmd_status)
    subparsers.add_parser("lint-phases").set_defaults(func=cmd_lint_phases)
    show_parser = subparsers.add_parser("show")
    show_parser.add_argument("phase", nargs="?")
    show_parser.set_defaults(func=cmd_show)
    subparsers.add_parser("sync").set_defaults(func=cmd_sync)
    subparsers.add_parser("resume").set_defaults(func=cmd_resume)
    run_parser = subparsers.add_parser("run")
    run_parser.add_argument("command", nargs=argparse.REMAINDER)
    run_parser.set_defaults(func=cmd_run)
    start_parser = subparsers.add_parser("start")
    start_parser.add_argument("--allow-dirty", action="store_true")
    start_parser.set_defaults(func=cmd_start)
    subparsers.add_parser("validate").set_defaults(func=cmd_validate)
    checkpoint_parser = subparsers.add_parser("checkpoint")
    checkpoint_parser.add_argument("message", nargs="+")
    checkpoint_parser.set_defaults(func=cmd_checkpoint)
    review_parser = subparsers.add_parser("review")
    review_parser.add_argument("note", nargs="*")
    review_parser.set_defaults(func=cmd_review)
    subparsers.add_parser("complete").set_defaults(func=cmd_complete)

    args = parser.parse_args()
    configure_lane(args.lane)
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
