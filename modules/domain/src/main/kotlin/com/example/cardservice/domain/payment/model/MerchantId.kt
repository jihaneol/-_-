package com.example.cardservice.domain.payment.model

@JvmInline
value class MerchantId(val value: String) {
    init {
        require(value.isNotBlank()) { "가맹점 ID는 비어 있을 수 없습니다." }
    }
}
