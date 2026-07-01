package com.example.cardservice.application.order.required

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.order.CreateOrderRequest
import com.example.cardservice.application.order.OrderPageResponse
import com.example.cardservice.application.order.OrderResponse

/**
 * 주문 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface OrderUseCase {
    fun createOrder(input: CreateOrderRequest): OrderResponse
    fun cancelOrder(orderId: Long): OrderResponse
    fun deleteOrder(orderId: Long)
}

/**
 * 주문 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface OrderQueryUseCase {
    fun listOrders(pagination: Pagination): OrderPageResponse
    fun getOrder(orderId: Long): OrderResponse
}
