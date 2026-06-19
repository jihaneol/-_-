package com.example.cardservice.application.payment.provided

import com.example.cardservice.domain.payment.model.Payment

/**
 * 승인된 결제 aggregate를 저장하기 위해 application service가 호출하는 outbound port다.
 */
interface SavePaymentPort {
    fun save(payment: Payment): Payment
}
