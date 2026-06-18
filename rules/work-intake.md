# Work Intake Rule

이 프로젝트는 새 기능을 바로 구현하지 않고, 작은 개발 단위로 다듬은 뒤 `work/03-active-work.md`에 올린 작업만 구현한다.

## Flow

```text
work/00-inbox.md
  -> work/01-feature-candidates.md
  -> work/02-prioritized-roadmap.md
  -> work/03-active-work.md
```

## File Roles

| File | Role | Rule |
|---|---|---|
| `work/00-inbox.md` | 원문에 가까운 아이디어 접수함 | 판단하지 말고 먼저 기록한다. |
| `work/01-feature-candidates.md` | 개발 후보 정리 | 가치, 범위, 제외 범위, API/UI 영향, 테스트를 정리한다. |
| `work/02-prioritized-roadmap.md` | 우선순위 최종본 | Now/Next/Later로 배치한다. |
| `work/03-active-work.md` | 현재 구현 계약 | 지금 구현할 하나의 작업만 둔다. |

## Intake Rule

새 기능이나 범위 변경이 나오면:

1. `work/00-inbox.md`에 사용자 요청과 날짜를 기록한다.
2. 구현할 가치가 있으면 `work/01-feature-candidates.md`에 후보로 다듬는다.
3. 후보가 현재 목표와 맞으면 `work/02-prioritized-roadmap.md`에 Now/Next/Later로 배치한다.
4. 실제 개발을 시작할 때만 `work/03-active-work.md`로 승격한다.
5. 완료 후 상세 기록은 Obsidian에 보관하고, `work/03-active-work.md`는 다음 작업으로 교체한다.

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

이 경우 `work/02-prioritized-roadmap.md`에서 순서를 정하고, `work/03-active-work.md`에는 하나씩만 올린다.

## Active Work Shape

`work/03-active-work.md`에는 최소한 다음 항목을 둔다.

```md
# Active Work

## Title

## Goal

## In Scope

## Out of Scope

## Task Breakdown

## Done Criteria

## Verification
```

## Done Rule

작업이 끝났다고 판단하려면:

- `Done Criteria`가 모두 충족되어야 한다.
- 관련 테스트나 문서 검증이 완료되어야 한다.
- 완료 내용, 수정 이유, 검증 결과, 남은 위험이 Obsidian에 기록되어야 한다.
- 커밋은 사용자가 명시적으로 요청한 경우에만 한다.

