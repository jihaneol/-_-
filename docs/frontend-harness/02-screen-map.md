# Screen Map Harness

Detailed screen map lives in `docs/what/04-screen-map.md`.

## Admin Screens

| Screen | Purpose |
|---|---|
| Dashboard | operational summary |
| Members | member list, member coupon inspection |
| Products | product and inventory operations |
| Orders/Payments | order creation, payment, cancellation, refund |
| Coupons | coupon and history inspection |

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
