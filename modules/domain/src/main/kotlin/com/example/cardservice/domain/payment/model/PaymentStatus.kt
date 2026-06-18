package com.example.cardservice.domain.payment.model

enum class PaymentStatus(
    val label: String,
) {
    AUTHORIZED("승인 완료"),
    CANCELLED("취소 완료"),
    SETTLED("정산 완료"),
    FAILED("실패"),
}
