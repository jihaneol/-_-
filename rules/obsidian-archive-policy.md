# Obsidian Archive Policy

프로젝트 내부 `workflow/phases/` 폴더는 실행 가능한 phase 최종본만 유지한다.

수정 이유, 진행 과정, 완료된 작업 기록, 삭제된 작업의 맥락은 Obsidian Idea Lab에 보관한다.

## Obsidian Locations

기본 위치:

```text
/Users/bigs/Documents/Obsidian Vault/02. Area/03. Idea Lab
```

프로젝트별 사용 위치:

```text
07.Build Logs/card-service/작업기록.md
07.Build Logs/card-service/days/YYYY-MM-DD-N일차.md
09.Context Handoffs/01.Active Work/card-service/현재작업.md
04.Decisions/card-service/결정기록.md
```

프로젝트별 Obsidian 기록은 반드시 프로젝트 이름 폴더 아래에 둔다. 다른 프로젝트도 같은 패턴을 사용한다.

```text
07.Build Logs/{project-name}/작업기록.md
07.Build Logs/{project-name}/days/YYYY-MM-DD-N일차.md
09.Context Handoffs/01.Active Work/{project-name}/현재작업.md
04.Decisions/{project-name}/결정기록.md
```

`작업기록.md`는 인덱스와 최신 요약만 담는다. 상세 완료 기록은 `days/` 아래의 일차별 파일에 쓴다.

## Local Project Files

프로젝트 안에는 현재 상태만 남긴다.

| File | Role | History policy |
|---|---|---|
| `docs/operations/00-inbox.md` | 아직 정리되지 않은 새 아이디어 | 처리 후 Obsidian에 기록하고 항목 삭제 가능 |
| `docs/operations/01-feature-candidates.md` | 현재 검토 중인 기능 후보 | 승인/거절 후 Obsidian에 기록하고 정리 |
| `docs/what/02-roadmap.md` | 현재 우선순위 최종본 | 변경 이유는 Obsidian에 기록 |
| `workflow/phases/*.md` | active phase 구현 계약 | 완료 후 Obsidian에 기록하고 phase 파일은 `workflow/archive/YYYY-MM-DD/`로 이동 |
| `workflow/archive/` | 완료된 phase 파일 | 실행 대기열에서 제거된 완료 계약 |
| `docs/operations/05-dev-checklist.md` | 현재 체크리스트 최종본 | 변경 이유는 Obsidian에 기록 |
| `AGENT.md` | Codex 프로젝트 운영 진입점 | 변경 이유는 Obsidian에 기록 |
| `rules/backend-architecture.md` | 백엔드 모듈, CQRS, QueryDSL 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/controller-code-rule.md` | 컨트롤러 코드 작성 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/database-schema-rule.md` | DB schema, constraint, index 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/error-message-rule.md` | 에러 메시지 작성 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/naming-rule.md` | 네이밍과 패키지 구조 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/rule-authoring-rule.md` | 규칙 문서 작성 기준 | 변경 이유는 Obsidian에 기록 |
| `rules/service-code-rule.md` | 서비스 코드 작성 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/test-rule.md` | 공통 테스트 기준 | 변경 이유는 Obsidian에 기록 |
| `rules/transaction-rule.md` | 트랜잭션 경계와 atomic write 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/concurrency-rule.md` | 동시성, idempotency, lock 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/event-publication-rule.md` | 이벤트 발행과 outbox 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/git-workflow.md` | 현재 Git 규칙 최종본 | 변경 이유는 Obsidian에 기록 |
| `rules/obsidian-archive-policy.md` | 현재 Obsidian 보관 규칙 | 변경 이유는 Obsidian에 기록 |
| `rules/work-intake.md` | 기능 접수와 active work 승격 규칙 | 변경 이유는 Obsidian에 기록 |

## Update Rule

문서를 수정할 때마다 우리 대화에서 나온 결정, 요청, 수정 이유를 기준으로 Obsidian 일차별 작업기록에 남긴다.

Phase 상태 변경은 `scripts/execute.py`로 수행한다. `scripts/execute.py`는 다음 파일을 자동 갱신한다.

- `workflow/state/run-state.md`
- `.codex/context/active-handoff.md`
- `09.Context Handoffs/01.Active Work/card-service/현재작업.md`
- `07.Build Logs/card-service/days/YYYY-MM-DD-N일차.md`

긴 작업 중에는 `python3 scripts/execute.py checkpoint "현재 상태"`로 중간 복구 지점을 남긴다. 이 기록은 로컬 handoff와 Obsidian 현재작업에 함께 반영된다.

```text
Date:
Work:
Status:
Summary:
Conversation basis:
Changed:
Why:
Scope:
Local files:
Git:
Verified:
Decision:
User reflection:
Risks:
Next:
```

## Completion Record Template

작업 완료 시에는 나중에 비슷한 작업과 구별할 수 있도록 상세히 쓴다.

```md
## YYYY-MM-DD - 작업명 완료

Work:
- 작업 ID 또는 작업명:
- Active work:
- Branch:

Status:
- completed

Summary:
- 한 문장으로 무엇을 끝냈는지 적는다.

Conversation basis:
- 사용자 요청:
- 대화에서 확정한 기준:
- 바뀐 방향:

Changed:
- 실제 변경 내용을 파일/영역 기준으로 적는다.
- 백엔드, 프론트, 문서, 테스트를 구분한다.

Why:
- 이 작업이 왜 필요했는지 적는다.
- 채용공고 역량 또는 포트폴리오 증거와 연결한다.

Scope:
- In scope:
- Out of scope:

Local files:
- 변경된 주요 로컬 파일 경로.

Git:
- Commit: `short-hash type: 커밋 제목`

Verified:
- 실행한 명령:
- 결과:
- 실행하지 못했다면 이유:

Decision:
- 작업 중 확정한 설계/기획 결정.

User reflection:
- 어려웠던 부분:
- 헷갈렸던 부분:
- 나중에 다시 보고 싶은 포인트:
- 내가 직접 보강할 내용:

Risks:
- 남은 위험이나 후속 확인 사항.

Next:
- 다음 작업.
```

## Completion Rule

작업이 끝나면:

1. 오늘 날짜의 Obsidian day file을 만든다. 예: `07.Build Logs/card-service/days/2026-06-19-2일차.md`.
2. 완료 내용을 day file에 상세히 정리한다.
3. Obsidian `07.Build Logs/card-service/작업기록.md`에는 날짜별 링크와 핵심 요약만 추가한다.
4. Obsidian `09.Context Handoffs/01.Active Work/card-service/현재작업.md`에 최종 상태와 다음 작업을 남긴다.
5. 완료 커밋의 짧은 해시와 제목을 day file에 추가한다.
6. 프로젝트 `workflow/state/run-state.md`와 `workflow/state/execute-state.json`을 다음 작업 기준으로 갱신한다.
7. 완료된 임시 작업 파일이 있다면 삭제한다.
8. 삭제 전 필요한 내용은 Obsidian에 이미 있어야 한다.

## Deletion Rule

삭제해도 되는 파일:

- 완료된 임시 feature brief.
- 완료된 임시 active-work 복사본.
- 더 이상 현재 기준이 아닌 중간 초안.
- 로컬 완료 기록 파일.
- 로컬 결정 기록 파일.

삭제하면 안 되는 파일:

- `docs/operations/00-inbox.md`
- `docs/operations/01-feature-candidates.md`
- `docs/what/02-roadmap.md`
- `workflow/phases/*.md`
- `docs/operations/05-dev-checklist.md`
- `.codex/templates/*`
- `AGENT.md`
- `rules/backend-architecture.md`
- `rules/controller-code-rule.md`
- `rules/database-schema-rule.md`
- `rules/error-message-rule.md`
- `rules/naming-rule.md`
- `rules/rule-authoring-rule.md`
- `rules/service-code-rule.md`
- `rules/test-rule.md`
- `rules/transaction-rule.md`
- `rules/concurrency-rule.md`
- `rules/event-publication-rule.md`
- `rules/git-workflow.md`
- `rules/obsidian-archive-policy.md`
- `rules/work-intake.md`

핵심 운영 파일은 삭제하지 않고 현재 최종본으로 갱신한다.

완료 기록과 결정 기록은 로컬에 별도 파일로 유지하지 않는다. Obsidian의 프로젝트별 폴더를 사용한다.
