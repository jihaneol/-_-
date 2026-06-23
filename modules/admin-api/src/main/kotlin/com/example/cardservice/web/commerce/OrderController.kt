package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.CreateOrderInput
import com.example.cardservice.application.commerce.CreateOrderLineInput
import com.example.cardservice.application.commerce.OrderResult
import com.example.cardservice.application.commerce.request.OrderCreateRequest
import com.example.cardservice.application.commerce.required.OrderQueryUseCase
import com.example.cardservice.application.commerce.required.OrderUseCase
import com.example.cardservice.application.commerce.response.OrderListResponse
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.created
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Order", description = "주문 운영 API")
class OrderController(
    private val orderUseCase: OrderUseCase,
    private val orderQueryUseCase: OrderQueryUseCase,
) {
    @PostMapping
    @Operation(summary = "주문 생성")
    fun createOrder(@RequestBody request: OrderCreateRequest): ResponseEntity<ApiResponse<OrderResult>> =
        created(
            orderUseCase.createOrder(
                CreateOrderInput(
                    memberId = request.memberId,
                    lines = request.lines.map { CreateOrderLineInput(it.productId, it.quantity) },
                ),
            ),
        )

    @GetMapping
    @Operation(summary = "주문 목록 조회")
    fun listOrders(): ApiResponse<OrderListResponse> =
        ApiResponse.success(OrderListResponse(orderQueryUseCase.listOrders()))

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회")
    fun getOrder(@PathVariable orderId: Long): ApiResponse<OrderResult> =
        ApiResponse.success(orderQueryUseCase.getOrder(orderId))

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 소프트 삭제")
    fun deleteOrder(@PathVariable orderId: Long): ResponseEntity<Void> {
        orderUseCase.deleteOrder(orderId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "결제 전 주문 취소")
    fun cancelOrder(@PathVariable orderId: Long): ApiResponse<OrderResult> =
        ApiResponse.success(orderUseCase.cancelOrder(orderId))
}
