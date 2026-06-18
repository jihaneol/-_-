package com.example.cardservice.application.payment

import com.example.cardservice.domain.payment.model.CustomerId
import com.example.cardservice.domain.payment.model.OrderId

data class CouponAccrualInput(
    val customerId: CustomerId,
    val orderId: OrderId,
    val brand: String,
    val quantity: Int,
)

data class CouponAccrualResult(
    val couponIds: List<String>,
)
