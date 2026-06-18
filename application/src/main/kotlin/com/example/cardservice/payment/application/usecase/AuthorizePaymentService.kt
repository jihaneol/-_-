package com.example.cardservice.payment.application.usecase

import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentCommand
import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentResult
import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentUseCase
import com.example.cardservice.payment.application.port.out.SavePaymentPort
import com.example.cardservice.payment.domain.model.Payment
import org.springframework.stereotype.Service

@Service
class AuthorizePaymentService(
    private val savePaymentPort: SavePaymentPort,
) : AuthorizePaymentUseCase {
    override fun authorize(command: AuthorizePaymentCommand): AuthorizePaymentResult {
        val payment = Payment.authorize(
            merchantId = command.merchantId,
            orderId = command.orderId,
            idempotencyKey = command.idempotencyKey,
            money = command.money,
        )
        val savedPayment = savePaymentPort.save(payment)

        return AuthorizePaymentResult(
            paymentId = requireNotNull(savedPayment.id) { "saved payment must have an id" },
            status = savedPayment.status,
            amount = savedPayment.money.amount,
            currency = savedPayment.money.currency,
        )
    }
}
