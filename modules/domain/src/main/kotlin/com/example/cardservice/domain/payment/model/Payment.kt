package com.example.cardservice.domain.payment.model

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

@Entity
@Access(AccessType.FIELD)
@Table(
    name = "payments",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_payments_idempotency_key", columnNames = ["idempotency_key"]),
    ],
)
class Payment protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "merchant_id", nullable = false, length = 100)
    private var merchantIdValue: String = ""

    @Column(name = "order_id", nullable = false, length = 100)
    private var orderIdValue: String = ""

    @Column(name = "idempotency_key", nullable = false, length = 150)
    private var idempotencyKeyValue: String = ""

    @Column(name = "amount", nullable = false)
    private var amountValue: Long = 0

    @Column(name = "currency", nullable = false, length = 3)
    private var currencyValue: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private var statusValue: PaymentStatus = PaymentStatus.AUTHORIZED

    val paymentId: PaymentId?
        get() = id?.let(::PaymentId)

    val merchantId: MerchantId
        get() = MerchantId(merchantIdValue)

    val orderId: OrderId
        get() = OrderId(orderIdValue)

    val idempotencyKey: IdempotencyKey
        get() = IdempotencyKey(idempotencyKeyValue)

    val money: Money
        get() = Money(amount = amountValue, currency = currencyValue)

    val status: PaymentStatus
        get() = statusValue

    private constructor(
        merchantId: MerchantId,
        orderId: OrderId,
        idempotencyKey: IdempotencyKey,
        money: Money,
        status: PaymentStatus,
    ) : this() {
        this.merchantIdValue = merchantId.value
        this.orderIdValue = orderId.value
        this.idempotencyKeyValue = idempotencyKey.value
        this.amountValue = money.amount
        this.currencyValue = money.currency
        this.statusValue = status
    }

    fun assignId(paymentId: PaymentId) {
        this.id = paymentId.value
    }

    fun refund() {
        require(statusValue == PaymentStatus.AUTHORIZED) { "이미 환불된 결제입니다." }
        statusValue = PaymentStatus.REFUNDED
    }

    companion object {
        fun authorize(
            merchantId: MerchantId,
            orderId: OrderId,
            idempotencyKey: IdempotencyKey,
            money: Money,
        ): Payment =
            Payment(
                merchantId = merchantId,
                orderId = orderId,
                idempotencyKey = idempotencyKey,
                money = money,
                status = PaymentStatus.AUTHORIZED,
            )
    }
}
