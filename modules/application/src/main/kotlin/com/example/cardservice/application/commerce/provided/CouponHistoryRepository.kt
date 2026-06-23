package com.example.cardservice.application.commerce.provided

import com.example.cardservice.domain.commerce.model.coupon.CouponHistory
import org.springframework.data.repository.Repository

/**
 * CouponHistory entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface CouponHistoryRepository : Repository<CouponHistory, Long> {
    fun saveAll(histories: Iterable<CouponHistory>): List<CouponHistory>
    fun findAll(): List<CouponHistory>
    fun findAllByMemberId(memberId: Long): List<CouponHistory>
    fun findAllByOrderId(orderId: Long): List<CouponHistory>
}
