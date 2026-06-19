# Capability Map

| Hiring capability | Project artifact | Proof |
|---|---|---|
| Kotlin + Spring Boot | `admin-api`, `shop-api`, application use cases | Gradle modules and API tests |
| DDD | Payment, order, inventory, coupon, coupon history models | Domain BehaviorSpec tests |
| Hexagonal architecture | `domain`, `application`, `infra`, `external`, `batch`, API runtime modules | Dependency rules and module boundaries |
| Transaction handling | order payment, inventory deduction, coupon issuance | integration tests |
| Idempotency | payment/order duplicate request handling | duplicate request tests and unique constraints |
| Concurrency | same idempotency key and inventory race protection | concurrency test target |
| Immutable records | payment ledger and coupon histories | append-only domain tests |
| Corrective workflow | cancellation/refund/voiding coupons | domain and application tests |
| Reporting/reconciliation | dashboard summary, coupon wallet, coupon consistency report | API tests, frontend state, consistency UI |
| MySQL modeling | `sql/schema` and JPA models | schema files, indexes, Testcontainers |
| Frontend operations | admin dashboard and action screens | React tests with MSW |
| Customer flow separation | shop app and `shop-api` namespace | route/API namespace tests |
| Design implementation | Figma-inspired admin and shop pages | CSS implementation, RTL tests, desktop/mobile browser overflow checks |

## Current Proof Mapping

| Harness requirement | Current artifact |
|---|---|
| Core transactional API | `POST /api/admin/orders/{orderId}/pay`, `POST /api/shop/orders/{orderId}/pay` |
| Corrective API/workflow | `POST /api/admin/orders/{orderId}/refund`, `POST /api/admin/members/{memberId}/coupon-exchanges` |
| Immutable history | `CouponHistory.issued`, `CouponHistory.voided`, `CouponHistory.exchanged` |
| Reporting output | dashboard summary, shop coupon wallet |
| Consistency output | `GET /api/admin/coupon-consistency` |
| Concurrency/idempotency proof | idempotency key lookup/unique constraint, pessimistic write lock adapter |
| Frontend proof | admin pages plus shop views `program05` through `checkout` |

## Priority

1. Core transactional correctness.
2. Duplicate side-effect prevention.
3. Immutable records and corrective workflows.
4. Settlement/reconciliation evidence.
5. Admin UI for operational proof.
6. Shop surface after admin/shop boundary split.
