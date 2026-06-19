package com.example.cardservice.infra.payment.persistence

import com.example.cardservice.application.payment.CouponAccrualInput
import com.example.cardservice.application.payment.CouponAccrualResult
import com.example.cardservice.application.payment.provided.AccrueCouponPort
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

/**
 * AccrueCouponPort를 인메모리 쿠폰 발급으로 구현하는 임시 persistence adapter다.
 */
@Component
class InMemoryCouponAccrualAdapter : AccrueCouponPort {
    private val sequence = AtomicLong(0)

    override fun accrue(command: CouponAccrualInput): CouponAccrualResult {
        val couponIds = (1..command.quantity).map {
            "${command.brand.lowercase()}_coupon_${sequence.incrementAndGet()}"
        }

        return CouponAccrualResult(couponIds = couponIds)
    }
}
