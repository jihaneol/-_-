package com.example.cardservice.domain.payment.model

@JvmInline
value class CustomerId(val value: Long) {
    init {
        require(value > 0) { "고객 ID는 0보다 커야 합니다." }
    }
}
