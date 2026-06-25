# Test Strategy Harness

Detailed test strategy lives in `docs/how/03-test-strategy.md`.

## Required Test Layers

- Domain BehaviorSpec tests for pure business rules.
- Application BehaviorSpec tests with MockK for outbound ports.
- Web slice tests for API adapters.
- Spring Boot context smoke tests for `admin-api` and `shop-api`.
- Testcontainers MySQL integration tests when persistence behavior matters.
- React Testing Library and MSW tests for frontend flows.

## Must-Prove Scenarios

- Duplicate idempotency request does not duplicate payment or coupon side effects.
- Payment/order flow deducts inventory once.
- Full refund voids issued coupons and records history.
- Coupon exchange cannot use voided or already exchanged coupons.
- Shop runtime does not expose admin-only routes.
- Admin UI shows loading, empty, error, success, and disabled-pending states.
- Shop pages 05-12 render without admin leakage and preserve purchase-to-wallet behavior.
- Browser overflow checks pass on desktop and mobile widths for shop pages 05-12.
- Outbox rows are persisted atomically with payment/refund state changes.
- Kafka publishing can be retried without losing pending outbox rows.
- Duplicate Kafka delivery does not duplicate consumer side effects.
- Traffic-spike tests compare synchronous projection/audit work against outbox-based projection/audit work under the same concurrent payment load.

## Validation Commands

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test
npm --prefix frontend test -- --run
npm --prefix frontend run build
bash scripts/hooks/validate_impeccable.sh
```

Use lane-specific validation through `scripts/execute.py` when executing a phase.

## Optional Docker-Dependent Validation

```bash
testcontainers.enabled=true JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :admin-api:test :shop-api:test
```

This runs the MySQL-backed Spring Boot integration tests when Docker is available.

Kafka/outbox validation, once implemented:

```bash
testcontainers.enabled=true kafka.enabled=true JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :application:test :infra:test :external:test
```

The Kafka gate should cover broker publishing, retryable outbox rows, and idempotent consumer replay with Testcontainers Kafka.

Load-test evidence, once implemented:

```bash
VUS=2 DURATION=5s scripts/load-test-payment-before.sh
VUS=50 DURATION=30s scripts/load-test-payment-before.sh
scripts/load-test-payment-before.sh
k6 run load-tests/payment-spike-outbox-kafka.js
```

Use the small `VUS=2` command only as a smoke check. Record p50, p95, p99, successful payments per second, duplicate side-effect count, outbox pending count, projection lag, and publish retry count from the baseline/full load run. The Kafka slice is only convincing if correctness remains unchanged while request-path latency or downstream failure isolation improves.
