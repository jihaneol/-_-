package com.example.cardservice.application.payment

data class CreateCouponOrderRequest(
    val customerId: Long,
    val orderId: Long,
    val idempotencyKey: String,
    val quantity: Int,
)
