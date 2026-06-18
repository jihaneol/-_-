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
chore/frontend-scaffold
feat/payment-authorization
feat/payment-cancellation
feat/settlement-batch
feat/reconciliation
feat/admin-payment-page
test/payment-concurrency
refactor/payment-domain
```

## Frontend Branch Rule

프론트 작업은 백엔드 작업과 분리한다.

권장 순서:

```text
main
  -> chore/project-scaffold
  -> chore/frontend-scaffold
```

백엔드 스캐폴딩이 `main`에 반영된 뒤라면:

```text
main
  -> chore/frontend-scaffold
```

규칙:

- React/Vite 초기 생성은 `chore/frontend-scaffold`에서 한다.
- 프론트 기능 구현은 스캐폴딩과 분리해 `feat/*` 브랜치에서 한다.
- API 계약이 아직 없으면 실제 API 연동 대신 MSW mock과 화면 구조까지만 만든다.
- 백엔드 기능 브랜치와 프론트 기능 브랜치를 한 브랜치에 섞지 않는다.

프론트 브랜치 예시:

```text
chore/frontend-scaffold
feat/admin-dashboard
feat/payment-admin-page
feat/settlement-screen
feat/reconciliation-screen
test/payment-ui-flow
```

## Work Unit Rule

하나의 작업은 `work/03-active-work.md`에 올라온 범위만 포함한다.

완료된 작업의 상세 과정과 수정 이유는 Obsidian에 보관한다. 프로젝트 안의 `work/` 파일은 현재 최종본만 유지한다.

작업 단위는 다음 중 하나로 나눈다.

| Unit | Meaning | Example |
|---|---|---|
| docs | 하네스, 기획, 의사결정 문서 | `docs: Payment API 계약 정의` |
| chore | 프로젝트 설정, 빌드, 도구 | `chore: Spring Boot 프로젝트 생성` |
| feat | 사용자/운영자 기능 | `feat: 결제 승인 유스케이스 추가` |
| test | 테스트 추가/보강 | `test: 중복 결제 요청 검증` |
| refactor | 동작 변경 없는 구조 개선 | `refactor: 결제 Port 분리` |
| fix | 버그 수정 | `fix: 중복 원장 기록 방지` |
| perf | 성능 개선 | `perf: 정산 조회 인덱스 추가` |

## Commit Message Format

형식:

```text
type: 한국어 요약
```

본문은 필요한 경우에만 짧게 쓴다:

```text
type: 한국어 요약

- Verified:
```

규칙:

- 영어 소문자 `type`을 사용한다.
- 제목은 짧게 유지하고 기본은 한글로 쓴다.
- 영어는 `Spring Boot`, `MockK`, `Payment`, `API`, `MSW`, `Testcontainers`처럼 기술명이나 검색에 필요한 용어에만 섞는다.
- 한 커밋에는 하나의 의도만 담는다.
- 테스트나 검증을 했다면 필요할 때만 본문에 `Verified:`를 남긴다.
- 완료 작업의 상세 설명은 Git 커밋 본문이 아니라 Obsidian에 남긴다.
- Obsidian에는 나중에 추적할 수 있도록 `commit hash + title`만 연결한다.

## Commit Examples

```text
docs: 하네스 템플릿 추가
docs: 카카오페이 결제 범위 정의
chore: Kotlin Spring Boot 백엔드 생성
chore: MySQL Docker Compose 추가
feat: 결제 승인 Aggregate 추가
feat: 결제 승인 API 공개
feat: 결제 취소 유스케이스 추가
feat: 일별 정산 배치 추가
feat: 원장 대사 리포트 추가
feat: 결제 관리자 대시보드 추가
test: 결제 BehaviorSpec 추가
test: 중복 결제 승인 검증
test: 정산 통합 테스트 추가
refactor: 결제 Port 분리
fix: 취소된 결제 재취소 방지
perf: 가맹점 결제 조회 인덱스 추가
```

## Suggested Development Sequence

### 1. Planning

```text
docs: 백엔드 스캐폴딩 작업 설정
```

### 2. Backend Scaffold

```text
chore: Kotlin Spring Boot 백엔드 생성
chore: MySQL Docker Compose 추가
chore: Kotest MockK Testcontainers 설정
```

### 3. Payment Core

```text
feat: Payment 도메인 모델 추가
test: 결제 BehaviorSpec 추가
feat: 결제 승인 유스케이스 추가
feat: 결제 승인 API 공개
test: 결제 승인 통합 테스트 추가
```

### 4. Corrective Flow

```text
feat: 결제 취소 유스케이스 추가
test: 결제 취소 규칙 검증
feat: 결제 취소 API 공개
```

### 5. Consistency

```text
feat: 불변 결제 원장 추가
test: 중복 결제 승인 검증
feat: 일별 정산 배치 추가
feat: 대사 리포트 추가
```

### 6. Frontend

```text
chore: React 관리자 프론트 생성
feat: 결제 관리자 페이지 추가
feat: 정산과 대사 화면 추가
test: MSW 기반 결제 UI 흐름 추가
```

프론트 작업은 `work/03-active-work.md`가 프론트 스캐폴딩이나 프론트 기능 작업으로 바뀐 뒤 시작한다.

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
docs: 현재 작업 정책 수정
```

## Git Log Rule

Git 커밋은 짧고 명확하게 유지한다. 상세한 작업 설명은 Obsidian에 남긴다.

권장 커밋 예시:

```text
feat: 결제 승인 유스케이스 추가
```

Obsidian 작업기록에는 이 정도만 Git 연결값으로 남긴다:

```text
Git:
- Commit: `abc1234 feat: 결제 승인 유스케이스 추가`
```

작업이 여러 커밋으로 나뉘면 관련 커밋을 짧게 나열한다.

```text
Git:
- `abc1234 feat: Payment 도메인 모델 추가`
- `def5678 feat: 결제 승인 유스케이스 추가`
- `fed9876 test: 결제 승인 검증`
```
