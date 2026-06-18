package com.example.cardservice.application.payment.provided

import com.example.cardservice.domain.payment.model.Payment
import org.springframework.data.repository.Repository

/**
 * Payment entity 저장을 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface PaymentRepository : Repository<Payment, Long> {
    fun save(payment: Payment): Payment
}
