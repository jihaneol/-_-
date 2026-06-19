package com.example.cardservice.web.shop

import com.example.cardservice.application.commerce.CreateOrderInput
import com.example.cardservice.application.commerce.CreateOrderLineInput
import com.example.cardservice.application.commerce.request.OrderCreateRequest
import com.example.cardservice.application.commerce.required.OrderQueryUseCase
import com.example.cardservice.application.commerce.required.OrderUseCase
import com.example.cardservice.application.commerce.response.toResponse
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
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
    fun createOrder(@RequestBody request: OrderCreateRequest): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    orderUseCase.createOrder(
                        CreateOrderInput(
                            memberId = request.memberId,
                            lines = request.lines.map { CreateOrderLineInput(it.productId, it.quantity) },
                        ),
                    ).toResponse(),
                ),
            )

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "쇼핑몰 주문 상세 조회")
    fun getOrder(@PathVariable orderId: Long): ApiResponse<Any> =
        ApiResponse.success(orderQueryUseCase.getOrder(orderId).toResponse())
}
