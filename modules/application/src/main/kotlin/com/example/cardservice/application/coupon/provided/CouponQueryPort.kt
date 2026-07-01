package com.example.cardservice.application.coupon.provided

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.coupon.CouponHistoryPageResponse
import com.example.cardservice.application.coupon.CouponPageResponse

/**
 * 쿠폰과 쿠폰 히스토리 목록 조회를 QueryDSL read model로 수행하기 위해 application query service가 호출하는 outbound port다.
 */
interface CouponQueryPort {
    fun searchCoupons(memberId: Long, pagination: Pagination): CouponPageResponse
    fun searchMemberCouponHistories(memberId: Long, pagination: Pagination): CouponHistoryPageResponse
    fun searchOrderCouponHistories(orderId: Long, pagination: Pagination): CouponHistoryPageResponse
}
