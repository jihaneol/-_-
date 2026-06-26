# Phase 011: Order Payment Conditional Update

## Problem

`OrderPaymentFacade.payOrder` currently starts by loading the order with `PESSIMISTIC_WRITE`.

Under duplicate payment requests or PG callback retries for the same order, the first request holds the order row lock while it performs inventory update, payment persistence, coupon issuance, coupon history persistence, and outbox append. Later requests wait for the full transaction tail.

Kafka/outbox does not fix this bottleneck because the request path still performs the order lock, inventory update, payment write, coupon writes, and outbox insert before commit.

## Target Flow

Before:

```text
select order for update
  -> idempotency lookup
  -> inventory conditional update
  -> payment save
  -> order.pay()
  -> order save
  -> coupon saveAll
  -> coupon history saveAll
  -> outbox save
  -> commit
```

After this phase:

```text
plain order lookup
  -> idempotency lookup
  -> payment save
  -> inventory conditional update
  -> coupon saveAll
  -> coupon history saveAll
  -> outbox save
  -> conditional order update CREATED -> PAID
  -> commit
```

The order row can still be locked by the database during the conditional update, but the code no longer holds an explicit order lock through the whole payment transaction. The conditional update is intentionally placed at the end of the write path so the order row lock is held for the shortest possible tail before commit.

## Duplicate Scenario

Two successful PG callbacks for `orderId=10` arrive at nearly the same time:

```text
A: save payment
A: update orders set status = PAID where id = 10 and status = CREATED -> 1 row
A: continue and commit

B: save payment
B: update orders set status = PAID where id = 10 and status = CREATED -> 0 rows
B: throw exception and rollback
```

The failed request rolls back its payment and inventory changes because they are in the same transaction.

## Coupon Rule

Coupon issuance may later move to after-commit or outbox consumer processing. It must be triggered only after the payment transaction commits. If the conditional order update fails and the transaction rolls back, no coupon issuance should run.

This phase keeps coupon issuance synchronous to avoid changing the API contract in the same slice.

## Implementation

- Add an application port for conditional order status mutation.
- Implement it in infra with a single JPQL update.
- Change `OrderPaymentFacade.payOrder` to use plain order lookup plus conditional update.
- Keep refund and coupon exchange pessimistic locking unchanged.
- Add behavior tests proving:
  - new payment uses conditional order update instead of order lock;
  - conditional update failure throws so the transaction rolls back;
  - idempotent retry does not repeat side effects.

## Result

- Added `OrderStatusMutationPort`.
- Added `JpaOrderStatusMutationAdapter` with a single conditional JPQL update.
- Changed `OrderPaymentFacade.payOrder` to use plain order lookup and conditional `CREATED -> PAID` update.
- Kept refund and coupon exchange pessimistic lock behavior unchanged.
- Kept coupon issuance synchronous in this slice.

## Completed Validation

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :application:test --tests '*OrderPaymentFacadeBehaviorSpec'
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :admin-api:test --tests '*OrderFlowIntegrationTest' -Dtestcontainers.enabled=true
```

## Validation

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :application:test --tests '*OrderPaymentFacadeBehaviorSpec'
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test
```
