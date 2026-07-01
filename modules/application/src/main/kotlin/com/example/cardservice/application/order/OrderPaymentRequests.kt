package com.example.cardservice.application.order

data class PayOrderRequest(val idempotencyKey: String) {
    var orderId: Long = 0L
}
