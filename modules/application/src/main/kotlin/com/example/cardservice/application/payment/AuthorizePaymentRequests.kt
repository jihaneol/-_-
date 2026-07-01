package com.example.cardservice.application.payment

import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.MerchantId
import com.example.cardservice.domain.payment.model.Money
import com.example.cardservice.domain.payment.model.OrderId

data class AuthorizePaymentRequest(
    val merchantId: MerchantId,
    val orderId: OrderId,
    val idempotencyKey: IdempotencyKey,
    val money: Money,
)
