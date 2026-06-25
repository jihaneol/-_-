package com.example.cardservice.application.order

import com.example.cardservice.domain.order.OrderStatus

data class CreateOrderInput(val memberId: Long, val lines: List<CreateOrderItemInput>)
data class CreateOrderItemInput(val productId: Long, val quantity: Long)
data class OrderResult(
    val id: Long,
    val memberId: Long,
    val status: OrderStatus,
    val totalAmount: Long,
    val currency: String,
    val paymentId: Long?,
    val lines: List<OrderItemResult>,
)

data class OrderPageResult(
    val items: List<OrderResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class OrderItemResult(
    val productId: Long,
    val productName: String,
    val unitPrice: Long,
    val quantity: Long,
    val lineAmount: Long,
)
