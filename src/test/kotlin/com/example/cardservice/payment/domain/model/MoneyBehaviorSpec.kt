package com.example.cardservice.payment.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class MoneyBehaviorSpec : BehaviorSpec({
    given("a positive amount and currency") {
        `when`("money is created") {
            then("it keeps the amount and currency") {
                val money = Money(amount = 1_000, currency = "KRW")

                money.amount shouldBe 1_000
                money.currency shouldBe "KRW"
            }
        }
    }

    given("a non-positive amount") {
        `when`("money is created") {
            then("it rejects the value") {
                shouldThrow<IllegalArgumentException> {
                    Money(amount = 0, currency = "KRW")
                }
            }
        }
    }
})
