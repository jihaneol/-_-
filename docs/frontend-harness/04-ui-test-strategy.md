# UI Test Strategy

## Unit and Component Tests

Use Vitest and React Testing Library.

Test:

- Authorize payment form validation.
- Cancel payment confirmation behavior.
- Payment status badge rendering.
- Settlement summary formatting.
- Reconciliation mismatch table states.

## API Mocking

Use MSW for API responses.

Create handlers for:

- Payment list success.
- Payment authorization success.
- Duplicate idempotency error.
- Cancellation success.
- Settlement result.
- Reconciliation mismatch result.
- Server error and retry path.

## Integration-Like UI Tests

Test one full user flow with MSW:

```text
Given the payment page is open
When an operator submits a valid authorization request
Then the success result is shown
And the payment list is refreshed
```

## Optional E2E

Use Playwright only if backend and frontend are both stable.

Priority E2E flow:

- Authorize payment.
- Open detail.
- Cancel payment.
- Run settlement.
- Run reconciliation.
