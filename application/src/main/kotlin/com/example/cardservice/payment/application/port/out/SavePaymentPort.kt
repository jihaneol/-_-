package com.example.cardservice.payment.application.port.out

import com.example.cardservice.payment.domain.model.Payment

interface SavePaymentPort {
    fun save(payment: Payment): Payment
}
