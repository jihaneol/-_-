package com.example.cardservice.payment.domain.model

@JvmInline
value class OrderId(val value: String) {
    init {
        require(value.isNotBlank()) { "orderId must not be blank" }
    }
}

