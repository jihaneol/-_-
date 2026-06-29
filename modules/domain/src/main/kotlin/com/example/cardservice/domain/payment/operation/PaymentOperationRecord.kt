package com.example.cardservice.domain.payment.operation

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
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Access(AccessType.FIELD)
@Table(
    name = "payment_operation_records",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_payment_operation_records_operation_order",
            columnNames = ["operation_type", "order_id"],
        ),
    ],
)
class PaymentOperationRecord protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0L
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 40)
    var operationType: PaymentOperationType = PaymentOperationType.PAYMENT_AUTHORIZED
        protected set

    @Column(name = "order_id", nullable = false)
    var orderId: Long = 0
        protected set

    @Column(name = "payment_ref_id", nullable = false)
    var paymentId: Long = 0
        protected set

    @Column(name = "member_id", nullable = false)
    var memberId: Long = 0
        protected set

    @Column(name = "amount", nullable = false)
    var amount: Long = 0
        protected set

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String = "KRW"
        protected set

    @Column(name = "issued_coupon_count", nullable = false)
    var issuedCouponCount: Int = 0
        protected set

    @Column(name = "voided_coupon_count", nullable = false)
    var voidedCouponCount: Int = 0
        protected set

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: LocalDateTime = LocalDateTime.now()
        protected set

    private constructor(
        operationType: PaymentOperationType,
        orderId: Long,
        paymentId: Long,
        memberId: Long,
        amount: Long,
        currency: String,
        issuedCouponCount: Int,
        voidedCouponCount: Int,
        occurredAt: LocalDateTime,
    ) : this() {
        require(orderId > 0) { "주문 ID는 0보다 커야 합니다." }
        require(paymentId > 0) { "결제 ID는 0보다 커야 합니다." }
        require(memberId > 0) { "회원 ID는 0보다 커야 합니다." }
        require(amount > 0) { "금액은 0보다 커야 합니다." }
        require(currency.isNotBlank()) { "통화는 비어 있을 수 없습니다." }
        require(issuedCouponCount >= 0) { "발급 쿠폰 수는 음수일 수 없습니다." }
        require(voidedCouponCount >= 0) { "무효화 쿠폰 수는 음수일 수 없습니다." }
        this.operationType = operationType
        this.orderId = orderId
        this.paymentId = paymentId
        this.memberId = memberId
        this.amount = amount
        this.currency = currency
        this.issuedCouponCount = issuedCouponCount
        this.voidedCouponCount = voidedCouponCount
        this.occurredAt = occurredAt
    }

    companion object {
        fun paymentAuthorized(
            orderId: Long,
            paymentId: Long,
            memberId: Long,
            amount: Long,
            currency: String,
            issuedCouponCount: Int,
            occurredAt: LocalDateTime = LocalDateTime.now(),
        ): PaymentOperationRecord =
            PaymentOperationRecord(
                operationType = PaymentOperationType.PAYMENT_AUTHORIZED,
                orderId = orderId,
                paymentId = paymentId,
                memberId = memberId,
                amount = amount,
                currency = currency,
                issuedCouponCount = issuedCouponCount,
                voidedCouponCount = 0,
                occurredAt = occurredAt,
            )

        fun paymentRefunded(
            orderId: Long,
            paymentId: Long,
            memberId: Long,
            amount: Long,
            currency: String,
            voidedCouponCount: Int,
            occurredAt: LocalDateTime = LocalDateTime.now(),
        ): PaymentOperationRecord =
            PaymentOperationRecord(
                operationType = PaymentOperationType.PAYMENT_REFUNDED,
                orderId = orderId,
                paymentId = paymentId,
                memberId = memberId,
                amount = amount,
                currency = currency,
                issuedCouponCount = 0,
                voidedCouponCount = voidedCouponCount,
                occurredAt = occurredAt,
            )
    }
}

enum class PaymentOperationType {
    PAYMENT_AUTHORIZED,
    PAYMENT_REFUNDED,
}
