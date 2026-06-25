# Test Rule

테스트는 포트폴리오 증거다. 기능이 맞는지만 보지 않고 transaction, 동시성, 정산/대사 정합성이 깨지지 않는다는 근거를 남긴다.

## 기본 규칙

- behavior change는 테스트를 먼저 작성하거나 수정한다.
- production code 변경이 있으면 관련 테스트 또는 검증 근거가 있어야 한다.
- domain/application/admin-api/shop-api/infra/external/batch 테스트 책임을 분리한다.
- 같은 시나리오를 여러 계층에서 반복 검증하지 않는다.
- 테스트 이름은 조건, 행동, 기대 결과가 드러나게 작성한다.

## Layer Rule

- domain 규칙은 `domain` 모듈에서 BehaviorSpec으로 검증한다.
- application orchestration은 `application` 모듈에서 port를 MockK로 대체해 검증한다.
- controller HTTP mapping과 error response는 `admin-api` 또는 `shop-api` 모듈에서 검증한다.
- JPA, QueryDSL, SQL schema, constraint는 `infra` integration test로 검증한다.
- batch, external/message adapter는 각 모듈에서 adapter 책임만 검증한다.

## Reliability Test Rule

- transaction atomicity는 integration test로 검증한다.
- idempotency와 lock 충돌은 실제 DB 기반 동시성 테스트를 둔다.
- outbox는 저장, publish 성공, 실패, 재시도 상태 전이를 검증한다.
- settlement/reconciliation은 실제 구현을 시작한 뒤 정상 집계와 mismatch 탐지를 함께 검증한다. 빈 placeholder 테스트나 패키지는 만들지 않는다.

## Smoke And Load Test Rule

- smoke test는 테스트 경로가 실행 가능한지 확인하는 용도다. 성능 결론으로 사용하지 않는다.
- smoke test는 짧고 작게 유지한다. 예: `VUS=1~2`, `DURATION=5s~10s`.
- smoke test가 확인해야 하는 것은 API 접근, fixture 생성, 주요 check 통과, metric 수집 여부다.
- baseline load test는 비교 가능한 성능 수치를 남기는 용도다. Before/After 비교는 같은 VUS, duration, 데이터 준비 방식, 측정 항목을 사용한다.
- load test 결과에는 최소한 p50, p95, p99, error rate, 성공 처리 수, 실패 수, 중복 side effect 여부를 남긴다.
- Kafka/outbox 비교에서는 request latency만 보지 말고 outbox pending count, projection lag, publish retry count, consumer duplicate replay count를 함께 기록한다.
- 인위적인 지연(`Thread.sleep`, 의미 없는 반복문)을 넣어 병목을 만들지 않는다. 실제 비용이 있는 DB write, projection update, audit insert, 집계 upsert를 기준으로 비교한다.
- smoke가 통과해도 baseline이 실행되지 않았으면 "성능 측정 완료"로 기록하지 않는다.

## Trade-off

- 단위 테스트는 빠르고 원인 파악이 쉽지만 DB constraint와 transaction 문제를 증명하지 못한다.
- 통합 테스트는 신뢰도가 높지만 느리고 fixture 관리 비용이 있다.
- 동시성 테스트는 포트폴리오 증거가 강하지만 실행 시간이 늘고 비결정성을 줄이는 설계가 필요하다.
- smoke test는 빠르게 피드백을 주지만 성능 대표성이 없다.
- load test는 비교 증거가 강하지만 실행 환경, 데이터 상태, warm-up, Docker/로컬 리소스 영향을 함께 기록해야 한다.
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
