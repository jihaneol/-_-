package com.example.cardservice.application.order.request

data class PayOrderRequest(
    val idempotencyKey: String,
)
