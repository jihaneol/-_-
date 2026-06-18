package com.example.cardservice.payment.domain.model

data class Payment(
    val id: PaymentId?,
    val merchantId: MerchantId,
    val orderId: OrderId,
    val idempotencyKey: IdempotencyKey,
    val money: Money,
    val status: PaymentStatus,
)
