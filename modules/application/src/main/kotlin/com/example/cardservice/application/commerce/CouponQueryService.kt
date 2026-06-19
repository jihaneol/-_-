package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CouponHistoryRepository
import com.example.cardservice.application.commerce.provided.CouponRepository
import com.example.cardservice.application.commerce.required.CouponQueryUseCase
import com.example.cardservice.domain.commerce.model.Coupon
import com.example.cardservice.domain.commerce.model.CouponHistory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 쿠폰 발급 건과 쿠폰 히스토리 조회 흐름을 조율하는 application query service다.
 */
@Service
class CouponQueryService(
    private val couponRepository: CouponRepository,
    private val couponHistoryRepository: CouponHistoryRepository,
) : CouponQueryUseCase {
    @Transactional(readOnly = true)
    override fun listCoupons(memberId: Long): List<CouponResult> =
        couponRepository.findAllByMemberId(memberId).map { it.toResult() }

    @Transactional(readOnly = true)
    override fun listMemberCouponHistories(memberId: Long): List<CouponHistoryResult> =
        couponHistoryRepository.findAllByMemberId(memberId).map { it.toResult() }

    @Transactional(readOnly = true)
    override fun listOrderCouponHistories(orderId: Long): List<CouponHistoryResult> =
        couponHistoryRepository.findAllByOrderId(orderId).map { it.toResult() }
}

private fun Coupon.toResult(): CouponResult =
    CouponResult(id = requireNotNull(id), memberId = memberId, orderId = orderId, paymentId = paymentId, status = status)

private fun CouponHistory.toResult(): CouponHistoryResult =
    CouponHistoryResult(
        id = requireNotNull(id),
        couponId = couponId,
        memberId = memberId,
        orderId = orderId,
        paymentId = paymentId,
        type = type,
    )
