package com.example.cardservice.application.coupon

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.coupon.provided.CouponHistoryRepository
import com.example.cardservice.application.coupon.provided.CouponQueryPort
import com.example.cardservice.application.coupon.provided.CouponRepository
import com.example.cardservice.application.coupon.required.CouponQueryUseCase
import com.example.cardservice.domain.coupon.Coupon
import com.example.cardservice.domain.coupon.CouponHistory
import com.example.cardservice.domain.coupon.CouponHistoryType
import com.example.cardservice.domain.coupon.CouponStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 쿠폰 발급 건과 쿠폰 히스토리 조회 흐름을 조율하는 application query service다.
 */
@Service
class CouponQueryService(
    private val couponRepository: CouponRepository,
    private val couponHistoryRepository: CouponHistoryRepository,
    private val couponQueryPort: CouponQueryPort,
) : CouponQueryUseCase {
    @Transactional(readOnly = true)
    override fun listCoupons(memberId: Long, pagination: Pagination): CouponPageResult =
        couponQueryPort.searchCoupons(memberId, pagination)

    @Transactional(readOnly = true)
    override fun listMemberCouponHistories(memberId: Long, pagination: Pagination): CouponHistoryPageResult =
        couponQueryPort.searchMemberCouponHistories(memberId, pagination)

    @Transactional(readOnly = true)
    override fun listOrderCouponHistories(orderId: Long, pagination: Pagination): CouponHistoryPageResult =
        couponQueryPort.searchOrderCouponHistories(orderId, pagination)

    @Transactional(readOnly = true)
    override fun getCouponWallet(memberId: Long): CouponWalletResult {
        val coupons = couponRepository.findAllByMemberId(memberId)
        val histories = couponHistoryRepository.findAllByMemberId(memberId)
            .sortedByDescending { it.id ?: 0L }
            .take(5)
            .map { it.toResult() }
        val issuedCouponCount = coupons.countStatus(CouponStatus.ISSUED)
        val exchangedCouponCount = coupons.countStatus(CouponStatus.EXCHANGED)
        val voidedCouponCount = coupons.countStatus(CouponStatus.VOIDED)
        return CouponWalletResult(
            memberId = memberId,
            issuedCouponCount = issuedCouponCount,
            exchangedCouponCount = exchangedCouponCount,
            voidedCouponCount = voidedCouponCount,
            totalCouponCount = coupons.size.toLong(),
            exchangeableSetCount = issuedCouponCount / REQUIRED_COUPON_COUNT,
            remainingToNextExchange = (REQUIRED_COUPON_COUNT - issuedCouponCount % REQUIRED_COUPON_COUNT) % REQUIRED_COUPON_COUNT,
            recentHistories = histories,
        )
    }

    @Transactional(readOnly = true)
    override fun getCouponConsistencyReport(): CouponConsistencyReportResult {
        val coupons = couponRepository.findAll()
        val histories = couponHistoryRepository.findAll()
        val memberRows = buildMemberRows(coupons, histories)
        val orderRows = buildOrderRows(coupons, histories)
        return CouponConsistencyReportResult(
            consistent = memberRows.all { it.consistent } && orderRows.all { it.consistent },
            totalCouponCount = coupons.size.toLong(),
            totalIssueHistoryCount = histories.countType(CouponHistoryType.ISSUED),
            totalVoidHistoryCount = histories.countType(CouponHistoryType.VOIDED),
            totalExchangeHistoryCount = histories.countType(CouponHistoryType.EXCHANGED),
            memberRows = memberRows,
            orderRows = orderRows,
        )
    }

    private fun buildMemberRows(
        coupons: List<Coupon>,
        histories: List<CouponHistory>,
    ): List<MemberCouponConsistencyResult> {
        val memberIds = (coupons.map { it.memberId } + histories.map { it.memberId }).distinct().sorted()
        return memberIds.map { memberId ->
            val memberCoupons = coupons.filter { it.memberId == memberId }
            val memberHistories = histories.filter { it.memberId == memberId }
            val issuedCouponCount = memberCoupons.countStatus(CouponStatus.ISSUED)
            val voidedCouponCount = memberCoupons.countStatus(CouponStatus.VOIDED)
            val exchangedCouponCount = memberCoupons.countStatus(CouponStatus.EXCHANGED)
            val issueHistoryCount = memberHistories.countType(CouponHistoryType.ISSUED)
            val voidHistoryCount = memberHistories.countType(CouponHistoryType.VOIDED)
            val exchangeHistoryCount = memberHistories.countType(CouponHistoryType.EXCHANGED)
            MemberCouponConsistencyResult(
                memberId = memberId,
                issuedCouponCount = issuedCouponCount,
                voidedCouponCount = voidedCouponCount,
                exchangedCouponCount = exchangedCouponCount,
                issueHistoryCount = issueHistoryCount,
                voidHistoryCount = voidHistoryCount,
                exchangeHistoryCount = exchangeHistoryCount,
                exchangeableSetCount = issuedCouponCount / REQUIRED_COUPON_COUNT,
                remainingToNextExchange = (REQUIRED_COUPON_COUNT - issuedCouponCount % REQUIRED_COUPON_COUNT) % REQUIRED_COUPON_COUNT,
                consistent = memberCoupons.size.toLong() == issueHistoryCount &&
                    voidedCouponCount == voidHistoryCount &&
                    exchangedCouponCount == exchangeHistoryCount,
            )
        }
    }

    private fun buildOrderRows(
        coupons: List<Coupon>,
        histories: List<CouponHistory>,
    ): List<OrderCouponConsistencyResult> {
        val orderIds = (coupons.map { it.orderId } + histories.map { it.orderId }).distinct().sorted()
        return orderIds.map { orderId ->
            val orderCoupons = coupons.filter { it.orderId == orderId }
            val orderHistories = histories.filter { it.orderId == orderId }
            val memberId = (orderCoupons.firstOrNull()?.memberId ?: orderHistories.firstOrNull()?.memberId) ?: 0L
            val issuedCouponCount = orderCoupons.countStatus(CouponStatus.ISSUED)
            val voidedCouponCount = orderCoupons.countStatus(CouponStatus.VOIDED)
            val exchangedCouponCount = orderCoupons.countStatus(CouponStatus.EXCHANGED)
            val issueHistoryCount = orderHistories.countType(CouponHistoryType.ISSUED)
            val voidHistoryCount = orderHistories.countType(CouponHistoryType.VOIDED)
            val exchangeHistoryCount = orderHistories.countType(CouponHistoryType.EXCHANGED)
            OrderCouponConsistencyResult(
                orderId = orderId,
                memberId = memberId,
                issuedCouponCount = issuedCouponCount,
                voidedCouponCount = voidedCouponCount,
                exchangedCouponCount = exchangedCouponCount,
                issueHistoryCount = issueHistoryCount,
                voidHistoryCount = voidHistoryCount,
                exchangeHistoryCount = exchangeHistoryCount,
                consistent = orderCoupons.size.toLong() == issueHistoryCount &&
                    voidedCouponCount == voidHistoryCount &&
                    exchangedCouponCount == exchangeHistoryCount,
            )
        }
    }
}

internal fun Coupon.toResult(): CouponResult =
    CouponResult(id = id, memberId = memberId, orderId = orderId, paymentId = paymentId, status = status)

internal fun CouponHistory.toResult(): CouponHistoryResult =
    CouponHistoryResult(
        id = id,
        couponId = couponId,
        memberId = memberId,
        orderId = orderId,
        paymentId = paymentId,
        type = type,
    )

private fun List<Coupon>.countStatus(status: CouponStatus): Long = count { it.status == status }.toLong()
private fun List<CouponHistory>.countType(type: CouponHistoryType): Long = count { it.type == type }.toLong()

private const val REQUIRED_COUPON_COUNT = 10L
