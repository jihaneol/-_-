package com.example.cardservice.application.commerce.provided

import com.example.cardservice.domain.commerce.model.CommerceOrder
import com.example.cardservice.domain.commerce.model.Coupon
import com.example.cardservice.domain.commerce.model.CouponHistory
import com.example.cardservice.domain.commerce.model.Inventory
import com.example.cardservice.domain.commerce.model.Member
import com.example.cardservice.domain.commerce.model.Product
import org.springframework.data.repository.Repository

/**
 * Member entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface MemberRepository : Repository<Member, Long> {
    fun save(member: Member): Member
    fun findAllByDeletedAtIsNull(): List<Member>
    fun findByIdAndDeletedAtIsNull(id: Long): Member?
    fun countByDeletedAtIsNull(): Long
}

/**
 * Product entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface ProductRepository : Repository<Product, Long> {
    fun save(product: Product): Product
    fun findAllByDeletedAtIsNull(): List<Product>
    fun findByIdAndDeletedAtIsNull(id: Long): Product?
    fun countByDeletedAtIsNull(): Long
}

/**
 * Inventory entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface InventoryRepository : Repository<Inventory, Long> {
    fun save(inventory: Inventory): Inventory
    fun findByProductId(productId: Long): Inventory?
}

/**
 * CommerceOrder entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface CommerceOrderRepository : Repository<CommerceOrder, Long> {
    fun save(order: CommerceOrder): CommerceOrder
    fun findAllByDeletedAtIsNull(): List<CommerceOrder>
    fun findByIdAndDeletedAtIsNull(id: Long): CommerceOrder?
    fun countByDeletedAtIsNull(): Long
    fun countByStatusAndDeletedAtIsNull(status: com.example.cardservice.domain.commerce.model.OrderStatus): Long
}

/**
 * Coupon entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface CouponRepository : Repository<Coupon, Long> {
    fun saveAll(coupons: Iterable<Coupon>): List<Coupon>
    fun findAll(): List<Coupon>
    fun findAllByMemberId(memberId: Long): List<Coupon>
    fun findAllByOrderId(orderId: Long): List<Coupon>
    fun countByOrderId(orderId: Long): Long
    fun countByStatus(status: com.example.cardservice.domain.commerce.model.CouponStatus): Long
    fun countByMemberIdAndStatus(memberId: Long, status: com.example.cardservice.domain.commerce.model.CouponStatus): Long
}

/**
 * CouponHistory entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface CouponHistoryRepository : Repository<CouponHistory, Long> {
    fun saveAll(histories: Iterable<CouponHistory>): List<CouponHistory>
    fun findAll(): List<CouponHistory>
    fun findAllByMemberId(memberId: Long): List<CouponHistory>
    fun findAllByOrderId(orderId: Long): List<CouponHistory>
}

/**
 * 결제/환불 흐름에서 주문과 재고를 쓰기 잠금으로 조회하기 위해 application service가 호출하는 outbound port다.
 */
interface CommerceLockPort {
    fun loadOrderForUpdate(orderId: Long): CommerceOrder?
    fun loadInventoryForUpdate(productId: Long): Inventory?
    fun loadCouponForUpdate(couponId: Long): Coupon?
    fun loadIssuedCouponsForExchange(memberId: Long, limit: Int): List<Coupon>
}
