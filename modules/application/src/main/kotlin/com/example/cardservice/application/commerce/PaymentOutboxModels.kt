package com.example.cardservice.application.commerce

data class PaymentOperationalEventPayload(
    val eventKey: String,
    val eventType: PaymentOperationalEventType,
    val orderId: Long,
    val paymentId: Long,
    val memberId: Long,
    val amount: Long,
    val currency: String,
    val issuedCouponCount: Int,
    val voidedCouponCount: Int,
)

enum class PaymentOperationalEventType {
    PAYMENT_AUTHORIZED,
    PAYMENT_REFUNDED,
}
