# Screen Map

## Routes

| Route | Purpose |
|---|---|
| `/` | Dashboard summary |
| `/payments` | Payment list, filters, authorization entry |
| `/payments/:paymentId` | Payment detail, status, ledger, cancellation |
| `/settlements` | Run and inspect daily settlement |
| `/reconciliation` | Run and inspect reconciliation mismatches |

## Dashboard

- Today's authorized amount.
- Cancelled amount.
- Settlement status.
- Reconciliation mismatch count.
- Recent payment events.

## Payments

- Merchant/date/status filters.
- Payment table.
- Authorize payment form.
- Payment detail panel.
- Cancel payment action with confirmation.

## Settlements

- Date picker.
- Run settlement action.
- Merchant settlement summary table.
- Empty and already-run states.

## Reconciliation

- Date picker.
- Run reconciliation action.
- Mismatch table.
- Mismatch type filters: missing, duplicated, amount mismatch.

## Required UI States

- Loading skeleton or compact spinner.
- Empty state with next action.
- API validation error.
- Server error with retry.
- Success feedback after mutation.
- Disabled submit while request is pending.
