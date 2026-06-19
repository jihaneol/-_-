package com.example.cardservice.application.commerce.required

import com.example.cardservice.application.commerce.ApproveCouponExchangeInput
import com.example.cardservice.application.commerce.ApproveCouponExchangeResult
import com.example.cardservice.application.commerce.CouponExchangeResult

/**
 * 쿠폰 교환 상태 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface CouponExchangeUseCase {
    fun exchangeCoupon(couponId: Long): CouponExchangeResult
    fun approveCouponExchange(memberId: Long, input: ApproveCouponExchangeInput): ApproveCouponExchangeResult
}
