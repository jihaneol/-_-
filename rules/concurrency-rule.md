# Concurrency Rule

동시성 방어는 application 코드의 실행 순서가 아니라 DB constraint, lock, transaction, idempotency contract로 보장한다.

## 기본 규칙

- 중복 생성 방지는 DB unique constraint를 기본 방어선으로 둔다.
- 결제 승인 idempotency key에는 unique constraint를 둔다.
- `find -> if null -> save` 흐름만으로 중복 생성을 막았다고 판단하지 않는다.
- `synchronized`, local memory lock, local cache만으로 결제 중복을 막지 않는다.
- 여러 서버 인스턴스에서 동시에 요청돼도 같은 결과가 나와야 한다.

## Idempotency Rule

- 같은 idempotency key와 같은 요청 의미는 같은 payment 결과를 반환한다.
- 같은 idempotency key지만 merchant, order, amount, currency 같은 요청 의미가 다르면 충돌로 처리한다.
- duplicate key 예외가 발생하면 새로 만들기를 재시도하지 말고 기존 payment를 다시 조회해 결과를 만든다.
- idempotency key는 controller convenience 값이 아니라 domain/application 흐름의 핵심 입력으로 다룬다.
- idempotency 응답 정책은 API contract와 test에 남긴다.

## Lock Rule

- 상태 변경 대상이 이미 존재하는 aggregate라면 optimistic locking을 기본 선택지로 검토한다.
- 동시에 취소, 정산, 상태 변경이 들어올 수 있는 흐름은 lock 전략을 명시한다.
- pessimistic lock은 재고 차감처럼 충돌 빈도가 높고 순서 보장이 중요한 경우에만 선택한다.
- lock timeout, deadlock, duplicate key는 사용자에게 이해 가능한 application error로 변환한다.

## Trade-off

- unique constraint는 단순하고 강하지만, 충돌 예외를 정상적인 동시성 흐름으로 처리하는 코드가 필요하다.
- optimistic locking은 lock 점유가 적고 확장에 유리하지만, 충돌 시 재시도 또는 사용자 안내 정책이 필요하다.
- pessimistic locking은 순서를 강하게 보장하지만, 처리량 저하와 deadlock 위험이 있다.
- idempotency key는 API를 안정적으로 만들지만, key 보관 기간과 요청 의미 비교 기준을 정해야 한다.

## Test Rule

- 같은 idempotency key로 동시에 여러 승인 요청을 보내는 integration test를 둔다.
- 테스트 결과는 payment 1건, authorization ledger 1건이어야 한다.
- 같은 key에 다른 amount/order가 들어오는 충돌 케이스를 테스트한다.
- lock 전략이 있는 상태 변경은 충돌 또는 재시도 동작을 테스트한다.
- 동시성 테스트는 portfolio evidence로 유지한다.

## Reference

- 트랜잭션 규칙: `rules/transaction-rule.md`
- DB schema 규칙: `rules/database-schema-rule.md`
- JPA/entity 규칙: `rules/jpa-entity-rule.md`
- 에러 메시지 규칙: `rules/error-message-rule.md`
- 테스트 규칙: `rules/test-rule.md`
