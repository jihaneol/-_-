package com.example.cardservice.payment.adapter.out.persistence

import com.example.cardservice.payment.application.port.out.AccrueCouponPort
import com.example.cardservice.payment.application.port.out.CouponAccrualCommand
import com.example.cardservice.payment.application.port.out.CouponAccrualResult
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemoryCouponAccrualAdapter : AccrueCouponPort {
    private val sequence = AtomicLong(0)

    override fun accrue(command: CouponAccrualCommand): CouponAccrualResult {
        val couponIds = (1..command.quantity).map {
            "${command.brand.lowercase()}_coupon_${sequence.incrementAndGet()}"
        }

        return CouponAccrualResult(couponIds = couponIds)
    }
}
