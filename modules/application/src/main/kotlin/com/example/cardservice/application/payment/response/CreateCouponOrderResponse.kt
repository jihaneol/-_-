package com.example.cardservice.application.payment.response

import io.swagger.v3.oas.annotations.media.Schema

data class CreateCouponOrderResponse(
    @get:Schema(description = "주문 ID", example = "order-1")
    val orderId: String,

    @get:Schema(description = "결제 ID", example = "1")
    val paymentId: String,

    @get:Schema(description = "결제 상태", example = "AUTHORIZED")
    val paymentStatus: String,

    @get:Schema(description = "결제 상태 한글 표시명", example = "승인 완료")
    val paymentStatusLabel: String,

    @get:Schema(description = "결제 금액", example = "10000")
    val amount: Long,

    @get:Schema(description = "통화", example = "KRW")
    val currency: String,

    @get:Schema(description = "적립된 쿠폰 ID 목록")
    val couponIds: List<String>,
)
