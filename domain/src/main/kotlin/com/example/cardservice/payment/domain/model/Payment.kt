package com.example.cardservice.payment.domain.model

data class Payment(
    val id: PaymentId?,
    val merchantId: MerchantId,
    val orderId: OrderId,
    val idempotencyKey: IdempotencyKey,
    val money: Money,
    val status: PaymentStatus,
) {
    companion object {
        fun authorize(
            merchantId: MerchantId,
            orderId: OrderId,
            idempotencyKey: IdempotencyKey,
            money: Money,
        ): Payment =
            Payment(
                id = null,
                merchantId = merchantId,
                orderId = orderId,
                idempotencyKey = idempotencyKey,
                money = money,
                status = PaymentStatus.AUTHORIZED,
            )
    }
}

