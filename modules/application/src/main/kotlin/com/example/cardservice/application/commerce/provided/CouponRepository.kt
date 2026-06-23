package com.example.cardservice.application.commerce.provided

import com.example.cardservice.domain.commerce.model.coupon.Coupon
import com.example.cardservice.domain.commerce.model.coupon.CouponStatus
import org.springframework.data.repository.Repository

/**
 * Coupon entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface CouponRepository : Repository<Coupon, Long> {
    fun saveAll(coupons: Iterable<Coupon>): List<Coupon>
    fun findAll(): List<Coupon>
    fun findAllByMemberId(memberId: Long): List<Coupon>
    fun findAllByOrderId(orderId: Long): List<Coupon>
    fun countByOrderId(orderId: Long): Long
    fun countByStatus(status: CouponStatus): Long
    fun countByMemberIdAndStatus(memberId: Long, status: CouponStatus): Long
}
