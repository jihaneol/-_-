package com.example.cardservice.application.order.required

import com.example.cardservice.application.order.PayOrderRequest
import com.example.cardservice.application.order.PayOrderResponse
import com.example.cardservice.application.order.RefundOrderResponse

/**
 * 주문 결제와 전체 환불 요청을 application layer로 전달하는 inbound port다.
 */
interface OrderPaymentUseCase {
    fun payOrder(request: PayOrderRequest): PayOrderResponse
    fun refundOrder(orderId: Long): RefundOrderResponse
}
