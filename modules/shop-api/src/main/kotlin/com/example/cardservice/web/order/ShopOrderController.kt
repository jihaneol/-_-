package com.example.cardservice.web.order

import com.example.cardservice.application.order.CreateOrderRequest
import com.example.cardservice.application.order.OrderResponse
import com.example.cardservice.application.order.required.OrderQueryUseCase
import com.example.cardservice.application.order.required.OrderUseCase
import com.example.cardservice.web.common.ApplicationResponseType
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/shop")
@Tag(name = "Shop Order", description = "쇼핑몰 주문 API")
class ShopOrderController(
    private val orderUseCase: OrderUseCase,
    private val orderQueryUseCase: OrderQueryUseCase,
) {
    @PostMapping("/orders")
    @Operation(summary = "쇼핑몰 주문 생성")
    fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<ApiResponse<OrderResponse>> =
        orderUseCase
            .createOrder(request)
            .toApplicationResponse(ApplicationResponseType.CREATED)

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "쇼핑몰 주문 상세 조회")
    fun getOrder(@PathVariable orderId: Long): ResponseEntity<ApiResponse<OrderResponse>> =
        orderQueryUseCase.getOrder(orderId).toApplicationResponse()
}
