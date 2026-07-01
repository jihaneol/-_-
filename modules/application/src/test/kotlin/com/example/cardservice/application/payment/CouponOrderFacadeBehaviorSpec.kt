package com.example.cardservice.application.payment

import com.example.cardservice.application.payment.required.AuthorizePaymentUseCase
import com.example.cardservice.application.payment.provided.AccrueCouponPort
import com.example.cardservice.application.payment.provided.ExternalPaymentPort
import com.example.cardservice.domain.payment.model.CustomerId
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.PaymentId
import com.example.cardservice.domain.payment.model.PaymentStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class CouponOrderFacadeBehaviorSpec : BehaviorSpec({
    given("a coupon order") {
        val externalPaymentPort = mockk<ExternalPaymentPort>()
        val authorizePaymentUseCase = mockk<AuthorizePaymentUseCase>()
        val accrueCouponPort = mockk<AccrueCouponPort>()
        val facade = CouponOrderFacade(
            externalPaymentPort = externalPaymentPort,
            authorizePaymentUseCase = authorizePaymentUseCase,
            accrueCouponPort = accrueCouponPort,
        )
        val input = CreateCouponOrderRequest(
            customerId = 1L,
            orderId = 10L,
            idempotencyKey = "idem-1",
            quantity = 2,
        )

        every { externalPaymentPort.approve(any()) } returns ExternalPaymentApproval("mock-order-1")
        every { authorizePaymentUseCase.authorize(any()) } answers {
            val authorizeInput = firstArg<AuthorizePaymentRequest>()
            AuthorizePaymentResponse(
                paymentId = PaymentId(1),
                status = PaymentStatus.AUTHORIZED,
                amount = authorizeInput.money.amount,
                currency = authorizeInput.money.currency,
            )
        }
        every { accrueCouponPort.accrue(any()) } returns CouponAccrualResponse(
            couponIds = listOf("coupon_1", "coupon_2"),
        )

        `when`("the order is created") {
            val result = facade.create(input)

            then("it approves payment and accrues coupons") {
                result.paymentId shouldBe 1L
                result.paymentStatus shouldBe PaymentStatus.AUTHORIZED.name
                result.amount shouldBe 10_000
                result.currency shouldBe "KRW"
                result.couponIds shouldBe listOf("coupon_1", "coupon_2")

                verify(exactly = 1) {
                    externalPaymentPort.approve(match { it.money.amount == 10_000L })
                }
                verify(exactly = 1) {
                    authorizePaymentUseCase.authorize(match { it.merchantId.value == 2L })
                }
                verify(exactly = 1) {
                    accrueCouponPort.accrue(match { it.brand == "COUPON" && it.quantity == 2 })
                }
            }
        }
    }
})
