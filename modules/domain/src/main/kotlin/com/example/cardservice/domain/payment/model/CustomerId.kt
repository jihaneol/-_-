package com.example.cardservice.domain.payment.model

@JvmInline
value class CustomerId(val value: String) {
    init {
        require(value.isNotBlank()) { "고객 ID는 비어 있을 수 없습니다." }
    }
}
