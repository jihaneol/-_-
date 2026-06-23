package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CommerceLockPort
import com.example.cardservice.application.commerce.provided.CouponHistoryRepository
import com.example.cardservice.application.commerce.provided.CouponRepository
import com.example.cardservice.application.commerce.provided.InventoryRepository
import com.example.cardservice.application.commerce.provided.MemberRepository
import com.example.cardservice.application.commerce.provided.ProductRepository
import com.example.cardservice.domain.commerce.model.coupon.Coupon
import com.example.cardservice.domain.commerce.model.coupon.CouponHistory
import com.example.cardservice.domain.commerce.model.coupon.CouponHistoryType
import com.example.cardservice.domain.commerce.model.coupon.CouponStatus
import com.example.cardservice.domain.commerce.model.inventory.Inventory
import com.example.cardservice.domain.commerce.model.member.Member
import com.example.cardservice.domain.commerce.model.product.Product
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class CouponExchangeServiceBehaviorSpec : BehaviorSpec({
    given("an issued coupon") {
        val commerceLockPort = mockk<CommerceLockPort>()
        val memberRepository = mockk<MemberRepository>()
        val productRepository = mockk<ProductRepository>()
        val inventoryRepository = mockk<InventoryRepository>()
        val couponRepository = mockk<CouponRepository>()
        val couponHistoryRepository = mockk<CouponHistoryRepository>()
        val service = CouponExchangeService(
            commerceLockPort = commerceLockPort,
            memberRepository = memberRepository,
            productRepository = productRepository,
            inventoryRepository = inventoryRepository,
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
        )
        val coupon = Coupon.issue(memberId = 7L, orderId = 11L, paymentId = 13L)
        assignId(coupon, 5L)
        val historySlot = slot<List<CouponHistory>>()

        every { commerceLockPort.loadCouponForUpdate(5L) } returns coupon
        every { couponRepository.saveAll(any<List<Coupon>>()) } answers { firstArg() }
        every { couponHistoryRepository.saveAll(capture(historySlot)) } answers {
            historySlot.captured.onEachIndexed { index, history -> assignId(history, index + 20L) }
        }

        `when`("the coupon is exchanged") {
            val result = service.exchangeCoupon(5L)

            then("it locks the coupon, stores exchanged state, and appends history") {
                result.coupon.id shouldBe 5L
                result.coupon.status shouldBe CouponStatus.EXCHANGED
                result.history.couponId shouldBe 5L
                result.history.type shouldBe CouponHistoryType.EXCHANGED
                verify { commerceLockPort.loadCouponForUpdate(5L) }
                verify { couponRepository.saveAll(match<List<Coupon>> { it.single().status == CouponStatus.EXCHANGED }) }
                verify { couponHistoryRepository.saveAll(match<List<CouponHistory>> { it.single().type == CouponHistoryType.EXCHANGED }) }
            }
        }
    }

    given("a member with ten issued coupons and an exchange product") {
        val commerceLockPort = mockk<CommerceLockPort>()
        val memberRepository = mockk<MemberRepository>()
        val productRepository = mockk<ProductRepository>()
        val inventoryRepository = mockk<InventoryRepository>()
        val couponRepository = mockk<CouponRepository>()
        val couponHistoryRepository = mockk<CouponHistoryRepository>()
        val service = CouponExchangeService(
            commerceLockPort = commerceLockPort,
            memberRepository = memberRepository,
            productRepository = productRepository,
            inventoryRepository = inventoryRepository,
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
        )
        val member = Member.create(name = "Lee", email = "lee@example.com")
        assignId(member, 3L)
        val product = Product.create(name = "Americano", price = 5_000L)
        assignId(product, 8L)
        val inventory = Inventory.create(productId = 8L, quantity = 3L)
        assignId(inventory, 9L)
        val coupons = (1L..10L).map { id ->
            Coupon.issue(memberId = 3L, orderId = id, paymentId = id).also { assignId(it, id) }
        }

        every { memberRepository.findByIdAndDeletedAtIsNull(3L) } returns member
        every { productRepository.findByIdAndDeletedAtIsNull(8L) } returns product
        every { commerceLockPort.loadInventoryForUpdate(8L) } returns inventory
        every { commerceLockPort.loadIssuedCouponsForExchange(3L, 10) } returns coupons
        every { inventoryRepository.save(inventory) } returns inventory
        every { couponRepository.saveAll(any<List<Coupon>>()) } answers { firstArg() }
        every { couponHistoryRepository.saveAll(any<List<CouponHistory>>()) } answers {
            firstArg<List<CouponHistory>>().onEachIndexed { index, history -> assignId(history, index + 20L) }
        }
        every { couponRepository.countByMemberIdAndStatus(3L, CouponStatus.ISSUED) } returns 0L

        `when`("the operator approves coupon exchange") {
            val result = service.approveCouponExchange(3L, ApproveCouponExchangeInput(productId = 8L))

            then("it exchanges ten coupons and deducts one inventory item") {
                result.exchangedCouponCount shouldBe 10
                result.remainingIssuedCouponCount shouldBe 0L
                result.exchangedCouponIds shouldBe (1L..10L).toList()
                inventory.quantity shouldBe 2L
                coupons.map { it.status }.toSet() shouldBe setOf(CouponStatus.EXCHANGED)
                verify { commerceLockPort.loadIssuedCouponsForExchange(3L, 10) }
                verify { inventoryRepository.save(inventory) }
                verify { couponHistoryRepository.saveAll(match<List<CouponHistory>> { it.size == 10 && it.all { history -> history.type == CouponHistoryType.EXCHANGED } }) }
            }
        }
    }
})

private fun assignId(target: Any, id: Long) {
    val field = target.javaClass.getDeclaredField("id")
    field.isAccessible = true
    field.set(target, id)
}
