package com.example.cardservice.domain.payment.model

data class Money(
    val amount: Long,
    val currency: String,
) {
    init {
        require(amount > 0) { "금액은 0보다 커야 합니다." }
        require(currency.isNotBlank()) { "통화는 비어 있을 수 없습니다." }
    }
}
