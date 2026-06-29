package com.example.cardservice.domain.payment.model

@JvmInline
value class MerchantId(val value: Long) {
    init {
        require(value > 0) { "가맹점 ID는 0보다 커야 합니다." }
    }
}
