package com.example.cardservice.application.commerce.required

import com.example.cardservice.application.commerce.CouponHistoryResult
import com.example.cardservice.application.commerce.CouponConsistencyReportResult
import com.example.cardservice.application.commerce.CouponHistoryPageResult
import com.example.cardservice.application.commerce.CouponPageQuery
import com.example.cardservice.application.commerce.CouponPageResult
import com.example.cardservice.application.commerce.CouponResult
import com.example.cardservice.application.commerce.CouponWalletResult
import com.example.cardservice.application.commerce.MemberCouponHistoryPageQuery
import com.example.cardservice.application.commerce.OrderCouponHistoryPageQuery

/**
 * 쿠폰과 쿠폰 히스토리 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface CouponQueryUseCase {
    fun listCoupons(query: CouponPageQuery): CouponPageResult
    fun listMemberCouponHistories(query: MemberCouponHistoryPageQuery): CouponHistoryPageResult
    fun listOrderCouponHistories(query: OrderCouponHistoryPageQuery): CouponHistoryPageResult
    fun getCouponWallet(memberId: Long): CouponWalletResult
    fun getCouponConsistencyReport(): CouponConsistencyReportResult
}
