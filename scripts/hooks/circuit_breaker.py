#!/usr/bin/env python3
import argparse
import hashlib
import json
import sys
from datetime import datetime, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
STATE_FILE = ROOT / "harness" / "state" / "circuit-breaker.json"
THRESHOLD = 6


def now_utc() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def load_state() -> dict:
    if not STATE_FILE.exists():
        return {"failures": {}}
    return json.loads(STATE_FILE.read_text())


def save_state(state: dict) -> None:
    STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
    STATE_FILE.write_text(json.dumps(state, indent=2, ensure_ascii=False) + "\n")


def key_for(text: str) -> str:
    digest = hashlib.sha256(text.encode()).hexdigest()[:16]
    return digest


def cmd_check(args: argparse.Namespace) -> int:
    state = load_state()
    key = key_for(args.key)
    item = state.get("failures", {}).get(key)
    if item and item.get("count", 0) >= THRESHOLD:
        print("Circuit breaker is open for this repeated failure.", file=sys.stderr)
        print(f"Failures: {item.get('count')}", file=sys.stderr)
        print(f"Last failure: {item.get('last_failure_at')}", file=sys.stderr)
        print(f"Summary: {item.get('summary')}", file=sys.stderr)
        print("Stop repeating the same action. Write an alternative plan, reduce scope, or switch to another phase.", file=sys.stderr)
        return 2
    return 0


def cmd_record(args: argparse.Namespace) -> int:
    state = load_state()
    failures = state.setdefault("failures", {})
    key = key_for(args.key)

    if args.status == "passed":
        failures.pop(key, None)
        save_state(state)
        return 0

    item = failures.setdefault(
        key,
        {
            "count": 0,
            "first_failure_at": now_utc(),
            "key": args.key,
        },
    )
    item["count"] = item.get("count", 0) + 1
    item["last_failure_at"] = now_utc()
    item["summary"] = args.summary
    save_state(state)

    if item["count"] >= THRESHOLD:
        print("Circuit breaker opened after repeated failures.", file=sys.stderr)
        print(f"Failures: {item['count']}", file=sys.stderr)
        print("Stop and choose an alternative approach before retrying.", file=sys.stderr)
        return 2
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Track repeated workflow failures.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    check = subparsers.add_parser("check")
    check.add_argument("--key", required=True)
    check.set_defaults(func=cmd_check)

    record = subparsers.add_parser("record")
    record.add_argument("--key", required=True)
    record.add_argument("--status", choices=["passed", "failed"], required=True)
    record.add_argument("--summary", default="-")
    record.set_defaults(func=cmd_record)

    args = parser.parse_args()
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
