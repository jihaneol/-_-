package com.example.cardservice.payment.domain.model

data class Money(
    val amount: Long,
    val currency: String,
) {
    init {
        require(amount > 0) { "amount must be positive" }
        require(currency.isNotBlank()) { "currency must not be blank" }
    }
}

