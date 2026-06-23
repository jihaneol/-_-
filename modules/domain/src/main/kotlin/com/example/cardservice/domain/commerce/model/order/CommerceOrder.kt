package com.example.cardservice.domain.commerce.model.order

import com.example.cardservice.domain.payment.model.Money
import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Access(AccessType.FIELD)
@Table(name = "commerce_orders")
class CommerceOrder protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "member_id", nullable = false)
    var memberId: Long = 0
        protected set

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Long = 0
        protected set

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String = "KRW"
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: OrderStatus = OrderStatus.CREATED
        protected set

    @Column(name = "payment_ref_id")
    var paymentId: Long? = null
        protected set

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
        protected set

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private val mutableLines: MutableList<OrderLine> = mutableListOf()

    val lines: List<OrderLine>
        get() = mutableLines.toList()

    private constructor(memberId: Long, lines: List<OrderLine>) : this() {
        require(memberId > 0) { "회원 ID는 0보다 커야 합니다." }
        require(lines.isNotEmpty()) { "주문 상품은 1개 이상이어야 합니다." }
        this.memberId = memberId
        this.mutableLines.addAll(lines)
        this.totalAmount = lines.sumOf { it.lineAmount }
        this.currency = "KRW"
    }

    fun pay(paymentId: Long) {
        require(status == OrderStatus.CREATED) { "결제 가능한 주문 상태가 아닙니다." }
        require(paymentId > 0) { "결제 ID는 0보다 커야 합니다." }
        this.paymentId = paymentId
        this.status = OrderStatus.PAID
    }

    fun cancel() {
        require(status == OrderStatus.CREATED) { "결제 완료된 주문은 주문 취소할 수 없습니다." }
        this.status = OrderStatus.CANCELLED
    }

    fun refund() {
        require(status == OrderStatus.PAID) { "결제 전 주문은 환불할 수 없습니다." }
        this.status = OrderStatus.REFUNDED
    }

    fun softDelete(now: LocalDateTime = LocalDateTime.now()) {
        if (deletedAt == null) {
            deletedAt = now
        }
    }

    fun money(): Money = Money(totalAmount, currency)

    companion object {
        fun create(memberId: Long, lines: List<OrderLine>): CommerceOrder = CommerceOrder(memberId, lines)
    }
}

enum class OrderStatus {
    CREATED,
    CANCELLED,
    PAID,
    REFUNDED,
}
