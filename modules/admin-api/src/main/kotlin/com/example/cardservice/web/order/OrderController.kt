package com.example.cardservice.web.order

import com.example.cardservice.application.common.DEFAULT_PAGE_SIZE
import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.order.CreateOrderInput
import com.example.cardservice.application.order.CreateOrderItemInput
import com.example.cardservice.application.order.OrderPageResult
import com.example.cardservice.application.order.OrderResult
import com.example.cardservice.application.order.request.OrderCreateRequest
import com.example.cardservice.application.order.required.OrderQueryUseCase
import com.example.cardservice.application.order.required.OrderUseCase
import com.example.cardservice.web.common.ApplicationResponseType
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
        orderUseCase
            .createOrder(
                CreateOrderInput(
                    memberId = request.memberId,
                    lines = request.lines.map { CreateOrderItemInput(it.productId, it.quantity) },
                ),
            )
            .toApplicationResponse(ApplicationResponseType.CREATED)

    @GetMapping
    @Operation(summary = "주문 목록 조회")
    fun listOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ResponseEntity<ApiResponse<OrderPageResult>> =
        orderQueryUseCase.listOrders(Pagination(page, size, sort)).toApplicationResponse()

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회")
    fun getOrder(@PathVariable orderId: Long): ResponseEntity<ApiResponse<OrderResult>> =
        orderQueryUseCase.getOrder(orderId).toApplicationResponse()

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 소프트 삭제")
    fun deleteOrder(@PathVariable orderId: Long): ResponseEntity<ApiResponse<Unit>> {
        orderUseCase.deleteOrder(orderId)
        return Unit.toApplicationResponse(ApplicationResponseType.NO_CONTENT)
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "결제 전 주문 취소")
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<ApiResponse<OrderResult>> =
        orderUseCase.cancelOrder(orderId).toApplicationResponse()
}
