package com.example.cardservice.application.payment

import com.example.cardservice.application.payment.provided.SavePaymentPort
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.MerchantId
import com.example.cardservice.domain.payment.model.Money
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.Payment
import com.example.cardservice.domain.payment.model.PaymentId
import com.example.cardservice.domain.payment.model.PaymentStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class AuthorizePaymentServiceBehaviorSpec : BehaviorSpec({
    given("a payment authorization input") {
        val savePaymentPort = mockk<SavePaymentPort>()
        val service = AuthorizePaymentService(savePaymentPort)
        val input = AuthorizePaymentInput(
            merchantId = MerchantId(2L),
            orderId = OrderId(10L),
            idempotencyKey = IdempotencyKey("idem-1"),
            money = Money(amount = 5_000, currency = "KRW"),
        )

        every { savePaymentPort.save(any()) } answers {
            firstArg<Payment>().apply {
                assignId(PaymentId(1))
            }
        }

        `when`("authorization is handled") {
            val result = service.authorize(input)

            then("it saves an authorized payment through the outbound port") {
                result.paymentId shouldBe PaymentId(1)
                result.status shouldBe PaymentStatus.AUTHORIZED
                result.amount shouldBe 5_000
                result.currency shouldBe "KRW"

                verify(exactly = 1) {
                    savePaymentPort.save(match { it.status == PaymentStatus.AUTHORIZED })
                }
            }
        }
    }
})
