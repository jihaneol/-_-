package com.example.cardservice.payment.domain.model

@JvmInline
value class CustomerId(val value: String) {
    init {
        require(value.isNotBlank()) { "customerId must not be blank" }
    }
}

