# card-service

Kotlin Spring Boot backend and React frontend for a transaction-heavy commerce/payment portfolio project.

The current presentable slice proves:

- paid order execution with idempotency protection,
- inventory deduction and full refund reversal,
- immutable coupon issue, void, and exchange history,
- 10-stamp coupon exchange with pessimistic locks,
- admin consistency reporting for coupon state versus history,
- separated admin and shop API/frontend boundaries.

## Run Locally

One-command local stack:

```bash
scripts/local-stack.sh
```

Open:

```text
Admin frontend: http://127.0.0.1:5174/
Shop frontend:  http://127.0.0.1:5174/shop.html
Admin API:      http://127.0.0.1:8082/actuator/health
Shop API:       http://127.0.0.1:8081/actuator/health
```

Manual local startup:

```bash
docker compose up -d mysql
./gradlew :admin-api:bootRun --args='--server.port=8082'
./gradlew :shop-api:bootRun --args='--server.port=8081'
npm --prefix frontend run dev -- --host 127.0.0.1 --port 5174
```

## Validate

Backend:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test
```

Frontend:

```bash
npm --prefix frontend test -- --run
npm --prefix frontend run build
```

Harness validation:

```bash
bash scripts/hooks/validate_backend.sh
bash scripts/hooks/validate_frontend.sh
```

Latest frontend proof, recorded on 2026-06-20:

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `bash scripts/hooks/validate_impeccable.sh`
- Browser checks for shop desktop/mobile and admin desktop/mobile: no horizontal page overflow, no shop admin leakage, no visible Figma planning tabs.

## Core Workflows

Admin workflow:

1. Create or inspect members, products, inventory, and orders.
2. Pay an order with an idempotency key.
3. Refund paid orders and inspect voided coupons.
4. Exchange ten issued coupons for one 5,000 KRW product.
5. Inspect coupon consistency by member and order.

Shop workflow:

1. Create a demo member.
2. Browse products and review order confirmation.
3. Pay an order through `/api/shop/**`.
4. Inspect the customer-safe coupon wallet and recent history.

## Hard Parts Proven

| Concern | Implementation | Proof |
|---|---|---|
| Duplicate payment side effects | idempotency key lookup plus unique constraint fallback | `OrderPaymentFacadeBehaviorSpec`, `CommerceFlowIntegrationTest` |
| Concurrent state changes | pessimistic locks for orders, inventory, coupons, and exchange coupon selection | `JpaCommerceLockAdapter`, exchange service tests |
| Immutable history | coupon issue, void, and exchange histories are appended instead of overwritten | domain/application tests and consistency report |
| Corrective workflow | full refund and admin coupon exchange | admin API tests and frontend MSW flow |
| Boundary separation | `/api/admin/**` and `/api/shop/**` runtimes plus separate frontends | shop boundary tests and frontend navigation tests |

## Planning And Evidence

- Backend harness: `docs/harness/`
- Frontend harness: `docs/frontend-harness/`
- Active work and change log: `work/`

## Parallel Codex Workflow

Backend and frontend are intentionally split into separate workflow lanes:

```text
workflow/backend/phases/
workflow/frontend/phases/
```

Use separate Codex threads or worktrees for true parallel work:

```bash
scripts/backend status
scripts/frontend status
```

The wrappers are shorthand for:

```bash
python3 scripts/execute.py --lane backend ...
python3 scripts/execute.py --lane frontend ...
```

Shared API expectations live in:

```text
docs/how/05-api-state-contract.md
```

Backend work should not edit `frontend/**`. Frontend work should not edit `modules/**`.
