# Context Recovery Hook Plan

## Purpose

긴 작업 중 Codex 컨텍스트가 압축되면 이전 대화와 진행 맥락 일부가 손실될 수 있다.

이 문서는 나중에 압축 전후 handoff 파일을 자동으로 만들기 위한 설계 메모다. 현재는 구현하지 않는다.

## Current Understanding

Codex hook에서 사용할 수 있는 관련 이벤트:

```text
PreCompact(auto|manual)
PostCompact(auto|manual)
SessionStart(compact)
```

가능한 방식:

```text
PreCompact
  -> 압축 직전 현재 작업 상태를 .codex/context/active-handoff.md에 저장

PostCompact
  -> 압축 직후 복구 로그를 남김

SessionStart(compact)
  -> 압축 후 재시작된 세션에서 active-handoff.md를 먼저 읽도록 유도
```

주의할 점:

- hook 입력으로 현재 남은 context token 수를 직접 받는 방식은 확인되지 않았다.
- 따라서 "토큰이 20% 이하일 때 저장"보다 "압축 직전에 저장"하는 방식이 현실적이다.
- 임시 파일은 압축 직후 바로 삭제하지 않는다. 복구가 확인되고 작업이 끝났을 때만 삭제하거나 비운다.

## Proposed Files

```text
.codex/hooks.json
.codex/hooks/pre_compact_handoff.py
.codex/hooks/post_compact_log.py
.codex/context/active-handoff.md
.codex/context/compaction-log.md
```

## active-handoff.md Content

다음 정보만 짧게 저장한다.

```text
# Active Handoff

## Goal
- 현재 사용자의 최신 요청

## Workspace
- repo path
- branch
- latest commit
- dirty files summary

## Current Work
- 지금까지 한 일
- 아직 해야 할 일
- 건드리면 안 되는 파일이나 주의점

## Verification
- 마지막으로 실행한 테스트/빌드
- 성공/실패 결과

## Obsidian
- 현재 작업 기록 위치
- 이번 작업을 기록해야 하는 위치

## Resume Instruction
- 다음 Codex 턴에서 먼저 읽고 이어갈 내용
```

## Hook Draft

나중에 구현할 때의 설정 예시:

```json
{
  "hooks": {
    "PreCompact": [
      {
        "matcher": "auto|manual",
        "hooks": [
          {
            "type": "command",
            "command": "/usr/bin/python3 \"$(git rev-parse --show-toplevel)/.codex/hooks/pre_compact_handoff.py\"",
            "statusMessage": "Saving handoff before context compaction"
          }
        ]
      }
    ],
    "PostCompact": [
      {
        "matcher": "auto|manual",
        "hooks": [
          {
            "type": "command",
            "command": "/usr/bin/python3 \"$(git rev-parse --show-toplevel)/.codex/hooks/post_compact_log.py\"",
            "statusMessage": "Recording compaction event"
          }
        ]
      }
    ]
  }
}
```

## AGENT.md Rule To Add Later

나중에 실제 hook을 만들 때 `AGENT.md`에 추가할 규칙:

```md
## Context Recovery Rule

작업 시작 시 `.codex/context/active-handoff.md`가 있으면 먼저 읽는다.
컨텍스트 압축 이후에는 해당 handoff를 기준으로 손실된 작업 맥락을 복구한다.
작업이 완료되고 Obsidian 기록까지 끝난 뒤 handoff 파일을 삭제하거나 비운다.
```

## Implementation Steps Later

1. `.codex/context/`와 `.codex/hooks/` 디렉터리를 만든다.
2. `pre_compact_handoff.py`를 만든다.
3. `post_compact_log.py`를 만든다.
4. `.codex/hooks.json`에 `PreCompact`, `PostCompact` hook을 등록한다.
5. `AGENT.md`에 Context Recovery Rule을 추가한다.
6. `/hooks`에서 hook을 확인하고 신뢰 처리한다.
7. 수동 `/compact`로 handoff 생성과 복구 흐름을 테스트한다.
8. 검증 후 Obsidian 작업기록에 결과를 남긴다.

## Done Criteria

- 압축 직전에 `.codex/context/active-handoff.md`가 생성된다.
- handoff에는 현재 목표, 변경 파일, 남은 작업, 검증 결과가 포함된다.
- 압축 후 다음 작업 시작 시 Codex가 handoff를 먼저 읽는다.
- 작업 완료 후 Obsidian 기록이 남고 handoff 파일은 삭제 또는 비움 처리된다.

## Deferred Decision

실제 구현 시 결정할 항목:

- handoff 파일을 항상 덮어쓸지, 날짜별로 보관할지
- Obsidian에도 압축 이벤트를 기록할지
- `Stop` hook에서도 handoff를 갱신할지
- 민감 정보가 handoff에 들어가지 않도록 필터링할지
