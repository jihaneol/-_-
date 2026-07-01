package com.example.cardservice.application.payment

import com.example.cardservice.application.payment.required.AuthorizePaymentUseCase
import com.example.cardservice.application.payment.required.CouponOrderUseCase
import com.example.cardservice.application.payment.provided.AccrueCouponPort
import com.example.cardservice.application.payment.provided.ExternalPaymentPort
import com.example.cardservice.domain.payment.model.CustomerId
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.MerchantId
import com.example.cardservice.domain.payment.model.Money
import com.example.cardservice.domain.payment.model.OrderId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
        // 검증
        require(input.quantity > 0) { "쿠폰 수량은 1개 이상이어야 합니다." }

        val money = Money(
            amount = COUPON_AMOUNT * input.quantity,
            currency = "KRW",
        )

        // 결제 승인
        externalPaymentPort.approve(
            ExternalPaymentRequest(
                orderId = OrderId(input.orderId),
                idempotencyKey = IdempotencyKey(input.idempotencyKey),
                money = money,
            ),
        )

        // 결제
        val payment = authorizePaymentUseCase.authorize(
            AuthorizePaymentRequest(
                merchantId = COUPON_MERCHANT_ID,
                orderId = OrderId(input.orderId),
                idempotencyKey = IdempotencyKey(input.idempotencyKey),
                money = money,
            ),
        )

        // 쿠폰 적립
        val coupons = accrueCouponPort.accrue(
            CouponAccrualRequest(
                customerId = CustomerId(input.customerId),
                orderId = OrderId(input.orderId),
                brand = "COUPON",
                quantity = input.quantity,
            ),
        )

        return CreateCouponOrderResponse(
            orderId = input.orderId,
            paymentId = payment.paymentId.value,
            paymentStatus = payment.status.name,
            paymentStatusLabel = payment.status.label,
            amount = payment.amount,
            currency = payment.currency,
            couponIds = coupons.couponIds,
        )
    }

    private companion object {
        const val COUPON_AMOUNT = 5_000L
        val COUPON_MERCHANT_ID = MerchantId(2L)
    }
}
