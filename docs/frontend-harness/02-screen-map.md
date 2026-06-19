# Screen Map Harness

Detailed screen map lives in `docs/what/04-screen-map.md`.

## Admin Screens

| Screen | Purpose |
|---|---|
| Dashboard | operational summary |
| Members | member list, member coupon inspection, 10-coupon exchange approval, coupon consistency report |
| Products | product and inventory operations |
| Orders/Payments | order creation, payment, cancellation, refund |
| Coupons | coupon and history inspection, exchange audit |

## Shop Screens

| Screen | Purpose |
|---|---|
| Home | product-led storefront, loyalty summary, signup entry |
| Catalog | sale product browsing with ecommerce filter/sort affordances |
| Signup | demo member creation |
| Cart | local selected-product review before checkout |
| Coupon Wallet | stamp count, next-exchange count, and recent coupon history |
| Orders | customer order result/history |

## Admin Coupon Exchange Design

1. Operator opens Members.
2. Operator selects a member and checks issued, exchanged, and voided counts.
3. Operator selects an on-sale 5,000 KRW product.
4. Exchange approval is disabled until the member has at least ten issued coupons.
5. Approval exchanges ten coupons, deducts one inventory unit, refetches member coupon state, and refreshes the consistency report.
6. The consistency report remains an operator-only diagnostic.

## Figma Shop Pages 09-12

| Figma Page | Implemented View | Purpose |
|---|---|---|
| 09 쇼핑몰 메인 개선안 | `home` | header, hero coupon guide, signup, coupon summary, product tiles |
| 10 마이페이지 개선안 - 쿠폰 중심 | `mypage` | customer profile strip, 10-slot coupon board, next purchase card, recent activity |
| 11 상품 상세 개선안 - 친근한 구매 결정 | `detail` | product photo area, coupon earning notice, quantity, cart/buy actions, exchange guide |
| 12 결제 개선안 - 안심 결제 | `checkout` | order item review, delivery fields, payment summary, expected coupon earning |

## Figma Shop Pages 05-08

Figma MCP is currently rate-limited, so these are implemented as the preceding customer journey screens that feed into pages 09-12.

| Figma Page | Implemented View | Purpose |
|---|---|---|
| 05 | `program05` | coupon exchange program introduction and journey summary |
| 06 | `guide06` | coupon earning rules, current stamp board, and signup entry |
| 07 | `catalog07` | customer-safe product list with coupon earning preview |
| 08 | `order08` | pre-checkout order confirmation with buyer and expected coupon summary |

## Ecommerce UX Pass

Customer-visible navigation should not expose Figma page numbers. The visible flow is:

1. Home storefront.
2. Catalog/product discovery.
3. Product detail.
4. Cart/order preview.
5. Checkout.
6. My page coupon wallet.

Coupon earning remains visible as a loyalty benefit, but product discovery and checkout are the primary shopping tasks.

## Coffee Kiosk UX Pass

Reference-informed improvements:

- Starbucks app pattern: order ahead, menu discovery, and rewards entry.
- Starbucks kiosk pattern: language and accessibility controls should be visible utilities.
- MegaMGC app pattern: quick order, pickup reservation, and saved-menu ideas should appear as low-friction ordering affordances.
- Cafe kiosk pattern: category-first menu, image-led product choice, step-by-step options, smart cart, and membership/payment integration.
- Reference URLs: Starbucks app `https://www.starbucks.com/rewards/mobile-apps/`, Starbucks Korea kiosk report `https://www.yna.co.kr/view/AKR20251113131700030`, MegaMGC app `https://apps.apple.com/us/app/%EB%A9%94%EA%B0%80mgc%EC%BB%A4%ED%94%BC/id1473428031`, kiosk case study `https://topping.io/portfolio/kiosk/beauty-coffee-kiosk-platform`, kiosk UI guidance `https://brunch.co.kr/%40rladbtls003/18`.

Implemented customer flow target:

1. Customer lands on a coffee-ordering home, not a generic ecommerce hero.
2. A visible order-step rail shows menu selection, cart/options, payment, and coupon wallet.
3. Catalog uses cafe categories and a right-side order ticket summary.
4. Cart/order preview exposes local drink options such as temperature, cup, pickup, and coupon earning.
5. Checkout uses pickup/payment language instead of delivery language.

Out of scope in this loop: persisted categories, saved favorites, option persistence, real pickup schedule, translation, kiosk hardware input, and payment-provider integration.

## Leakage Rule

Shop screens must not expose product creation, inventory adjustment, full member list, dashboard summary, operational refund, or admin coupon inspection.

## Shop Figma Flow

1. Customer can inspect pages 05-08 for program, rules, catalog, and order preview.
2. Customer opens the page 09 main page.
3. Header exposes product, coupon, my page, and checkout affordances without admin navigation.
4. Hero band explains the 5,000 KRW purchase to one-stamp policy and shows a floating 10-slot coupon card.
5. Customer signs up in the same page for the demo flow.
6. Customer buys a product from the product tile grid or detail page.
7. After payment, the wallet panel refetches `shop.wallet(memberId)` and renders issued, exchanged, and voided coupon counts.
8. Recent coupon history appears as customer-safe activity copy, not an operational audit table.

## Coupon Exchange Admin Flow

1. Operator opens Members.
2. Operator selects a member.
3. Screen shows issued, exchanged, and voided coupons with history.
4. Operator selects one 5,000 KRW exchange product.
5. Operator can approve exchange only when the member has at least 10 `ISSUED` coupons.
6. After success, the screen refetches coupon list, coupon history, dashboard summary, and coupon consistency report.
7. Operator checks the consistency report to compare current coupon state with issue, void, and exchange histories by member/order.
