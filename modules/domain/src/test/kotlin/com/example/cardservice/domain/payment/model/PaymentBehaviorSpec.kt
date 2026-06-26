package com.example.cardservice.domain.payment.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PaymentBehaviorSpec : BehaviorSpec({
    given("valid payment authorization values") {
        `when`("a payment is authorized") {
            then("it creates an authorized payment with domain rules") {
                val payment = Payment.authorize(
                    merchantId = MerchantId(2L),
                    orderId = OrderId(10L),
                    idempotencyKey = IdempotencyKey("idem-1"),
                    money = Money(amount = 5_000, currency = "KRW"),
                )

                payment.id shouldBe 0L
                payment.status shouldBe PaymentStatus.AUTHORIZED
                payment.money.amount shouldBe 5_000
            }
        }
    }
})
