package com.example.cardservice.application.payment.provided

import com.example.cardservice.application.payment.CouponAccrualRequest
import com.example.cardservice.application.payment.CouponAccrualResponse

/**
 * 결제 완료 후 쿠폰 적립을 요청하기 위해 application service가 호출하는 outbound port다.
 */
interface AccrueCouponPort {
    fun accrue(input: CouponAccrualRequest): CouponAccrualResponse
}
