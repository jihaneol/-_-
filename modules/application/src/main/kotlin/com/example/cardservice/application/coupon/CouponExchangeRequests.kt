package com.example.cardservice.application.coupon

data class ApproveCouponExchangeRequest(val productId: Long) {
    var memberId: Long = 0L
}
