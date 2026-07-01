package com.example.cardservice.application.order

data class CreateOrderRequest(val memberId: Long, val lines: List<CreateOrderItemRequest>)
data class CreateOrderItemRequest(val productId: Long, val quantity: Long)
