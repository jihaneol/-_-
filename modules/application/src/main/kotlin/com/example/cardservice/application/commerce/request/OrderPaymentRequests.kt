package com.example.cardservice.application.commerce.request

data class PayOrderRequest(
    val idempotencyKey: String,
)
