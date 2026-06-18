package com.example.cardservice.payment.application.port.`in`

import com.example.cardservice.payment.domain.model.IdempotencyKey
import com.example.cardservice.payment.domain.model.MerchantId
import com.example.cardservice.payment.domain.model.Money
import com.example.cardservice.payment.domain.model.OrderId
import com.example.cardservice.payment.domain.model.PaymentId
import com.example.cardservice.payment.domain.model.PaymentStatus

interface AuthorizePaymentUseCase {
    fun authorize(command: AuthorizePaymentCommand): AuthorizePaymentResult
}

data class AuthorizePaymentCommand(
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
