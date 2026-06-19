package com.example.cardservice.domain.payment.model

@JvmInline
value class IdempotencyKey(val value: String) {
    init {
        require(value.isNotBlank()) { "중복 요청 방지 키는 비어 있을 수 없습니다." }
    }
}
