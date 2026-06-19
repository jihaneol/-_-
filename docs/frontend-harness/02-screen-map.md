# Screen Map Harness

Detailed screen map lives in `docs/what/04-screen-map.md`.

## Admin Screens

| Screen | Purpose |
|---|---|
| Dashboard | operational summary |
| Members | member list, member coupon inspection, issued coupon exchange, coupon consistency report |
| Products | product and inventory operations |
| Orders/Payments | order creation, payment, cancellation, refund |
| Coupons | coupon and history inspection, exchange audit |

## Shop Screens

| Screen | Purpose |
|---|---|
| Catalog | sale product browsing |
| Signup | demo member creation |
| Cart | customer order review and payment |
| Coupon Wallet | stamp count and coupon history |
| Orders | customer order result/history |

## Leakage Rule

Shop screens must not expose product creation, inventory adjustment, full member list, dashboard summary, operational refund, or admin coupon inspection.

## Coupon Exchange Admin Flow

1. Operator opens Members.
2. Operator selects a member.
3. Screen shows issued, exchanged, and voided coupons with history.
4. Operator selects one 5,000 KRW exchange product.
5. Operator can approve exchange only when the member has at least 10 `ISSUED` coupons.
6. After success, the screen refetches coupon list, coupon history, dashboard summary, and coupon consistency report.
7. Operator checks the consistency report to compare current coupon state with issue, void, and exchange histories by member/order.
