package com.example.cardservice.payment.adapter.out.persistence

import com.example.cardservice.payment.application.port.out.SavePaymentPort
import com.example.cardservice.payment.domain.model.Payment
import com.example.cardservice.payment.domain.model.PaymentId
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemoryPaymentPersistenceAdapter : SavePaymentPort {
    private val sequence = AtomicLong(0)
    private val payments = ConcurrentHashMap<String, Payment>()

    override fun save(payment: Payment): Payment {
        val paymentWithId = payment.copy(
            id = payment.id ?: PaymentId("pay_${sequence.incrementAndGet()}"),
        )
        payments[paymentWithId.id!!.value] = paymentWithId
        return paymentWithId
    }
}
