package com.example.cardservice.domain.product

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
