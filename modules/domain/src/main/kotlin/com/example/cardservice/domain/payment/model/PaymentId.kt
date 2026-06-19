package com.example.cardservice.domain.payment.model

@JvmInline
value class PaymentId(val value: Long) {
    init {
        require(value > 0) { "결제 ID는 0보다 커야 합니다." }
    }
}
