package com.example.cardservice.application.payment.request

import io.swagger.v3.oas.annotations.media.Schema

data class CreateCouponOrderRequest(
    @get:Schema(description = "고객 ID", example = "customer-1")
    val customerId: String,

    @get:Schema(description = "주문 ID", example = "order-1")
    val orderId: String,

    @get:Schema(description = "중복 요청 방지 키", example = "idem-1")
    val idempotencyKey: String,

    @get:Schema(description = "쿠폰 수량", example = "2", minimum = "1")
    val quantity: Int,
)
