package com.example.cardservice.web.order

import com.example.cardservice.application.order.PayOrderRequest
import com.example.cardservice.application.order.PayOrderResponse
import com.example.cardservice.application.order.RefundOrderResponse
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
@RequestMapping("/api/admin/orders/{orderId}")
@Tag(name = "Order Payment", description = "주문 결제와 전체 환불 API")
class OrderPaymentController(
    private val orderPaymentUseCase: OrderPaymentUseCase,
) {
    @PostMapping("/pay")
    @Operation(summary = "주문 결제")
    fun payOrder(
        @PathVariable orderId: Long,
        @RequestBody request: PayOrderRequest,
    ): ResponseEntity<ApiResponse<PayOrderResponse>> =
        orderPaymentUseCase.payOrder(request.copy().also { it.orderId = orderId }).toApplicationResponse()

    @PostMapping("/refund")
    @Operation(summary = "주문 전체 환불")
    fun refundOrder(@PathVariable orderId: Long): ResponseEntity<ApiResponse<RefundOrderResponse>> =
        orderPaymentUseCase.refundOrder(orderId).toApplicationResponse()
}
