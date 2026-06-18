package com.example.cardservice.application.payment.provided

import com.example.cardservice.application.payment.ExternalPaymentApproval
import com.example.cardservice.application.payment.ExternalPaymentRequest

/**
 * 외부 결제 승인 시스템에 결제 승인을 요청하기 위해 application service가 호출하는 outbound port다.
 */
interface ExternalPaymentPort {
    fun approve(request: ExternalPaymentRequest): ExternalPaymentApproval
}
