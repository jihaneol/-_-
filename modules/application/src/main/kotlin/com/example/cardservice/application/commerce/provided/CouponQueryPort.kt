package com.example.cardservice.application.commerce.provided

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.commerce.CouponHistoryPageResult
import com.example.cardservice.application.commerce.CouponPageResult

/**
 * 쿠폰과 쿠폰 히스토리 목록 조회를 QueryDSL read model로 수행하기 위해 application query service가 호출하는 outbound port다.
 */
interface CouponQueryPort {
    fun searchCoupons(memberId: Long, pagination: Pagination): CouponPageResult
    fun searchMemberCouponHistories(memberId: Long, pagination: Pagination): CouponHistoryPageResult
    fun searchOrderCouponHistories(orderId: Long, pagination: Pagination): CouponHistoryPageResult
}
