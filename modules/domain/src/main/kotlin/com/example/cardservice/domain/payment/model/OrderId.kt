package com.example.cardservice.domain.payment.model

@JvmInline
value class OrderId(val value: String) {
    init {
        require(value.isNotBlank()) { "주문 ID는 비어 있을 수 없습니다." }
    }
}
