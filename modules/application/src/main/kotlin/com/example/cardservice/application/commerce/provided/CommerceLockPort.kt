package com.example.cardservice.application.commerce.provided

import com.example.cardservice.domain.commerce.model.coupon.Coupon
import com.example.cardservice.domain.commerce.model.inventory.Inventory
import com.example.cardservice.domain.commerce.model.order.CommerceOrder

/**
 * 결제/환불 흐름에서 주문과 재고를 쓰기 잠금으로 조회하기 위해 application service가 호출하는 outbound port다.
 */
interface CommerceLockPort {
    fun loadOrderForUpdate(orderId: Long): CommerceOrder?
    fun loadInventoryForUpdate(productId: Long): Inventory?
    fun loadCouponForUpdate(couponId: Long): Coupon?
    fun loadIssuedCouponsForExchange(memberId: Long, limit: Int): List<Coupon>
}
