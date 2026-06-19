package com.example.cardservice.application.commerce.request

data class OrderCreateRequest(
    val memberId: Long,
    val lines: List<OrderLineRequest>,
)

data class OrderLineRequest(
    val productId: Long,
    val quantity: Long,
)
