package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CommerceLockPort
import com.example.cardservice.application.commerce.provided.CommerceOrderRepository
import com.example.cardservice.application.commerce.provided.CouponHistoryRepository
import com.example.cardservice.application.commerce.provided.CouponRepository
import com.example.cardservice.application.commerce.provided.InventoryMutationPort
import com.example.cardservice.application.commerce.provided.OutboxEventRepository
import com.example.cardservice.domain.commerce.model.coupon.Coupon
import com.example.cardservice.domain.commerce.model.coupon.CouponHistory
import com.example.cardservice.application.payment.provided.PaymentRepository
import com.example.cardservice.domain.commerce.model.order.CommerceOrder
import com.example.cardservice.domain.commerce.model.order.OrderLine
import com.example.cardservice.domain.commerce.model.outbox.OutboxEvent
import com.fasterxml.jackson.databind.ObjectMapper
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
        val inventoryMutationPort = mockk<InventoryMutationPort>(relaxed = true)
        val orderRepository = mockk<CommerceOrderRepository>(relaxed = true)
        val paymentRepository = mockk<PaymentRepository>(relaxed = true)
        val couponRepository = mockk<CouponRepository>(relaxed = true)
        val couponHistoryRepository = mockk<CouponHistoryRepository>(relaxed = true)
        val outboxEventRepository = mockk<OutboxEventRepository>(relaxed = true)
        val commerceLockPort = mockk<CommerceLockPort>(relaxed = true)
        val service = OrderPaymentFacade(
            orderRepository = orderRepository,
            commerceLockPort = commerceLockPort,
            inventoryMutationPort = inventoryMutationPort,
            paymentRepository = paymentRepository,
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
            outboxEventRepository = outboxEventRepository,
            objectMapper = ObjectMapper(),
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
                verify(exactly = 0) { inventoryMutationPort.decreaseQuantityIfEnough(any(), any()) }
                verify(exactly = 0) { paymentRepository.save(any()) }
                verify(exactly = 0) { couponRepository.saveAll(any<List<Coupon>>()) }
                verify(exactly = 0) { couponHistoryRepository.saveAll(any<List<CouponHistory>>()) }
                verify(exactly = 0) { outboxEventRepository.save(any()) }
            }
        }
    }

    given("a new payment request") {
        val inventoryMutationPort = mockk<InventoryMutationPort>(relaxed = true)
        val orderRepository = mockk<CommerceOrderRepository>(relaxed = true)
        val paymentRepository = mockk<PaymentRepository>(relaxed = true)
        val couponRepository = mockk<CouponRepository>(relaxed = true)
        val couponHistoryRepository = mockk<CouponHistoryRepository>(relaxed = true)
        val outboxEventRepository = mockk<OutboxEventRepository>(relaxed = true)
        val commerceLockPort = mockk<CommerceLockPort>(relaxed = true)
        val service = OrderPaymentFacade(
            orderRepository = orderRepository,
            commerceLockPort = commerceLockPort,
            inventoryMutationPort = inventoryMutationPort,
            paymentRepository = paymentRepository,
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
            outboxEventRepository = outboxEventRepository,
            objectMapper = ObjectMapper(),
        )
        val order = CommerceOrder.create(
            memberId = 1L,
            lines = listOf(OrderLine.create(productId = 1L, productName = "Americano", unitPrice = 12_000L, quantity = 1L)),
        )
        assignId(order, 10L)
        val payment = Payment.authorize(
            merchantId = MerchantId("commerce-merchant"),
            orderId = OrderId("10"),
            idempotencyKey = IdempotencyKey("idem-new"),
            money = Money(12_000L, "KRW"),
        )
        payment.assignId(PaymentId(7L))

        every { commerceLockPort.loadOrderForUpdate(10L) } returns order
        every { paymentRepository.findByIdempotencyKeyValue("idem-new") } returns null
        every { inventoryMutationPort.decreaseQuantityIfEnough(1L, 1L) } returns true
        every { paymentRepository.save(any()) } returns payment
        every { orderRepository.save(order) } returns order
        every { couponRepository.saveAll(any<List<Coupon>>()) } answers { firstArg() }
        every { couponHistoryRepository.saveAll(any<List<CouponHistory>>()) } answers { firstArg() }
        every { outboxEventRepository.save(any()) } answers { firstArg() }

        `when`("the order is paid") {
            val result = service.payOrder(10L, PayOrderInput("idem-new"))

            then("it appends the operational outbox event once") {
                result.paymentId shouldBe 7L
                result.issuedCouponCount shouldBe 2
                verify(exactly = 1) { outboxEventRepository.save(any<OutboxEvent>()) }
            }
        }
    }
})

private fun assignId(target: Any, id: Long) {
    val field = target.javaClass.getDeclaredField("id")
    field.isAccessible = true
    field.set(target, id)
}
