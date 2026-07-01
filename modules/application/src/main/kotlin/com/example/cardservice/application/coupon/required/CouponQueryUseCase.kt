package com.example.cardservice.application.coupon.required

import com.example.cardservice.application.coupon.CouponHistoryResponse
import com.example.cardservice.application.coupon.CouponConsistencyReportResponse
import com.example.cardservice.application.coupon.CouponHistoryPageResponse
import com.example.cardservice.application.coupon.CouponPageResponse
import com.example.cardservice.application.coupon.CouponWalletResponse
import com.example.cardservice.application.common.Pagination

/**
 * 쿠폰과 쿠폰 히스토리 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface CouponQueryUseCase {
    fun listCoupons(memberId: Long, pagination: Pagination): CouponPageResponse
    fun listMemberCouponHistories(memberId: Long, pagination: Pagination): CouponHistoryPageResponse
    fun listOrderCouponHistories(orderId: Long, pagination: Pagination): CouponHistoryPageResponse
    fun getCouponWallet(memberId: Long): CouponWalletResponse
    fun getCouponConsistencyReport(): CouponConsistencyReportResponse
}
