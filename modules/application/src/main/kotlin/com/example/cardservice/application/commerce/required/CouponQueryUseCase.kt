package com.example.cardservice.application.commerce.required

import com.example.cardservice.application.commerce.CouponHistoryResult
import com.example.cardservice.application.commerce.CouponConsistencyReportResult
import com.example.cardservice.application.commerce.CouponResult
import com.example.cardservice.application.commerce.CouponWalletResult

/**
 * 쿠폰과 쿠폰 히스토리 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface CouponQueryUseCase {
    fun listCoupons(memberId: Long): List<CouponResult>
    fun listMemberCouponHistories(memberId: Long): List<CouponHistoryResult>
    fun listOrderCouponHistories(orderId: Long): List<CouponHistoryResult>
    fun getCouponWallet(memberId: Long): CouponWalletResult
    fun getCouponConsistencyReport(): CouponConsistencyReportResult
}
