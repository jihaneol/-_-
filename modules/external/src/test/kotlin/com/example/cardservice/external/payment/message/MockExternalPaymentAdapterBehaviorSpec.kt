package com.example.cardservice.external.payment.message

import com.example.cardservice.application.payment.ExternalPaymentRequest
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.Money
import com.example.cardservice.domain.payment.model.OrderId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import kotlin.system.measureTimeMillis

class MockExternalPaymentAdapterBehaviorSpec : BehaviorSpec({
    given("the mock external payment adapter") {
        val adapter = MockExternalPaymentAdapter()

        `when`("payment is approved") {
            var approvalKey = ""
            val elapsed = measureTimeMillis {
                approvalKey = adapter.approve(
                    ExternalPaymentRequest(
                        orderId = OrderId("order-1"),
                        idempotencyKey = IdempotencyKey("idem-1"),
                        money = Money(amount = 5_000, currency = "KRW"),
                    ),
                ).approvalKey
            }

            then("it behaves like a delayed external call") {
                approvalKey shouldBe "mock_order-1"
                elapsed shouldBeGreaterThanOrEqual 300L
            }
        }
    }
})
