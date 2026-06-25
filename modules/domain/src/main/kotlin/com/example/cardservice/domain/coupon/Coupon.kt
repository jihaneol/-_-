package com.example.cardservice.domain.coupon

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

@Entity
@Access(AccessType.FIELD)
@Table(name = "coupons")
class Coupon protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
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
    @Column(name = "status", nullable = false, length = 30)
    var status: CouponStatus = CouponStatus.ISSUED
        protected set

    private constructor(memberId: Long, orderId: Long, paymentId: Long) : this() {
        require(memberId > 0) { "회원 ID는 0보다 커야 합니다." }
        require(orderId > 0) { "주문 ID는 0보다 커야 합니다." }
        require(paymentId > 0) { "결제 ID는 0보다 커야 합니다." }
        this.memberId = memberId
        this.orderId = orderId
        this.paymentId = paymentId
    }

    fun void() {
        require(status == CouponStatus.ISSUED) { "발급 상태 쿠폰만 무효화할 수 있습니다." }
        status = CouponStatus.VOIDED
    }

    fun exchange() {
        require(status == CouponStatus.ISSUED) { "발급 상태 쿠폰만 교환할 수 있습니다." }
        status = CouponStatus.EXCHANGED
    }

    companion object {
        fun issue(memberId: Long, orderId: Long, paymentId: Long): Coupon = Coupon(memberId, orderId, paymentId)

        fun issueCount(paidAmount: Long): Int {
            require(paidAmount > 0) { "결제 금액은 0보다 커야 합니다." }
            return (paidAmount / 5_000L).toInt()
        }
    }
}

enum class CouponStatus {
    ISSUED,
    VOIDED,
    EXCHANGED,
}
