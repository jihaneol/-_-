# Service Code Examples

이 문서는 `rules/service-code-rule.md`의 긴 복사용 예시다. 평소에는 규칙 파일만 읽고, 새 service를 만들 때 이 파일을 참고한다.

## Required Port Example

```kotlin
package com.example.cardservice.application.payment.required

import com.example.cardservice.application.payment.AuthorizePaymentRequest
import com.example.cardservice.application.payment.AuthorizePaymentResponse

interface AuthorizePaymentUseCase {
    fun authorize(input: AuthorizePaymentRequest): AuthorizePaymentResponse
}
```

## Request And Response Example

```kotlin
package com.example.cardservice.application.payment

import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.MerchantId
import com.example.cardservice.domain.payment.model.Money
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.PaymentId
import com.example.cardservice.domain.payment.model.PaymentStatus

data class AuthorizePaymentRequest(
    val merchantId: MerchantId,
    val orderId: OrderId,
    val idempotencyKey: IdempotencyKey,
    val money: Money,
)

data class AuthorizePaymentResponse(
    val paymentId: PaymentId,
    val status: PaymentStatus,
    val amount: Long,
    val currency: String,
)
```

## Service Example

```kotlin
package com.example.cardservice.application.payment

import com.example.cardservice.application.payment.provided.SavePaymentPort
import com.example.cardservice.application.payment.required.AuthorizePaymentUseCase
import com.example.cardservice.domain.payment.model.Payment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 도메인 결제 승인 규칙을 실행하고 승인된 결제 aggregate를 저장하는 application service다.
 */
@Service
class AuthorizePaymentService(
    private val savePaymentPort: SavePaymentPort,
) : AuthorizePaymentUseCase {
    @Transactional
    override fun authorize(input: AuthorizePaymentRequest): AuthorizePaymentResponse {
        val payment = Payment.authorize(
            merchantId = input.merchantId,
            orderId = input.orderId,
            idempotencyKey = input.idempotencyKey,
            money = input.money,
        )
        val savedPayment = savePaymentPort.save(payment)
        val savedMoney = savedPayment.money

        return AuthorizePaymentResponse(
            paymentId = requireNotNull(savedPayment.paymentId) { "저장된 결제에는 결제 ID가 있어야 합니다." },
            status = savedPayment.status,
            amount = savedMoney.amount,
            currency = savedMoney.currency,
        )
    }
}
```

## Orchestration Facade Example

```kotlin
/**
 * 쿠폰 주문 생성 흐름에서 외부 결제 승인, 결제 저장, 쿠폰 적립을 순서대로 조율하는 application facade다.
 */
@Service
class CouponOrderFacade(
    private val externalPaymentPort: ExternalPaymentPort,
    private val authorizePaymentUseCase: AuthorizePaymentUseCase,
    private val accrueCouponPort: AccrueCouponPort,
) : CouponOrderUseCase {
    @Transactional
    override fun create(input: CreateCouponOrderRequest): CreateCouponOrderResponse {
        require(input.quantity > 0) { "쿠폰 수량은 1개 이상이어야 합니다." }

        val money = Money(
            amount = COUPON_AMOUNT * input.quantity,
            currency = "KRW",
        )

        externalPaymentPort.approve(
            ExternalPaymentRequest(
                orderId = input.orderId,
                idempotencyKey = input.idempotencyKey,
                money = money,
            ),
        )

        val payment = authorizePaymentUseCase.authorize(
            AuthorizePaymentRequest(
                merchantId = COUPON_MERCHANT_ID,
                orderId = input.orderId,
                idempotencyKey = input.idempotencyKey,
                money = money,
            ),
        )

        val coupons = accrueCouponPort.accrue(
            CouponAccrualRequest(
                customerId = input.customerId,
                orderId = input.orderId,
                brand = "COUPON",
                quantity = input.quantity,
            ),
        )

        return CreateCouponOrderResponse(
            orderId = input.orderId,
            paymentId = payment.paymentId,
            paymentStatus = payment.status,
            amount = payment.amount,
            currency = payment.currency,
            couponIds = coupons.couponIds,
        )
    }
}
```

## Service Test Example

```kotlin
class AuthorizePaymentServiceBehaviorSpec : BehaviorSpec({
    given("a payment authorization input") {
        val savePaymentPort = mockk<SavePaymentPort>()
        val service = AuthorizePaymentService(savePaymentPort)
        val input = AuthorizePaymentRequest(
            merchantId = MerchantId("coupon-merchant"),
            orderId = OrderId("order-1"),
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

            then("it saves an authorized payment through the provided port") {
                result.paymentId shouldBe PaymentId(1)
                result.status shouldBe PaymentStatus.AUTHORIZED

                verify(exactly = 1) {
                    savePaymentPort.save(match { it.status == PaymentStatus.AUTHORIZED })
                }
            }
        }
    }
})
```
