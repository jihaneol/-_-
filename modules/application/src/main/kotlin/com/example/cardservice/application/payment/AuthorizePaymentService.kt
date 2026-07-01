package com.example.cardservice.application.payment

import com.example.cardservice.application.payment.required.AuthorizePaymentUseCase
import com.example.cardservice.application.payment.provided.SavePaymentPort
import com.example.cardservice.domain.payment.model.Payment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 도메인 결제 승인 규칙을 실행하고 승인된 결제 aggregate를 저장하는 application service다.
 */
@Service
class AuthorizePaymentService(
    private val savePaymentPort: SavePaymentPort,
) : AuthorizePaymentUseCase {
    @Transactional
    override fun authorize(input: AuthorizePaymentRequest): AuthorizePaymentResponse {
        // 도메인 생성
        val payment = Payment.authorize(
            merchantId = input.merchantId,
            orderId = input.orderId,
            idempotencyKey = input.idempotencyKey,
            money = input.money,
        )
        val savedPayment = savePaymentPort.save(payment)
        val savedMoney = savedPayment.money

        return AuthorizePaymentResponse(
            paymentId = requireNotNull(savedPayment.paymentId) { "저장된 결제에는 결제 ID가 있어야 합니다." },
            status = savedPayment.status,
            amount = savedMoney.amount,
            currency = savedMoney.currency,
        )
    }
}
