package com.example.cardservice.payment.domain.model

@JvmInline
value class IdempotencyKey(val value: String) {
    init {
        require(value.isNotBlank()) { "idempotencyKey must not be blank" }
    }
}

