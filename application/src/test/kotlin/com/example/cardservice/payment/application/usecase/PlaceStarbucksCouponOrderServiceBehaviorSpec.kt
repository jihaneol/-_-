package com.example.cardservice.payment.application.usecase

import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentCommand
import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentResult
import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentUseCase
import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderCommand
import com.example.cardservice.payment.application.port.out.AccrueCouponPort
import com.example.cardservice.payment.application.port.out.CouponAccrualResult
import com.example.cardservice.payment.application.port.out.ExternalPaymentApproval
import com.example.cardservice.payment.application.port.out.ExternalPaymentPort
import com.example.cardservice.payment.domain.model.CustomerId
import com.example.cardservice.payment.domain.model.IdempotencyKey
import com.example.cardservice.payment.domain.model.OrderId
import com.example.cardservice.payment.domain.model.PaymentId
import com.example.cardservice.payment.domain.model.PaymentStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class PlaceStarbucksCouponOrderServiceBehaviorSpec : BehaviorSpec({
    given("a Starbucks coupon order") {
        val externalPaymentPort = mockk<ExternalPaymentPort>()
        val authorizePaymentUseCase = mockk<AuthorizePaymentUseCase>()
        val accrueCouponPort = mockk<AccrueCouponPort>()
        val service = PlaceStarbucksCouponOrderService(
            externalPaymentPort = externalPaymentPort,
            authorizePaymentUseCase = authorizePaymentUseCase,
            accrueCouponPort = accrueCouponPort,
        )
        val command = PlaceStarbucksCouponOrderCommand(
            customerId = CustomerId("customer-1"),
            orderId = OrderId("order-1"),
            idempotencyKey = IdempotencyKey("idem-1"),
            quantity = 2,
        )

        every { externalPaymentPort.approve(any()) } returns ExternalPaymentApproval("mock-order-1")
        every { authorizePaymentUseCase.authorize(any()) } answers {
            val authorizeCommand = firstArg<AuthorizePaymentCommand>()
            AuthorizePaymentResult(
                paymentId = PaymentId("pay_1"),
                status = PaymentStatus.AUTHORIZED,
                amount = authorizeCommand.money.amount,
                currency = authorizeCommand.money.currency,
            )
        }
        every { accrueCouponPort.accrue(any()) } returns CouponAccrualResult(
            couponIds = listOf("starbucks_coupon_1", "starbucks_coupon_2"),
        )

        `when`("the order is placed") {
            val result = service.place(command)

            then("it approves payment and accrues Starbucks coupons") {
                result.paymentId shouldBe PaymentId("pay_1")
                result.paymentStatus shouldBe PaymentStatus.AUTHORIZED
                result.amount shouldBe 10_000
                result.currency shouldBe "KRW"
                result.couponIds shouldBe listOf("starbucks_coupon_1", "starbucks_coupon_2")

                verify(exactly = 1) {
                    externalPaymentPort.approve(match { it.money.amount == 10_000L })
                }
                verify(exactly = 1) {
                    authorizePaymentUseCase.authorize(match { it.merchantId.value == "starbucks" })
                }
                verify(exactly = 1) {
                    accrueCouponPort.accrue(match { it.brand == "STARBUCKS" && it.quantity == 2 })
                }
            }
        }
    }
})
