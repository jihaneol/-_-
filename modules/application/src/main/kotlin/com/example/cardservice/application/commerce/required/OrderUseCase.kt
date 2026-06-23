package com.example.cardservice.application.commerce.required

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.commerce.CreateOrderInput
import com.example.cardservice.application.commerce.OrderPageResult
import com.example.cardservice.application.commerce.OrderResult

/**
 * 주문 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface OrderUseCase {
    fun createOrder(input: CreateOrderInput): OrderResult
    fun cancelOrder(orderId: Long): OrderResult
    fun deleteOrder(orderId: Long)
}

/**
 * 주문 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface OrderQueryUseCase {
    fun listOrders(pagination: Pagination): OrderPageResult
    fun getOrder(orderId: Long): OrderResult
}
