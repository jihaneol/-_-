# Harness Templates

Use these templates when starting a new portfolio project.

## Steps

1. Choose the target role.
2. Extract required capabilities from the job post.
3. Fill the placeholders in `backend-template.md`.
4. If a frontend is included, fill the placeholders in `frontend-template.md`.
5. Copy the generated backend files into `harness/backend`.
6. Copy the generated frontend files into `harness/frontend`.
7. Keep the generated docs as the project-specific instance.

## Placeholder Translation

| Payment term | Generic term | Other examples |
|---|---|---|
| authorize payment | `{core_action}` | reserve stock, confirm order, create booking |
| cancel payment | `{corrective_action}` | release reservation, refund order, cancel booking |
| ledger | `{history_record}` | audit event, order event, stock movement |
| settlement | `{batch_report}` | daily report, closing summary, inventory snapshot |
| reconciliation | `{consistency_check}` | audit check, stock consistency check, order consistency check |
| payment event | `{async_event}` | OrderConfirmed, StockReserved, BookingCancelled |

## Current Instance

This repository currently uses the templates for a card/payment service project.
