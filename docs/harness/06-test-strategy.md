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
