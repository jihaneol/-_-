package com.example.cardservice.domain.commerce.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CommerceBehaviorSpec : BehaviorSpec({
    given("a 12000 KRW order") {
        val order = CommerceOrder.create(
            memberId = 1L,
            lines = listOf(OrderLine.create(productId = 1L, productName = "Americano", unitPrice = 12_000L, quantity = 1L)),
        )

        `when`("the coupon issue count is calculated") {
            then("it issues two stamp coupons") {
                Coupon.issueCount(order.totalAmount) shouldBe 2
            }
        }
    }

    given("inventory with five items") {
        val inventory = Inventory.create(productId = 1L, quantity = 5L)

        `when`("six items are deducted") {
            then("it rejects the deduction") {
                shouldThrow<IllegalArgumentException> {
                    inventory.decrease(6L)
                }.message shouldBe "재고가 부족합니다."
            }
        }
    }

    given("a created order") {
        val order = CommerceOrder.create(
            memberId = 1L,
            lines = listOf(OrderLine.create(productId = 1L, productName = "Coffee", unitPrice = 5_000L, quantity = 1L)),
        )

        `when`("it is paid") {
            order.pay(paymentId = 1L)

            then("it cannot be cancelled and can be fully refunded once") {
                shouldThrow<IllegalArgumentException> {
                    order.cancel()
                }.message shouldBe "결제 완료된 주문은 주문 취소할 수 없습니다."

                order.refund()
                order.status shouldBe OrderStatus.REFUNDED

                shouldThrow<IllegalArgumentException> {
                    order.refund()
                }.message shouldBe "결제 전 주문은 환불할 수 없습니다."
            }
        }
    }

    given("issued coupons") {
        val coupons = listOf(
            Coupon.issue(memberId = 1L, orderId = 1L, paymentId = 1L),
            Coupon.issue(memberId = 1L, orderId = 1L, paymentId = 1L),
        )

        `when`("a full refund voids them") {
            coupons.forEach { it.void() }

            then("all coupons become voided") {
                coupons.map { it.status } shouldBe listOf(CouponStatus.VOIDED, CouponStatus.VOIDED)
            }
        }
    }

    given("an issued coupon") {
        val coupon = Coupon.issue(memberId = 1L, orderId = 1L, paymentId = 1L)

        `when`("an operator exchanges it") {
            coupon.exchange()

            then("it becomes exchanged and cannot be exchanged again") {
                coupon.status shouldBe CouponStatus.EXCHANGED
                shouldThrow<IllegalArgumentException> {
                    coupon.exchange()
                }.message shouldBe "발급 상태 쿠폰만 교환할 수 있습니다."
            }
        }
    }

    given("a member") {
        val member = Member.create(name = "Kim", email = "kim@example.com")

        `when`("the member is soft deleted") {
            member.softDelete()

            then("it is marked deleted without removing the object") {
                member.deleted shouldBe true
            }
        }
    }
})
