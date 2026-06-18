package com.example.cardservice.application.payment.required

import com.example.cardservice.application.payment.AuthorizePaymentInput
import com.example.cardservice.application.payment.AuthorizePaymentResult

/**
 * 결제 승인 요청을 application layer로 전달하는 inbound port다.
 */
interface AuthorizePaymentUseCase {
    fun authorize(input: AuthorizePaymentInput): AuthorizePaymentResult
}
