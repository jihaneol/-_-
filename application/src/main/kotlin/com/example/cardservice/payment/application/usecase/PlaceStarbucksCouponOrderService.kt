package com.example.cardservice.payment.application.usecase

import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentCommand
import com.example.cardservice.payment.application.port.`in`.AuthorizePaymentUseCase
import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderCommand
import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderResult
import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderUseCase
import com.example.cardservice.payment.application.port.out.AccrueCouponPort
import com.example.cardservice.payment.application.port.out.CouponAccrualCommand
import com.example.cardservice.payment.application.port.out.ExternalPaymentPort
import com.example.cardservice.payment.application.port.out.ExternalPaymentRequest
import com.example.cardservice.payment.domain.model.MerchantId
import com.example.cardservice.payment.domain.model.Money
import org.springframework.stereotype.Service

@Service
class PlaceStarbucksCouponOrderService(
    private val externalPaymentPort: ExternalPaymentPort,
    private val authorizePaymentUseCase: AuthorizePaymentUseCase,
    private val accrueCouponPort: AccrueCouponPort,
) : PlaceStarbucksCouponOrderUseCase {
    override fun place(command: PlaceStarbucksCouponOrderCommand): PlaceStarbucksCouponOrderResult {
        require(command.quantity > 0) { "quantity must be positive" }

        val money = Money(
            amount = STARBUCKS_COUPON_AMOUNT * command.quantity,
            currency = "KRW",
        )

        externalPaymentPort.approve(
            ExternalPaymentRequest(
                orderId = command.orderId,
                idempotencyKey = command.idempotencyKey,
                money = money,
            ),
        )

        val payment = authorizePaymentUseCase.authorize(
            AuthorizePaymentCommand(
                merchantId = STARBUCKS_MERCHANT_ID,
                orderId = command.orderId,
                idempotencyKey = command.idempotencyKey,
                money = money,
            ),
        )

        val coupons = accrueCouponPort.accrue(
            CouponAccrualCommand(
                customerId = command.customerId,
                orderId = command.orderId,
                brand = "STARBUCKS",
                quantity = command.quantity,
            ),
        )

        return PlaceStarbucksCouponOrderResult(
            orderId = command.orderId,
            paymentId = payment.paymentId,
            paymentStatus = payment.status,
            amount = payment.amount,
            currency = payment.currency,
            couponIds = coupons.couponIds,
        )
    }

    private companion object {
        const val STARBUCKS_COUPON_AMOUNT = 5_000L
        val STARBUCKS_MERCHANT_ID = MerchantId("starbucks")
    }
}
