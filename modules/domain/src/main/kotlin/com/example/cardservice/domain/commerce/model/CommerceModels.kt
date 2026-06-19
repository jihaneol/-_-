package com.example.cardservice.domain.commerce.model

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
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Access(AccessType.FIELD)
@Table(name = "members")
class Member protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "name", nullable = false, length = 100)
    var name: String = ""
        protected set

    @Column(name = "email", nullable = false, length = 200)
    var email: String = ""
        protected set

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
        protected set

    val deleted: Boolean
        get() = deletedAt != null

    private constructor(name: String, email: String) : this() {
        require(name.isNotBlank()) { "회원 이름은 비어 있을 수 없습니다." }
        require(email.isNotBlank()) { "회원 이메일은 비어 있을 수 없습니다." }
        this.name = name
        this.email = email
    }

    fun update(name: String, email: String) {
        require(!deleted) { "삭제된 회원은 수정할 수 없습니다." }
        require(name.isNotBlank()) { "회원 이름은 비어 있을 수 없습니다." }
        require(email.isNotBlank()) { "회원 이메일은 비어 있을 수 없습니다." }
        this.name = name
        this.email = email
    }

    fun softDelete(now: LocalDateTime = LocalDateTime.now()) {
        if (deletedAt == null) {
            deletedAt = now
        }
    }

    companion object {
        fun create(name: String, email: String): Member = Member(name, email)
    }
}

@Entity
@Access(AccessType.FIELD)
@Table(name = "products")
class Product protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "name", nullable = false, length = 150)
    var name: String = ""
        protected set

    @Column(name = "price", nullable = false)
    var price: Long = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false, length = 30)
    var saleStatus: ProductSaleStatus = ProductSaleStatus.ON_SALE
        protected set

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
        protected set

    val deleted: Boolean
        get() = deletedAt != null

    private constructor(name: String, price: Long) : this() {
        require(name.isNotBlank()) { "상품명은 비어 있을 수 없습니다." }
        require(price > 0) { "상품 가격은 0보다 커야 합니다." }
        this.name = name
        this.price = price
    }

    fun update(name: String, price: Long, saleStatus: ProductSaleStatus) {
        require(!deleted) { "삭제된 상품은 수정할 수 없습니다." }
        require(name.isNotBlank()) { "상품명은 비어 있을 수 없습니다." }
        require(price > 0) { "상품 가격은 0보다 커야 합니다." }
        this.name = name
        this.price = price
        this.saleStatus = saleStatus
    }

    fun softDelete(now: LocalDateTime = LocalDateTime.now()) {
        if (deletedAt == null) {
            deletedAt = now
        }
    }

    companion object {
        fun create(name: String, price: Long): Product = Product(name, price)
    }
}

enum class ProductSaleStatus {
    ON_SALE,
    STOPPED,
}

@Entity
@Access(AccessType.FIELD)
@Table(
    name = "inventories",
    uniqueConstraints = [UniqueConstraint(name = "uk_inventories_product_id", columnNames = ["product_id"])],
)
class Inventory protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "product_id", nullable = false)
    var productId: Long = 0
        protected set

    @Column(name = "quantity", nullable = false)
    var quantity: Long = 0
        protected set

    private constructor(productId: Long, quantity: Long) : this() {
        require(productId > 0) { "상품 ID는 0보다 커야 합니다." }
        require(quantity >= 0) { "재고 수량은 음수일 수 없습니다." }
        this.productId = productId
        this.quantity = quantity
    }

    fun increase(quantity: Long) {
        require(quantity > 0) { "증가 수량은 0보다 커야 합니다." }
        this.quantity += quantity
    }

    fun decrease(quantity: Long) {
        require(quantity > 0) { "차감 수량은 0보다 커야 합니다." }
        require(this.quantity >= quantity) { "재고가 부족합니다." }
        this.quantity -= quantity
    }

    companion object {
        fun create(productId: Long, quantity: Long): Inventory = Inventory(productId, quantity)
    }
}

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

@Entity
@Access(AccessType.FIELD)
@Table(name = "order_lines")
class OrderLine protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "product_id", nullable = false)
    var productId: Long = 0
        protected set

    @Column(name = "product_name", nullable = false, length = 150)
    var productName: String = ""
        protected set

    @Column(name = "unit_price", nullable = false)
    var unitPrice: Long = 0
        protected set

    @Column(name = "quantity", nullable = false)
    var quantity: Long = 0
        protected set

    @Column(name = "line_amount", nullable = false)
    var lineAmount: Long = 0
        protected set

    private constructor(productId: Long, productName: String, unitPrice: Long, quantity: Long) : this() {
        require(productId > 0) { "상품 ID는 0보다 커야 합니다." }
        require(productName.isNotBlank()) { "상품명은 비어 있을 수 없습니다." }
        require(unitPrice > 0) { "상품 가격은 0보다 커야 합니다." }
        require(quantity > 0) { "주문 수량은 0보다 커야 합니다." }
        this.productId = productId
        this.productName = productName
        this.unitPrice = unitPrice
        this.quantity = quantity
        this.lineAmount = unitPrice * quantity
    }

    companion object {
        fun create(productId: Long, productName: String, unitPrice: Long, quantity: Long): OrderLine =
            OrderLine(productId, productName, unitPrice, quantity)
    }
}

enum class OrderStatus {
    CREATED,
    CANCELLED,
    PAID,
    REFUNDED,
}

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
