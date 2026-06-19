# Work Intake Rule

이 프로젝트는 새 기능을 바로 구현하지 않고, 작은 phase로 다듬은 뒤 `workflow/phases/*.md`에 올린 작업만 구현한다.

## Flow

```text
docs/operations/00-inbox.md
  -> docs/operations/01-feature-candidates.md
  -> docs/what/02-roadmap.md
  -> workflow/phases/phase-NNN-title.md
```

## File Roles

| File | Role | Rule |
|---|---|---|
| `docs/operations/00-inbox.md` | 원문에 가까운 아이디어 접수함 | 판단하지 말고 먼저 기록한다. |
| `docs/operations/01-feature-candidates.md` | 개발 후보 정리 | 가치, 범위, 제외 범위, API/UI 영향, 테스트를 정리한다. |
| `docs/what/02-roadmap.md` | 우선순위 최종본 | Now/Next/Later로 배치한다. |
| `workflow/phases/*.md` | phase 구현 계약 | 하나의 phase마다 독립적인 완료/검증 기준을 둔다. |

## Intake Rule

새 기능이나 범위 변경이 나오면:

1. `docs/operations/00-inbox.md`에 사용자 요청과 날짜를 기록한다.
2. 구현할 가치가 있으면 `docs/operations/01-feature-candidates.md`에 후보로 다듬는다.
3. 후보가 현재 목표와 맞으면 `docs/what/02-roadmap.md`에 Now/Next/Later로 배치한다.
4. 실제 개발을 시작할 때만 `workflow/phases/phase-NNN-title.md`로 승격한다.
5. 완료 후 상세 기록은 Obsidian에 보관하고, phase 파일은 `workflow/archive/YYYY-MM-DD/`로 이동하며, phase 상태는 `scripts/execute.py`와 `workflow/state/`에 기록한다.

## Multiple Work Rule

두 개 이상의 작업이 있을 때는 다음 기준으로 묶거나 분리한다.

### 묶어도 되는 경우

하나의 완료 기준으로 검증되어야 하고, 같은 트랜잭션/흐름 안에서 의미가 완성되는 작업은 하나의 active work로 묶을 수 있다.

예시:

```text
Payment idempotency + immutable ledger
```

이 경우 중복 요청 방지와 원장 기록은 같은 결제 승인 흐름에서 함께 검증되어야 하므로 하나의 active work가 될 수 있다.

### 분리해야 하는 경우

완료 기준, 리뷰 기준, 배포 기준, 검증 기준이 다르면 분리한다.

예시:

```text
Payment cancellation API
React admin frontend scaffold
Daily settlement batch
```

이 경우 `docs/what/02-roadmap.md`에서 순서를 정하고, `workflow/phases/`에는 순서가 드러나는 phase 파일로 하나씩 올린다.

## Active Work Shape

`workflow/phases/*.md`에는 최소한 다음 항목을 둔다.

```md
# Phase NNN: Title

## Goal

## Docs Read

## Scope

## Out Of Scope

## Files To Touch

## Test First

## Implementation Steps

## Done Criteria

## Validation

## Review Focus
```

## Done Rule

작업이 끝났다고 판단하려면:

- 기능/수정 작업은 테스트를 먼저 작성하거나 수정해야 한다.
- production code 변경이 있는데 테스트 변경이 없으면 완료할 수 없다.
- `Done Criteria`가 모두 충족되어야 한다.
- 관련 테스트나 문서 검증이 완료되어야 한다.
- 공통 테스트 기준은 `rules/test-rule.md`를 따른다.
- 완료 내용, 수정 이유, 검증 결과, 남은 위험이 Obsidian에 기록되어야 한다.
- 장시간 구현 모드에서는 `scripts/execute.py complete`가 검증된 완료 작업을 자동 커밋한다.
