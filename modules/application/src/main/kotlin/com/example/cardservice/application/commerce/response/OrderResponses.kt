package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.OrderLineResult
import com.example.cardservice.application.commerce.OrderResult
import com.example.cardservice.domain.commerce.model.OrderStatus

data class OrderResponse(
    val id: Long,
    val memberId: Long,
    val status: OrderStatus,
    val totalAmount: Long,
    val currency: String,
    val paymentId: Long?,
    val lines: List<OrderLineResponse>,
)

data class OrderLineResponse(
    val productId: Long,
    val productName: String,
    val unitPrice: Long,
    val quantity: Long,
    val lineAmount: Long,
)

fun OrderResult.toResponse(): OrderResponse =
    OrderResponse(id, memberId, status, totalAmount, currency, paymentId, lines.map { it.toResponse() })

fun OrderLineResult.toResponse(): OrderLineResponse =
    OrderLineResponse(productId, productName, unitPrice, quantity, lineAmount)
