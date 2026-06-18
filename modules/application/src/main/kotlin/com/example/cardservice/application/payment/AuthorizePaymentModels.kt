package com.example.cardservice.application.payment

import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.MerchantId
import com.example.cardservice.domain.payment.model.Money
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.PaymentId
import com.example.cardservice.domain.payment.model.PaymentStatus

data class AuthorizePaymentInput(
    val merchantId: MerchantId,
    val orderId: OrderId,
    val idempotencyKey: IdempotencyKey,
    val money: Money,
)

data class AuthorizePaymentResult(
    val paymentId: PaymentId,
    val status: PaymentStatus,
    val amount: Long,
    val currency: String,
)
