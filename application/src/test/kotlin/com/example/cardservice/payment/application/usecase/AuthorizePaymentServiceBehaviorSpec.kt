package com.example.cardservice.payment.application.usecase

import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentCommand
import com.example.cardservice.payment.application.port.out.SavePaymentPort
import com.example.cardservice.payment.domain.model.IdempotencyKey
import com.example.cardservice.payment.domain.model.MerchantId
import com.example.cardservice.payment.domain.model.Money
import com.example.cardservice.payment.domain.model.OrderId
import com.example.cardservice.payment.domain.model.Payment
import com.example.cardservice.payment.domain.model.PaymentId
import com.example.cardservice.payment.domain.model.PaymentStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class AuthorizePaymentServiceBehaviorSpec : BehaviorSpec({
    given("a payment authorization command") {
        val savePaymentPort = mockk<SavePaymentPort>()
        val service = AuthorizePaymentService(savePaymentPort)
        val command = AuthorizePaymentCommand(
            merchantId = MerchantId("starbucks"),
            orderId = OrderId("order-1"),
            idempotencyKey = IdempotencyKey("idem-1"),
            money = Money(amount = 5_000, currency = "KRW"),
        )

        every { savePaymentPort.save(any()) } answers {
            firstArg<Payment>().copy(id = PaymentId("pay_1"))
        }

        `when`("authorization is handled") {
            val result = service.authorize(command)

            then("it saves an authorized payment through the outbound port") {
                result.paymentId shouldBe PaymentId("pay_1")
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
