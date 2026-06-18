package com.example.cardservice.infra.payment.persistence

import com.example.cardservice.application.payment.provided.PaymentRepository
import com.example.cardservice.application.payment.provided.SavePaymentPort
import com.example.cardservice.domain.payment.model.Payment
import org.springframework.stereotype.Component

/**
 * SavePaymentPort를 Spring Data JPA repository로 구현하는 persistence adapter다.
 */
@Component
class JpaPaymentAdapter(
    private val paymentRepository: PaymentRepository,
) : SavePaymentPort {
    override fun save(payment: Payment): Payment =
        paymentRepository.save(payment)
}
