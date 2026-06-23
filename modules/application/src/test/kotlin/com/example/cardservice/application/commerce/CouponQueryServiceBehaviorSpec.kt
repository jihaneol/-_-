package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CouponHistoryRepository
import com.example.cardservice.application.commerce.provided.CouponQueryPort
import com.example.cardservice.application.commerce.provided.CouponRepository
import com.example.cardservice.domain.commerce.model.coupon.Coupon
import com.example.cardservice.domain.commerce.model.coupon.CouponHistory
import com.example.cardservice.domain.commerce.model.coupon.CouponHistoryType
import com.example.cardservice.domain.commerce.model.coupon.CouponStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class CouponQueryServiceBehaviorSpec : BehaviorSpec({
    given("coupons and immutable histories") {
        val couponRepository = mockk<CouponRepository>()
        val couponHistoryRepository = mockk<CouponHistoryRepository>()
        val couponQueryPort = mockk<CouponQueryPort>(relaxed = true)
        val service = CouponQueryService(
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
            couponQueryPort = couponQueryPort,
        )
        val issuedCoupon = Coupon.issue(memberId = 3L, orderId = 7L, paymentId = 9L).also { assignQueryTestId(it, 1L) }
        val exchangedCoupon = Coupon.issue(memberId = 3L, orderId = 7L, paymentId = 9L).also {
            assignQueryTestId(it, 2L)
            it.exchange()
        }
        val voidedCoupon = Coupon.issue(memberId = 3L, orderId = 7L, paymentId = 9L).also {
            assignQueryTestId(it, 3L)
            it.void()
        }
        val histories = listOf(
            CouponHistory.issued(issuedCoupon),
            CouponHistory.issued(exchangedCoupon),
            CouponHistory.exchanged(exchangedCoupon),
            CouponHistory.issued(voidedCoupon),
            CouponHistory.voided(voidedCoupon),
        ).onEachIndexed { index, history -> assignQueryTestId(history, index + 10L) }

        every { couponRepository.findAll() } returns listOf(issuedCoupon, exchangedCoupon, voidedCoupon)
        every { couponHistoryRepository.findAll() } returns histories
        every { couponRepository.findAllByMemberId(3L) } returns listOf(issuedCoupon, exchangedCoupon, voidedCoupon)
        every { couponHistoryRepository.findAllByMemberId(3L) } returns histories

        `when`("the operator requests coupon consistency") {
            val report = service.getCouponConsistencyReport()

            then("it compares current coupon state with history events by member and order") {
                report.consistent shouldBe true
                report.totalCouponCount shouldBe 3L
                report.totalIssueHistoryCount shouldBe 3L
                report.totalVoidHistoryCount shouldBe 1L
                report.totalExchangeHistoryCount shouldBe 1L

                val memberRow = report.memberRows.single()
                memberRow.memberId shouldBe 3L
                memberRow.issuedCouponCount shouldBe 1L
                memberRow.voidedCouponCount shouldBe 1L
                memberRow.exchangedCouponCount shouldBe 1L
                memberRow.issueHistoryCount shouldBe 3L
                memberRow.voidHistoryCount shouldBe 1L
                memberRow.exchangeHistoryCount shouldBe 1L
                memberRow.exchangeableSetCount shouldBe 0L
                memberRow.remainingToNextExchange shouldBe 9L
                memberRow.consistent shouldBe true

                val orderRow = report.orderRows.single()
                orderRow.orderId shouldBe 7L
                orderRow.memberId shouldBe 3L
                orderRow.consistent shouldBe true
                orderRow.exchangeHistoryCount shouldBe 1L
            }
        }

        `when`("the customer requests a coupon wallet summary") {
            val wallet = service.getCouponWallet(3L)

            then("it returns customer-safe counts and recent history") {
                wallet.memberId shouldBe 3L
                wallet.issuedCouponCount shouldBe 1L
                wallet.exchangedCouponCount shouldBe 1L
                wallet.voidedCouponCount shouldBe 1L
                wallet.totalCouponCount shouldBe 3L
                wallet.exchangeableSetCount shouldBe 0L
                wallet.remainingToNextExchange shouldBe 9L
                wallet.recentHistories.size shouldBe 5
                wallet.recentHistories.first().id shouldBe 14L
            }
        }
    }
})

private fun assignQueryTestId(target: Any, id: Long) {
    val field = target.javaClass.getDeclaredField("id")
    field.isAccessible = true
    field.set(target, id)
}
