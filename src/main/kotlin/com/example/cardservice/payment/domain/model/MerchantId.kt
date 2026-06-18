package com.example.cardservice.payment.domain.model

@JvmInline
value class MerchantId(val value: String) {
    init {
        require(value.isNotBlank()) { "merchantId must not be blank" }
    }
}
