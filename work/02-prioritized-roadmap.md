# Prioritized Roadmap

## Now

- Coupon Exchange Admin: one corrective workflow that changes ten `ISSUED` coupons to `EXCHANGED`, deducts one exchange-product inventory item, and records immutable history rows.
- Coupon exchange consistency report: compare issued, voided, and exchanged counts by member/order.
- Customer Coupon Wallet: expose customer-safe issued/exchanged/voided summary and recent histories through `/api/shop/**`.
- Figma-inspired Shop Pages 05-12: implement the full customer flow from program introduction to checkout and my page.
- Portfolio Harness Polish: align README, planning, API contracts, frontend contracts, and validation evidence.
- Shop Ecommerce UX Pass: convert the visible customer shop from design-demo tabs into a normal ecommerce storefront using the existing shop APIs.
- Coffee Kiosk Ordering UX Pass: refine the shop into a coffee-ordering flow with category-first discovery, order-step rail, drink-option controls, pickup summary, and coupon visibility.

## Next

- Paginated Query CQRS: migrate member/product/order/coupon/history collection reads to QueryDSL page results and update frontend pagination state.
- Dedicated exchange order ledger if the operator needs a separate `EXC-*` order number beyond coupon histories.
- Real product categories, drink option persistence, quick-order storage, and pickup scheduling APIs if the cafe ordering demo becomes a backend feature.
- Screenshot capture set for README after final visual approval.

## Later

- Customer-facing coupon reward redemption flow.
- Reward inventory and operator approval queue.
- Authentication and role-based access control.
- Settlement batch and reconciliation tables beyond coupon consistency reporting.
