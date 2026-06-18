package com.example.cardservice.payment.domain.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PaymentBehaviorSpec : BehaviorSpec({
    given("valid payment authorization values") {
        `when`("a payment is authorized") {
            then("it creates an authorized payment without infrastructure dependencies") {
                val payment = Payment.authorize(
                    merchantId = MerchantId("starbucks"),
                    orderId = OrderId("order-1"),
                    idempotencyKey = IdempotencyKey("idem-1"),
                    money = Money(amount = 5_000, currency = "KRW"),
                )

                payment.id shouldBe null
                payment.status shouldBe PaymentStatus.AUTHORIZED
                payment.money.amount shouldBe 5_000
            }
        }
    }
})
