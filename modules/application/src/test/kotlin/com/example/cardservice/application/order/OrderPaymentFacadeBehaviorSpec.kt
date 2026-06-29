package com.example.cardservice.application.order

import com.example.cardservice.application.order.provided.OrderWorkflowLockPort
import com.example.cardservice.application.order.provided.OrderRepository
import com.example.cardservice.application.order.provided.OrderStatusMutationPort
import com.example.cardservice.application.coupon.provided.CouponHistoryRepository
import com.example.cardservice.application.coupon.provided.CouponRepository
import com.example.cardservice.application.inventory.provided.InventoryMutationPort
import com.example.cardservice.application.outbox.provided.OutboxEventRepository
import com.example.cardservice.domain.coupon.Coupon
import com.example.cardservice.domain.coupon.CouponHistory
import com.example.cardservice.application.payment.provided.PaymentRepository
import com.example.cardservice.domain.order.Order
import com.example.cardservice.domain.order.OrderItem
import com.example.cardservice.domain.outbox.OutboxEvent
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
        val orderRepository = mockk<OrderRepository>(relaxed = true)
        val paymentRepository = mockk<PaymentRepository>(relaxed = true)
        val couponRepository = mockk<CouponRepository>(relaxed = true)
        val couponHistoryRepository = mockk<CouponHistoryRepository>(relaxed = true)
        val outboxEventRepository = mockk<OutboxEventRepository>(relaxed = true)
        val commerceLockPort = mockk<OrderWorkflowLockPort>(relaxed = true)
        val orderStatusMutationPort = mockk<OrderStatusMutationPort>(relaxed = true)
        val service = OrderPaymentFacade(
            orderRepository = orderRepository,
            commerceLockPort = commerceLockPort,
            orderStatusMutationPort = orderStatusMutationPort,
            inventoryMutationPort = inventoryMutationPort,
            paymentRepository = paymentRepository,
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
            outboxEventRepository = outboxEventRepository,
            objectMapper = ObjectMapper(),
        )
        val order = Order.create(
            memberId = 1L,
            lines = listOf(OrderItem.create(productId = 1L, productName = "Americano", unitPrice = 12_000L, quantity = 1L)),
        )
        assignId(order, 10L)
        val payment = Payment.authorize(
            merchantId = MerchantId(1L),
            orderId = OrderId(10L),
            idempotencyKey = IdempotencyKey("idem-1"),
            money = Money(12_000L, "KRW"),
        )
        payment.assignId(PaymentId(5L))

        every { orderRepository.findByIdAndDeletedAtIsNull(10L) } returns order
        every { orderStatusMutationPort.markPaidIfCreated(10L, 5L) } returns true
        every { paymentRepository.findByIdempotencyKeyValue("idem-1") } returns payment
        every { couponRepository.countByOrderId(10L) } returns 2L

        `when`("the order is paid again") {
            val result = service.payOrder(10L, PayOrderInput("idem-1"))

            then("it returns the existing payment result without repeating side effects") {
                result.paymentId shouldBe 5L
                result.orderStatus shouldBe com.example.cardservice.domain.order.OrderStatus.PAID
                result.issuedCouponCount shouldBe 2
                verify(exactly = 0) { commerceLockPort.loadOrderForUpdate(any()) }
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
        val orderRepository = mockk<OrderRepository>(relaxed = true)
        val paymentRepository = mockk<PaymentRepository>(relaxed = true)
        val couponRepository = mockk<CouponRepository>(relaxed = true)
        val couponHistoryRepository = mockk<CouponHistoryRepository>(relaxed = true)
        val outboxEventRepository = mockk<OutboxEventRepository>(relaxed = true)
        val commerceLockPort = mockk<OrderWorkflowLockPort>(relaxed = true)
        val orderStatusMutationPort = mockk<OrderStatusMutationPort>(relaxed = true)
        val service = OrderPaymentFacade(
            orderRepository = orderRepository,
            commerceLockPort = commerceLockPort,
            orderStatusMutationPort = orderStatusMutationPort,
            inventoryMutationPort = inventoryMutationPort,
            paymentRepository = paymentRepository,
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
            outboxEventRepository = outboxEventRepository,
            objectMapper = ObjectMapper(),
        )
        val order = Order.create(
            memberId = 1L,
            lines = listOf(OrderItem.create(productId = 1L, productName = "Americano", unitPrice = 12_000L, quantity = 1L)),
        )
        assignId(order, 10L)
        val payment = Payment.authorize(
            merchantId = MerchantId(1L),
            orderId = OrderId(10L),
            idempotencyKey = IdempotencyKey("idem-new"),
            money = Money(12_000L, "KRW"),
        )
        payment.assignId(PaymentId(7L))

        every { orderRepository.findByIdAndDeletedAtIsNull(10L) } returns order
        every { paymentRepository.findByIdempotencyKeyValue("idem-new") } returns null
        every { inventoryMutationPort.decreaseQuantityIfEnough(1L, 1L) } returns true
        every { paymentRepository.save(any()) } returns payment
        every { orderStatusMutationPort.markPaidIfCreated(10L, 7L) } returns true
        every { couponRepository.saveAll(any<List<Coupon>>()) } answers { firstArg() }
        every { couponHistoryRepository.saveAll(any<List<CouponHistory>>()) } answers { firstArg() }
        every { outboxEventRepository.save(any()) } answers { firstArg() }

        `when`("the order is paid") {
            val result = service.payOrder(10L, PayOrderInput("idem-new"))

            then("it appends the operational outbox event once") {
                result.paymentId shouldBe 7L
                result.orderStatus shouldBe com.example.cardservice.domain.order.OrderStatus.PAID
                result.issuedCouponCount shouldBe 2
                verify(exactly = 0) { commerceLockPort.loadOrderForUpdate(any()) }
                verify(exactly = 1) { orderStatusMutationPort.markPaidIfCreated(10L, 7L) }
                verify(exactly = 1) { outboxEventRepository.save(any<OutboxEvent>()) }
            }
        }
    }

    given("a competing payment request wins the order status update first") {
        val inventoryMutationPort = mockk<InventoryMutationPort>(relaxed = true)
        val orderRepository = mockk<OrderRepository>(relaxed = true)
        val paymentRepository = mockk<PaymentRepository>(relaxed = true)
        val couponRepository = mockk<CouponRepository>(relaxed = true)
        val couponHistoryRepository = mockk<CouponHistoryRepository>(relaxed = true)
        val outboxEventRepository = mockk<OutboxEventRepository>(relaxed = true)
        val commerceLockPort = mockk<OrderWorkflowLockPort>(relaxed = true)
        val orderStatusMutationPort = mockk<OrderStatusMutationPort>(relaxed = true)
        val service = OrderPaymentFacade(
            orderRepository = orderRepository,
            commerceLockPort = commerceLockPort,
            orderStatusMutationPort = orderStatusMutationPort,
            inventoryMutationPort = inventoryMutationPort,
            paymentRepository = paymentRepository,
            couponRepository = couponRepository,
            couponHistoryRepository = couponHistoryRepository,
            outboxEventRepository = outboxEventRepository,
            objectMapper = ObjectMapper(),
        )
        val order = Order.create(
            memberId = 1L,
            lines = listOf(OrderItem.create(productId = 1L, productName = "Americano", unitPrice = 12_000L, quantity = 1L)),
        )
        assignId(order, 10L)
        val payment = Payment.authorize(
            merchantId = MerchantId(1L),
            orderId = OrderId(10L),
            idempotencyKey = IdempotencyKey("idem-race"),
            money = Money(12_000L, "KRW"),
        )
        payment.assignId(PaymentId(9L))

        every { orderRepository.findByIdAndDeletedAtIsNull(10L) } returns order
        every { paymentRepository.findByIdempotencyKeyValue("idem-race") } returns null
        every { paymentRepository.save(any()) } returns payment
        every { inventoryMutationPort.decreaseQuantityIfEnough(1L, 1L) } returns true
        every { couponRepository.saveAll(any<List<Coupon>>()) } answers { firstArg() }
        every { couponHistoryRepository.saveAll(any<List<CouponHistory>>()) } answers { firstArg() }
        every { outboxEventRepository.save(any()) } answers { firstArg() }
        every { orderStatusMutationPort.markPaidIfCreated(10L, 9L) } returns false

        `when`("the order status update loses") {
            then("it rejects the payment transaction") {
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    service.payOrder(10L, PayOrderInput("idem-race"))
                }.message shouldBe "이미 처리된 주문입니다."

                verify(exactly = 0) { commerceLockPort.loadOrderForUpdate(any()) }
                verify(exactly = 1) { orderStatusMutationPort.markPaidIfCreated(10L, 9L) }
            }
        }
    }
})

private fun assignId(target: Any, id: Long) {
    val field = target.javaClass.getDeclaredField("id")
    field.isAccessible = true
    field.set(target, id)
}
