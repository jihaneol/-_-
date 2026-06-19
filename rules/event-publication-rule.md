# Event Publication Rule

이벤트는 도메인에서 중요한 일이 발생했다는 사실을 표현하되, 브로커 발행 성공을 business transaction 안에서 직접 보장하려고 하지 않는다.

## 기본 규칙

- domain event는 domain model 또는 domain service가 만들 수 있다.
- event 저장, 발행 요청, retry orchestration은 application/external/infra 경계에서 처리한다.
- Kafka/RabbitMQ 같은 broker 세부 기술은 domain과 application model에 새지 않게 한다.
- payment 상태 변경과 ledger append가 필요한 이벤트는 outbox row와 함께 같은 transaction에 저장한다.
- business transaction 안에서 broker에 직접 publish하지 않는다.

## Outbox Rule

- outbox event에는 event type, aggregate id, payload, status, occurredAt, retry count를 둔다.
- payload에는 JPA entity 전체를 넣지 않고 안정적인 식별자, 상태, 금액, 시각 같은 필요한 값만 넣는다.
- publisher는 pending outbox event를 읽어 broker에 발행하고 성공하면 sent 상태로 바꾼다.
- 발행 실패는 failed 또는 retryable 상태로 남기고 원인을 기록한다.
- 같은 event가 두 번 발행될 수 있음을 전제로 consumer는 멱등하게 처리한다.

## Event Timing Rule

- 내부 DB 상태 변경이 성공해야만 외부 이벤트가 발행 대상이 된다.
- commit 전 event publish 성공에 의존하는 흐름을 만들지 않는다.
- 이벤트 수신자가 즉시 처리하지 않아도 핵심 payment transaction은 완료될 수 있어야 한다.
- 정산, 대사, 알림처럼 후속 처리가 늦어져도 되는 흐름부터 event 기반으로 분리한다.

## Trade-off

- transactional outbox는 발행 유실을 줄이지만, outbox table과 publisher 운영 부담이 생긴다.
- event-driven flow는 서비스 결합을 낮추지만, eventual consistency와 중복 수신을 다뤄야 한다.
- payload를 작게 유지하면 변경에 강하지만, consumer가 추가 조회를 해야 할 수 있다.
- broker 직접 publish는 구현이 단순하지만, DB commit 성공과 publish 성공 사이의 crash gap을 막기 어렵다.

## Test Rule

- payment 변경 transaction이 성공하면 outbox event가 함께 저장되는지 검증한다.
- payment 변경 transaction이 rollback되면 outbox event도 남지 않아야 한다.
- publisher는 성공, 실패, 재시도 상태 전이를 테스트한다.
- consumer가 같은 event를 두 번 받아도 결과가 중복되지 않는지 테스트한다.

## Reference

- 트랜잭션 규칙: `rules/transaction-rule.md`
- 동시성 규칙: `rules/concurrency-rule.md`
- 아키텍처 규칙: `rules/backend-architecture.md`
- 테스트 규칙: `rules/test-rule.md`
