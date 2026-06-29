package com.example.cardservice.application.order.provided

/**
 * 주문 상태 전이를 조건부 update로 수행해 긴 pessimistic lock 조회를 피하는 outbound port다.
 */
interface OrderStatusMutationPort {
    fun markPaidIfCreated(orderId: Long, paymentId: Long): Boolean
}
