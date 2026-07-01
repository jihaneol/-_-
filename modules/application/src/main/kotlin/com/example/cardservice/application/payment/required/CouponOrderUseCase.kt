package com.example.cardservice.application.payment.required

import com.example.cardservice.application.payment.CreateCouponOrderRequest
import com.example.cardservice.application.payment.CreateCouponOrderResponse

/**
 * 쿠폰 주문 생성 요청을 application layer로 전달하는 inbound port다.
 */
interface CouponOrderUseCase {
    fun create(input: CreateCouponOrderRequest): CreateCouponOrderResponse
}
