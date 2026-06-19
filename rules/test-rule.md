# Test Rule

테스트는 포트폴리오 증거다. 기능이 맞는지만 보지 않고 transaction, 동시성, 정산/대사 정합성이 깨지지 않는다는 근거를 남긴다.

## 기본 규칙

- behavior change는 테스트를 먼저 작성하거나 수정한다.
- production code 변경이 있으면 관련 테스트 또는 검증 근거가 있어야 한다.
- domain/application/bootstrap/infra/external/batch 테스트 책임을 분리한다.
- 같은 시나리오를 여러 계층에서 반복 검증하지 않는다.
- 테스트 이름은 조건, 행동, 기대 결과가 드러나게 작성한다.

## Layer Rule

- domain 규칙은 `domain` 모듈에서 BehaviorSpec으로 검증한다.
- application orchestration은 `application` 모듈에서 port를 MockK로 대체해 검증한다.
- controller HTTP mapping과 error response는 `bootstrap` 모듈에서 검증한다.
- JPA, QueryDSL, SQL schema, constraint는 `infra` integration test로 검증한다.
- batch, external/message adapter는 각 모듈에서 adapter 책임만 검증한다.

## Reliability Test Rule

- transaction atomicity는 integration test로 검증한다.
- idempotency와 lock 충돌은 실제 DB 기반 동시성 테스트를 둔다.
- outbox는 저장, publish 성공, 실패, 재시도 상태 전이를 검증한다.
- settlement/reconciliation은 정상 집계와 mismatch 탐지를 함께 검증한다.

## Trade-off

- 단위 테스트는 빠르고 원인 파악이 쉽지만 DB constraint와 transaction 문제를 증명하지 못한다.
- 통합 테스트는 신뢰도가 높지만 느리고 fixture 관리 비용이 있다.
- 동시성 테스트는 포트폴리오 증거가 강하지만 실행 시간이 늘고 비결정성을 줄이는 설계가 필요하다.
- 모든 계층에서 같은 케이스를 반복하면 안전해 보이지만 유지보수 비용이 커진다.

## Completion Rule

- 검증하지 못한 항목은 완료 처리하지 말고 이유와 남은 위험을 Obsidian에 남긴다.
- phase의 `Done Criteria`와 테스트 결과가 맞아야 한다.
- 테스트가 없는 문서 변경은 참조, 링크, 형식 검증으로 대체할 수 있다.

## Reference

- 작업 접수 규칙: `rules/work-intake.md`
- 트랜잭션 규칙: `rules/transaction-rule.md`
- 동시성 규칙: `rules/concurrency-rule.md`
- 이벤트 발행 규칙: `rules/event-publication-rule.md`
