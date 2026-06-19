#!/usr/bin/env python3
import argparse
import re
import shlex
import sys


DANGEROUS_PATTERNS = [
    (re.compile(r"(^|\s)rm\s+(-[^\s]*[rf][^\s]*|-[^\s]*[fr][^\s]*)\s+(/|\*|~|\.)?"), "destructive recursive remove"),
    (re.compile(r"(^|\s)git\s+reset\s+--hard(\s|$)"), "hard git reset"),
    (re.compile(r"(^|\s)git\s+clean\s+-[^\s]*[fd][^\s]*"), "force git clean"),
    (re.compile(r"(^|\s)git\s+checkout\s+--\s+"), "destructive git checkout"),
    (re.compile(r"(^|\s)git\s+restore\s+.*--source"), "source restore can discard work"),
    (re.compile(r"(^|\s)sudo(\s|$)"), "sudo command"),
    (re.compile(r"(^|\s)chmod\s+777(\s|$)"), "world-writable chmod"),
    (re.compile(r"(^|\s)dd\s+if=.*\s+of="), "raw disk write"),
    (re.compile(r"(^|\s)mkfs(\.|\s|$)"), "filesystem format"),
    (re.compile(r"(^|\s)killall(\s|$)"), "process-wide kill"),
    (re.compile(r"(curl|wget)\s+.*(\||>)\s*(sh|bash)"), "downloaded script execution"),
]


def command_text(argv: list[str]) -> str:
    return " ".join(shlex.quote(part) for part in argv)


def main() -> int:
    parser = argparse.ArgumentParser(description="Block dangerous commands before workflow execution.")
    parser.add_argument("command", nargs=argparse.REMAINDER)
    args = parser.parse_args()

    command = args.command
    if command and command[0] == "--":
        command = command[1:]
    if not command:
        print("Command guard failed: no command provided.", file=sys.stderr)
        return 1

    text = command_text(command)
    for pattern, reason in DANGEROUS_PATTERNS:
        if pattern.search(text):
            print("Command guard blocked a dangerous command.", file=sys.stderr)
            print(f"Reason: {reason}", file=sys.stderr)
            print(f"Command: {text}", file=sys.stderr)
            print("Choose a non-destructive alternative or ask the user before proceeding.", file=sys.stderr)
            return 2

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
