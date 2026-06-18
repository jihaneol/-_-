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
type: 한국어 요약 / English summary
```

본문은 필요한 경우에만 짧게 쓴다:

```text
type: 한국어 요약 / English summary

- Verified:
```

규칙:

- 영어 소문자 `type`을 사용한다.
- 제목은 짧게 유지하되, 한글 요약과 영어 요약을 모두 넣는다.
- 한글을 먼저 쓰고 영어를 뒤에 쓴다.
- 한 커밋에는 하나의 의도만 담는다.
- 테스트나 검증을 했다면 필요할 때만 본문에 `Verified:`를 남긴다.
- 완료 작업의 상세 설명은 Git 커밋 본문이 아니라 Obsidian에 남긴다.
- Obsidian에는 나중에 추적할 수 있도록 `commit hash + bilingual title`만 연결한다.

## Commit Examples

```text
docs: 하네스 템플릿 추가 / add reusable harness templates
docs: 카카오페이 결제 범위 정의 / define kakaopay payment scope
chore: 백엔드 프로젝트 생성 / scaffold kotlin spring boot backend
chore: MySQL 도커 컴포즈 추가 / add mysql docker compose
feat: 결제 승인 애그리거트 추가 / add payment authorization aggregate
feat: 결제 승인 API 공개 / expose payment authorization api
feat: 결제 취소 유스케이스 추가 / add payment cancellation use case
feat: 일별 정산 배치 추가 / add daily settlement batch
feat: 원장 대사 리포트 추가 / add ledger reconciliation report
feat: 결제 관리자 대시보드 추가 / add payment admin dashboard
test: 결제 행위 테스트 추가 / add payment behavior specs
test: 중복 결제 승인 검증 / cover duplicate payment authorization
test: 정산 통합 테스트 추가 / add settlement integration test
refactor: 결제 포트 분리 / separate payment ports
fix: 취소된 결제 재취소 방지 / reject recancelling payment
perf: 가맹점 결제 조회 인덱스 추가 / add merchant payment lookup index
```

## Suggested Development Sequence

### 1. Planning

```text
docs: 백엔드 스캐폴딩 작업 설정 / set backend scaffold work
```

### 2. Backend Scaffold

```text
chore: 백엔드 프로젝트 생성 / scaffold kotlin spring boot backend
chore: MySQL 도커 컴포즈 추가 / add mysql docker compose
chore: 테스트 도구 설정 / configure kotest mockk and testcontainers
```

### 3. Payment Core

```text
feat: 결제 도메인 모델 추가 / add payment domain model
test: 결제 행위 테스트 추가 / add payment behavior specs
feat: 결제 승인 유스케이스 추가 / add payment authorization use case
feat: 결제 승인 API 공개 / expose payment authorization api
test: 결제 승인 통합 테스트 추가 / add payment authorization integration test
```

### 4. Corrective Flow

```text
feat: 결제 취소 유스케이스 추가 / add payment cancellation use case
test: 결제 취소 규칙 검증 / cover payment cancellation rules
feat: 결제 취소 API 공개 / expose payment cancellation api
```

### 5. Consistency

```text
feat: 불변 결제 원장 추가 / add immutable payment ledger
test: 중복 결제 승인 검증 / cover duplicate payment authorization
feat: 일별 정산 배치 추가 / add daily settlement batch
feat: 대사 리포트 추가 / add reconciliation report
```

### 6. Frontend

```text
chore: 리액트 관리자 프론트 생성 / scaffold react admin frontend
feat: 결제 관리자 페이지 추가 / add payment admin page
feat: 정산과 대사 화면 추가 / add settlement and reconciliation screens
test: MSW 기반 결제 UI 흐름 추가 / add msw-backed payment ui flow
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
docs: 현재 작업 정책 수정 / update active work policy
```

## Git Log Rule

Git 커밋은 짧고 명확하게 유지한다. 상세한 작업 설명은 Obsidian에 남긴다.

권장 커밋 예시:

```text
feat: 결제 승인 유스케이스 추가 / add payment authorization use case
```

Obsidian 작업기록에는 이 정도만 Git 연결값으로 남긴다:

```text
Git:
- Commit: `abc1234 feat: 결제 승인 유스케이스 추가 / add payment authorization use case`
```

작업이 여러 커밋으로 나뉘면 관련 커밋을 짧게 나열한다.

```text
Git:
- `abc1234 feat: 결제 도메인 모델 추가 / add payment domain model`
- `def5678 feat: 결제 승인 유스케이스 추가 / add payment authorization use case`
- `fed9876 test: 결제 승인 검증 / cover payment authorization`
```
