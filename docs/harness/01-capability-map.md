# Capability Map

| Hiring capability | Project artifact | Proof |
|---|---|---|
| Kotlin + Spring Boot | `admin-api`, `shop-api`, application use cases | Gradle modules and API tests |
| DDD | Payment, order, inventory, coupon, ledger, settlement models | Domain BehaviorSpec tests |
| Hexagonal architecture | `domain`, `application`, `infra`, `external`, `batch`, API runtime modules | Dependency rules and module boundaries |
| Transaction handling | order payment, inventory deduction, coupon issuance | integration tests |
| Idempotency | payment/order duplicate request handling | duplicate request tests and unique constraints |
| Concurrency | same idempotency key and inventory race protection | concurrency test target |
| Immutable records | payment ledger and coupon histories | append-only domain tests |
| Corrective workflow | cancellation/refund/voiding coupons | domain and application tests |
| Reporting/batch | settlement and reconciliation target | batch tests and mismatch reports |
| MySQL modeling | `sql/schema` and JPA models | schema files, indexes, Testcontainers |
| Frontend operations | admin dashboard and action screens | React tests with MSW |
| Customer flow separation | shop app and `shop-api` namespace | route/API namespace tests |

## Priority

1. Core transactional correctness.
2. Duplicate side-effect prevention.
3. Immutable records and corrective workflows.
4. Settlement/reconciliation evidence.
5. Admin UI for operational proof.
6. Shop surface after admin/shop boundary split.
