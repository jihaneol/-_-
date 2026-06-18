package com.example.cardservice.payment.adapter.out.message

import com.example.cardservice.payment.application.port.out.ExternalPaymentRequest
import com.example.cardservice.payment.domain.model.IdempotencyKey
import com.example.cardservice.payment.domain.model.Money
import com.example.cardservice.payment.domain.model.OrderId
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
