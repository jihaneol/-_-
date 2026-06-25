package com.example.cardservice.web.payment

import com.example.cardservice.application.order.PayOrderInput
import com.example.cardservice.application.order.PayOrderResult
import com.example.cardservice.application.order.request.PayOrderRequest
import com.example.cardservice.application.order.required.OrderPaymentUseCase
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/shop/orders/{orderId}")
@Tag(name = "Shop Order Payment", description = "쇼핑몰 주문 결제 API")
class ShopOrderPaymentController(
    private val orderPaymentUseCase: OrderPaymentUseCase,
) {
    @PostMapping("/pay")
    @Operation(summary = "쇼핑몰 주문 결제")
    fun payOrder(
        @PathVariable orderId: Long,
        @RequestBody request: PayOrderRequest,
    ): ResponseEntity<ApiResponse<PayOrderResult>> =
        orderPaymentUseCase.payOrder(orderId, PayOrderInput(request.idempotencyKey)).toApplicationResponse()
}
