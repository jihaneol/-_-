package com.example.cardservice.application.coupon.required

import com.example.cardservice.application.coupon.CouponHistoryResult
import com.example.cardservice.application.coupon.CouponConsistencyReportResult
import com.example.cardservice.application.coupon.CouponHistoryPageResult
import com.example.cardservice.application.coupon.CouponPageResult
import com.example.cardservice.application.coupon.CouponWalletResult
import com.example.cardservice.application.common.Pagination

/**
 * 쿠폰과 쿠폰 히스토리 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface CouponQueryUseCase {
    fun listCoupons(memberId: Long, pagination: Pagination): CouponPageResult
    fun listMemberCouponHistories(memberId: Long, pagination: Pagination): CouponHistoryPageResult
    fun listOrderCouponHistories(orderId: Long, pagination: Pagination): CouponHistoryPageResult
    fun getCouponWallet(memberId: Long): CouponWalletResult
    fun getCouponConsistencyReport(): CouponConsistencyReportResult
}
