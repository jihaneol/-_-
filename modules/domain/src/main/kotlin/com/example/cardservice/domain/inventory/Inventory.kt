package com.example.cardservice.domain.inventory

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

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
