package com.example.cardservice.application.payment

data class CreateCouponOrderResponse(
    val orderId: Long,
    val paymentId: Long,
    val paymentStatus: String,
    val paymentStatusLabel: String,
    val amount: Long,
    val currency: String,
    val couponIds: List<String>,
)
