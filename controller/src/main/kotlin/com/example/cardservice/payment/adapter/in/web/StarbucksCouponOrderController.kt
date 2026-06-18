package com.example.cardservice.payment.adapter.`in`.web

import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderCommand
import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderUseCase
import com.example.cardservice.payment.domain.model.CustomerId
import com.example.cardservice.payment.domain.model.IdempotencyKey
import com.example.cardservice.payment.domain.model.OrderId
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/starbucks-coupon-orders")
class StarbucksCouponOrderController(
    private val placeStarbucksCouponOrderUseCase: PlaceStarbucksCouponOrderUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun place(@Valid @RequestBody request: StarbucksCouponOrderRequest): StarbucksCouponOrderResponse {
        val result = placeStarbucksCouponOrderUseCase.place(
            PlaceStarbucksCouponOrderCommand(
                customerId = CustomerId(request.customerId),
                orderId = OrderId(request.orderId),
                idempotencyKey = IdempotencyKey(request.idempotencyKey),
                quantity = request.quantity,
            ),
        )

        return StarbucksCouponOrderResponse(
            orderId = result.orderId.value,
            paymentId = result.paymentId.value,
            paymentStatus = result.paymentStatus.name,
            amount = result.amount,
            currency = result.currency,
            couponIds = result.couponIds,
        )
    }
}

data class StarbucksCouponOrderRequest(
    @field:NotBlank
    val customerId: String,
    @field:NotBlank
    val orderId: String,
    @field:NotBlank
    val idempotencyKey: String,
    @field:Min(1)
    val quantity: Int,
)

data class StarbucksCouponOrderResponse(
    val orderId: String,
    val paymentId: String,
    val paymentStatus: String,
    val amount: Long,
    val currency: String,
    val couponIds: List<String>,
)
