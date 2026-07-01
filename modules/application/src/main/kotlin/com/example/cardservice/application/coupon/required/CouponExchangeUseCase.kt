package com.example.cardservice.application.coupon.required

import com.example.cardservice.application.coupon.ApproveCouponExchangeRequest
import com.example.cardservice.application.coupon.ApproveCouponExchangeResponse
import com.example.cardservice.application.coupon.CouponExchangeResponse

/**
 * 쿠폰 교환 상태 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface CouponExchangeUseCase {
    fun exchangeCoupon(couponId: Long): CouponExchangeResponse
    fun approveCouponExchange(request: ApproveCouponExchangeRequest): ApproveCouponExchangeResponse
}
