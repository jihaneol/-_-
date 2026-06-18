package com.example.cardservice.payment.application.port.out

import com.example.cardservice.payment.domain.model.CustomerId
import com.example.cardservice.payment.domain.model.OrderId

interface AccrueCouponPort {
    fun accrue(command: CouponAccrualCommand): CouponAccrualResult
}

data class CouponAccrualCommand(
    val customerId: CustomerId,
    val orderId: OrderId,
    val brand: String,
    val quantity: Int,
)

data class CouponAccrualResult(
    val couponIds: List<String>,
)
