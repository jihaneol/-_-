package com.example.cardservice.application.order

import com.example.cardservice.domain.order.OrderStatus

data class OrderResponse(
    val id: Long,
    val memberId: Long,
    val status: OrderStatus,
    val totalAmount: Long,
    val currency: String,
    val paymentId: Long?,
    val lines: List<OrderItemResponse>,
)

data class OrderPageResponse(
    val items: List<OrderResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class OrderItemResponse(
    val productId: Long,
    val productName: String,
    val unitPrice: Long,
    val quantity: Long,
    val lineAmount: Long,
)
