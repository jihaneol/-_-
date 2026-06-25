package com.example.cardservice.domain.order

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Access(AccessType.FIELD)
@Table(name = "order_lines")
class OrderItem protected constructor() {
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
        fun create(productId: Long, productName: String, unitPrice: Long, quantity: Long): OrderItem =
            OrderItem(productId, productName, unitPrice, quantity)
    }
}
