package com.example.cardservice.payment.domain.model

@JvmInline
value class PaymentId(val value: String) {
    init {
        require(value.isNotBlank()) { "paymentId must not be blank" }
    }
}

