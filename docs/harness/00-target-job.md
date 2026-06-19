# Target Job Harness

## Target

Kakao Pay style payment/card-service backend role.

## Evidence To Prove

- Kotlin and Spring Boot production-style backend development.
- DDD and hexagonal architecture around payment, order, inventory, coupon, ledger, settlement, and reconciliation flows.
- Transaction correctness under duplicate requests and concurrent access.
- Immutable history records for payment ledger and coupon history.
- MySQL schema, constraints, and indexes that support correctness and reporting.
- Behavior-style domain/application tests with MockK where outbound ports are mocked.
- Spring Boot integration tests for API and persistence behavior.
- React admin UI that exposes operational states and failure cases.

## Project Positioning

This is not a generic CRUD portfolio. The project is a compact transaction system that demonstrates money-adjacent correctness:

- authorize or pay once,
- reject or suppress duplicate side effects,
- append immutable records,
- reverse through explicit corrective workflows,
- summarize and reconcile persisted data,
- expose the important states to an operator.

## Current Scope Change

The system is expanding from a single admin-oriented runtime into separate admin and shop surfaces:

- `admin-api` and admin frontend for operator workflows.
- `shop-api` and shop frontend for customer purchase and coupon-wallet workflows.
- shared domain/application/infra modules for core business rules.

## Presentable Slice

The currently completed slice is the commerce coupon workflow:

- core transaction: order payment deducts inventory and issues coupons once per idempotency key,
- corrective workflows: full refund voids coupons; admin exchange consumes ten issued coupons for one 5,000 KRW product,
- immutable records: coupon issue, void, and exchange histories,
- reporting/reconciliation: dashboard summaries, customer coupon wallet, and admin coupon consistency report,
- UI proof: React admin operations plus React shop pages 05-12.

Full settlement ledger, Kafka/RabbitMQ, and authentication remain later-scope items.
