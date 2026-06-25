package com.example.cardservice.application.order.required

import com.example.cardservice.application.order.PayOrderInput
import com.example.cardservice.application.order.PayOrderResult
import com.example.cardservice.application.order.RefundOrderResult

/**
 * 주문 결제와 전체 환불 요청을 application layer로 전달하는 inbound port다.
 */
interface OrderPaymentUseCase {
    fun payOrder(orderId: Long, input: PayOrderInput): PayOrderResult
    fun refundOrder(orderId: Long): RefundOrderResult
}
