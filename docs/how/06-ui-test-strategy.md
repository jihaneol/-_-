# UI Test Strategy

## Unit and Component Tests

Use Vitest and React Testing Library.

Test:

- Order payment form validation.
- Refund action behavior.
- Order status badge rendering.
- Coupon stamp status rendering.
- Coupon history table states.

## API Mocking

Use MSW for API responses.

Create handlers for:

- Member/product/order list success.
- Order creation success.
- Order payment success.
- Duplicate idempotency error.
- Full refund success.
- Coupon and coupon history success.
- Server error and retry path.

## Integration-Like UI Tests

Test one full user flow with MSW:

```text
Given the commerce dashboard is open
When an operator creates an order and pays it
Then the issued coupon count is shown
And the order list is refreshed
```

## Optional E2E

Use Playwright only if backend and frontend are both stable.

Priority E2E flow:

- Create member/product/inventory.
- Create order.
- Pay order.
- Inspect issued coupons.
- Refund order.
