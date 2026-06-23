package com.example.cardservice.domain.commerce.model.coupon

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Access(AccessType.FIELD)
@Table(name = "coupon_histories")
class CouponHistory protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "coupon_id")
    var couponId: Long? = null
        protected set

    @Column(name = "member_id", nullable = false)
    var memberId: Long = 0
        protected set

    @Column(name = "order_id", nullable = false)
    var orderId: Long = 0
        protected set

    @Column(name = "payment_ref_id", nullable = false)
    var paymentId: Long = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    var type: CouponHistoryType = CouponHistoryType.ISSUED
        protected set

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    private constructor(
        couponId: Long?,
        memberId: Long,
        orderId: Long,
        paymentId: Long,
        type: CouponHistoryType,
    ) : this() {
        this.couponId = couponId
        this.memberId = memberId
        this.orderId = orderId
        this.paymentId = paymentId
        this.type = type
    }

    companion object {
        fun issued(coupon: Coupon): CouponHistory =
            CouponHistory(coupon.id, coupon.memberId, coupon.orderId, coupon.paymentId, CouponHistoryType.ISSUED)

        fun voided(coupon: Coupon): CouponHistory =
            CouponHistory(coupon.id, coupon.memberId, coupon.orderId, coupon.paymentId, CouponHistoryType.VOIDED)

        fun exchanged(coupon: Coupon): CouponHistory =
            CouponHistory(coupon.id, coupon.memberId, coupon.orderId, coupon.paymentId, CouponHistoryType.EXCHANGED)
    }
}

enum class CouponHistoryType {
    ISSUED,
    VOIDED,
    EXCHANGED,
}
