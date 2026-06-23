package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CommerceLockPort
import com.example.cardservice.application.commerce.provided.CommerceOrderRepository
import com.example.cardservice.application.commerce.provided.CouponHistoryRepository
import com.example.cardservice.application.commerce.provided.CouponRepository
import com.example.cardservice.application.commerce.provided.InventoryRepository
import com.example.cardservice.application.payment.provided.PaymentRepository
import com.example.cardservice.domain.commerce.model.order.CommerceOrder
import com.example.cardservice.domain.commerce.model.order.OrderLine
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.MerchantId
import com.example.cardservice.domain.payment.model.Money
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.Payment
import com.example.cardservice.domain.payment.model.PaymentId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class OrderPaymentFacadeBehaviorSpec : BehaviorSpec({
    given("an existing payment for the same idempotency key") {
        val inventoryRepository = mockk<InventoryRepository>(relaxed = true)
        val orderRepository = mockk<CommerceOrderRepository>(relaxed = true)
        val paymentRepository = mockk<PaymentRepository>(relaxed = true)
        val couponRepository = mockk<CouponRepository>(relaxed = true)
        val couponHistoryRepository = mockk<CouponHistoryRepository>(relaxed = true)
        val commerceLockPort = mockk<CommerceLockPort>(relaxed = true)
        val service = OrderPaymentFacade(
            orderRepository = orderRepository,
            commerceLockPort = commerceLockPort,
            inventoryRepository = inventoryRepository,
            paymentRepository = paymentRepository,
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
        )
        val order = CommerceOrder.create(
            memberId = 1L,
            lines = listOf(OrderLine.create(productId = 1L, productName = "Americano", unitPrice = 12_000L, quantity = 1L)),
        )
        assignId(order, 10L)
        val payment = Payment.authorize(
            merchantId = MerchantId("commerce-merchant"),
            orderId = OrderId("10"),
            idempotencyKey = IdempotencyKey("idem-1"),
            money = Money(12_000L, "KRW"),
        )
        payment.assignId(PaymentId(5L))

        every { commerceLockPort.loadOrderForUpdate(10L) } returns order
        every { paymentRepository.findByIdempotencyKeyValue("idem-1") } returns payment
        every { couponRepository.countByOrderId(10L) } returns 2L
        every { orderRepository.save(order) } returns order

        `when`("the order is paid again") {
            val result = service.payOrder(10L, PayOrderInput("idem-1"))

            then("it returns the existing payment result without repeating side effects") {
                result.paymentId shouldBe 5L
                result.issuedCouponCount shouldBe 2
                verify(exactly = 0) { inventoryRepository.save(any()) }
                verify(exactly = 0) { paymentRepository.save(any()) }
                verify(exactly = 0) { couponRepository.saveAll(any<List<com.example.cardservice.domain.commerce.model.coupon.Coupon>>()) }
                verify(exactly = 0) { couponHistoryRepository.saveAll(any<List<com.example.cardservice.domain.commerce.model.coupon.CouponHistory>>()) }
            }
        }
    }
})

private fun assignId(target: Any, id: Long) {
    val field = target.javaClass.getDeclaredField("id")
    field.isAccessible = true
    field.set(target, id)
}
