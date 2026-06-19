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

## Validation Commands

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test
npm --prefix frontend test -- --run
npm --prefix frontend run build
```

Use lane-specific validation through `scripts/execute.py` when executing a phase.
