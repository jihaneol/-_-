# Git Workflow

이 프로젝트는 포트폴리오 증거를 남기는 것이 중요하므로, 커밋은 기능 단위보다 더 작게 쪼개되 의미 없는 저장용 커밋은 피한다.

## Branch Strategy

기본 브랜치:

```text
main
```

작업 브랜치 형식:

```text
type/short-topic
```

예시:

```text
docs/harness-update
chore/project-scaffold
feat/payment-authorization
feat/payment-cancellation
feat/settlement-batch
feat/reconciliation
feat/admin-payment-page
test/payment-concurrency
refactor/payment-domain
```

## Work Unit Rule

하나의 작업은 `work/03-active-work.md`에 올라온 범위만 포함한다.

완료된 작업의 상세 과정과 수정 이유는 Obsidian에 보관한다. 프로젝트 안의 `work/` 파일은 현재 최종본만 유지한다.

작업 단위는 다음 중 하나로 나눈다.

| Unit | Meaning | Example |
|---|---|---|
| docs | 하네스, 기획, 의사결정 문서 | `docs: define payment API contract` |
| chore | 프로젝트 설정, 빌드, 도구 | `chore: scaffold spring boot project` |
| feat | 사용자/운영자 기능 | `feat: add payment authorization use case` |
| test | 테스트 추가/보강 | `test: cover duplicate authorization requests` |
| refactor | 동작 변경 없는 구조 개선 | `refactor: isolate payment outbound ports` |
| fix | 버그 수정 | `fix: prevent duplicate ledger append` |
| perf | 성능 개선 | `perf: add settlement query index` |

## Commit Message Format

형식:

```text
type: imperative summary
```

본문은 필요한 경우에만 짧게 쓴다:

```text
type: imperative summary

- Verified:
```

규칙:

- 영어 소문자 `type`을 사용한다.
- 제목은 72자 안쪽으로 유지한다.
- 제목은 명령형으로 쓴다.
- 한 커밋에는 하나의 의도만 담는다.
- 테스트나 검증을 했다면 필요할 때만 본문에 `Verified:`를 남긴다.
- 완료 작업의 상세 설명은 Git 커밋 본문이 아니라 Obsidian에 남긴다.
- Obsidian에는 나중에 추적할 수 있도록 `commit hash + title`만 연결한다.

## Commit Examples

```text
docs: add reusable project harness templates
docs: define kakaopay payment service scope
chore: scaffold kotlin spring boot backend
chore: add mysql docker compose
feat: add payment authorization aggregate
feat: expose payment authorization api
feat: add payment cancellation use case
feat: add daily settlement batch
feat: add ledger reconciliation report
feat: add payment admin dashboard
test: add payment behavior specs
test: cover duplicate payment authorization
test: add settlement integration test
refactor: separate payment ports and adapters
fix: reject cancellation for already cancelled payment
perf: add merchant payment lookup index
```

## Suggested Development Sequence

### 1. Planning

```text
docs: update active work for backend scaffold
```

### 2. Backend Scaffold

```text
chore: scaffold kotlin spring boot backend
chore: add mysql docker compose
chore: configure kotest mockk and testcontainers
```

### 3. Payment Core

```text
feat: add payment domain model
test: add payment behavior specs
feat: add payment authorization use case
feat: expose payment authorization api
test: add payment authorization integration test
```

### 4. Corrective Flow

```text
feat: add payment cancellation use case
test: cover payment cancellation rules
feat: expose payment cancellation api
```

### 5. Consistency

```text
feat: add immutable payment ledger
test: cover duplicate payment authorization
feat: add daily settlement batch
feat: add reconciliation report
```

### 6. Frontend

```text
chore: scaffold react admin frontend
feat: add payment admin page
feat: add settlement and reconciliation screens
test: add msw-backed payment ui flow
```

## Commit Before Checklist

- [ ] 변경 범위가 `work/03-active-work.md`와 맞는다.
- [ ] 불필요한 IDE/local 파일이 포함되지 않았다.
- [ ] 문서 변경과 코드 변경이 너무 섞이지 않았다.
- [ ] 관련 테스트 또는 검증을 실행했다.
- [ ] 검증하지 못했다면 Obsidian 작업기록에 이유를 남겼다.
- [ ] Obsidian 작업기록에 작업 목적, 변경 내용, 검증, 다음 작업, Git 커밋 해시와 제목을 기록했다.
- [ ] 수정 이유와 완료 기록을 Obsidian에 정리했다.

## Do Not Commit

- `.DS_Store`
- `.idea/`
- `.env`
- 빌드 산출물
- 로컬 DB 데이터
- 개인 토큰/키

## Current Repository Baseline

초기 커밋:

```text
bb67b22 Initialize project planning harness
```

이후 커밋은 이 문서의 규칙을 따른다.

## Obsidian Link Rule

문서나 작업 기준이 바뀌는 커밋은 Obsidian 작업기록에도 같은 이유를 남긴다.

권장 커밋:

```text
docs: update active work policy
```

## Git Log Rule

Git 커밋은 짧고 명확하게 유지한다. 상세한 작업 설명은 Obsidian에 남긴다.

권장 커밋 예시:

```text
feat: add payment authorization use case
```

Obsidian 작업기록에는 이 정도만 Git 연결값으로 남긴다:

```text
Git:
- Commit: `abc1234 feat: add payment authorization use case`
```

작업이 여러 커밋으로 나뉘면 관련 커밋을 짧게 나열한다.

```text
Git:
- `abc1234 feat: add payment domain model`
- `def5678 feat: add payment authorization use case`
- `fed9876 test: cover payment authorization`
```
