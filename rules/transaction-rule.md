# Transaction Rule

결제, 취소, 정산, 대사처럼 상태와 기록을 함께 바꾸는 흐름은 단순한 service 호출 순서가 아니라 transaction 단위로 설계한다.

## 기본 규칙

- 변경 use case service/facade가 transaction boundary를 소유한다.
- 변경 use case에는 기본적으로 `@Transactional`을 적용한다.
- 조회 use case에는 가능한 경우 `@Transactional(readOnly = true)`를 적용한다.
- 같은 application service/facade 클래스 안에 `@Transactional` 변경 메서드와 `@Transactional(readOnly = true)` 조회 메서드를 같이 두지 않는다.
- 조회 트랜잭션은 `{Feature}QueryService`/`{Feature}QueryFacade` 같은 별도 구현체에 둔다.
- controller, request/response model, domain entity, outbound port interface에는 transaction annotation을 두지 않는다.
- persistence adapter는 DB 저장 세부 구현을 담당하지만, 업무 transaction의 시작과 끝을 결정하지 않는다.
- 하나의 업무 결과로 함께 남아야 하는 payment, ledger, outbox row는 같은 transaction 안에서 저장한다.
- transaction 안에서 실패하면 payment, ledger, outbox 중 일부만 남으면 안 된다.

## 외부 호출 규칙

- 외부 결제 승인, Kafka/RabbitMQ publish, webhook 호출은 DB transaction 안에서 직접 수행하지 않는다.
- 외부 호출이 반드시 선행되어야 하는 흐름은 호출 결과를 받은 뒤 DB transaction을 짧게 열어 내부 상태를 저장한다.
- 내부 상태 변경 뒤 외부로 알려야 하는 흐름은 transactional outbox를 기본 선택지로 둔다.
- 장애 복구가 필요한 외부 호출 실패는 로그만 남기지 않고 재시도 가능한 상태로 기록한다.

## Batch Transaction Rule

- 정산과 대사는 merchant/date 같은 작은 처리 단위로 transaction을 나눈다.
- 한 처리 단위 실패가 전체 batch 결과를 숨기지 않도록 성공, 실패, 재시도 대상을 구분해 기록한다.
- 대량 조회와 대량 쓰기를 하나의 긴 transaction으로 묶지 않는다.
- batch 결과가 다시 실행될 수 있다면 같은 입력에 대해 중복 summary가 생기지 않도록 unique constraint 또는 upsert 전략을 둔다.

## Trade-off

- use case에 transaction을 두면 흐름을 이해하기 쉽고 테스트 경계가 명확하지만, service/facade가 너무 많은 port를 조율하면 transaction이 길어질 수 있다.
- transaction을 짧게 유지하면 DB lock 점유 시간이 줄지만, 외부 호출과 내부 저장 사이의 실패 보상 설계가 필요하다.
- outbox를 쓰면 발행 유실을 줄일 수 있지만, outbox table, publisher, retry 상태 관리가 추가된다.
- batch transaction을 잘게 나누면 부분 성공을 다룰 수 있지만, 전체 결과를 집계하는 별도 상태가 필요하다.

## Test Rule

- payment 저장과 ledger append가 atomic한지는 integration test로 검증한다.
- outbox를 쓰는 흐름은 payment, ledger, outbox가 함께 commit되는지 검증한다.
- transaction rollback이 필요한 실패 케이스는 부분 row가 남지 않는지 확인한다.
- application 단위 MockK test만으로 transaction 보장을 완료 처리하지 않는다.

## Reference

- 아키텍처 규칙: `rules/backend-architecture.md`
- 서비스 규칙: `rules/service-code-rule.md`
- 이벤트 발행 규칙: `rules/event-publication-rule.md`
- 테스트 규칙: `rules/test-rule.md`
