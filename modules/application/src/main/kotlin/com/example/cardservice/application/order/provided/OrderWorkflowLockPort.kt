package com.example.cardservice.application.order.provided

import com.example.cardservice.domain.coupon.Coupon
import com.example.cardservice.domain.inventory.Inventory
import com.example.cardservice.domain.order.Order

/**
 * 결제/환불 흐름에서 주문과 재고를 쓰기 잠금으로 조회하기 위해 application service가 호출하는 outbound port다.
 */
interface OrderWorkflowLockPort {
    fun loadOrderForUpdate(orderId: Long): Order?
    fun loadInventoryForUpdate(productId: Long): Inventory?
    fun loadCouponForUpdate(couponId: Long): Coupon?
    fun loadIssuedCouponsForExchange(memberId: Long, limit: Int): List<Coupon>
}
