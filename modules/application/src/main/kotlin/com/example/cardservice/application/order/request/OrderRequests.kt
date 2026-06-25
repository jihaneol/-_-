package com.example.cardservice.application.order.request

data class OrderCreateRequest(
    val memberId: Long,
    val lines: List<OrderItemRequest>,
)

data class OrderItemRequest(
    val productId: Long,
    val quantity: Long,
)
