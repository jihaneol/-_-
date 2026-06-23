# Prioritized Roadmap

Only shaped feature candidates should appear here.

## Now

| Feature | Why now | Development basis |
|---|---|---|
| Portfolio proof cleanup | Keep workflow state, docs, and Obsidian records aligned with completed admin/shop coupon MVP | `workflow/backend/archive/2026-06-20/` |

## Next

| Feature | Why next | Depends on |
|---|---|---|
| Settlement/reconciliation slice | Add the next transaction-heavy backend proof after coupon exchange MVP is presentable | clean backend workflow state |
| Authentication and authorization | Protect admin/shop surfaces after operational flows are stable | admin/shop runtime split |

## Later

| Feature | Reason to defer |
|---|---|
| Dedicated payment ledger domain and append port | Deferred because the current MVP proves immutable history with coupon issue, void, and exchange histories |
| Kafka/RabbitMQ outbox | Add after core payment flow and tests are stable |
| Load testing | Add after API behavior is stable |
| Partial refund | Deferred because refund allocation and coupon reversal rules complicate MVP |
