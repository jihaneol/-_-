package com.example.cardservice.application.payment

import com.example.cardservice.domain.payment.model.PaymentId
import com.example.cardservice.domain.payment.model.PaymentStatus

data class AuthorizePaymentResponse(
    val paymentId: PaymentId,
    val status: PaymentStatus,
    val amount: Long,
    val currency: String,
)
