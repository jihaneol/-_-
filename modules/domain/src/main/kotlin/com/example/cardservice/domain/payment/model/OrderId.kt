package com.example.cardservice.domain.payment.model

@JvmInline
value class OrderId(val value: Long) {
    init {
        require(value > 0) { "주문 ID는 0보다 커야 합니다." }
    }
}
