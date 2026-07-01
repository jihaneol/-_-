package com.example.cardservice.web.inventory

import com.example.cardservice.application.inventory.AdjustInventoryRequest
import com.example.cardservice.application.inventory.CreateInventoryRequest
import com.example.cardservice.application.inventory.InventoryResponse
import com.example.cardservice.application.inventory.required.InventoryQueryUseCase
import com.example.cardservice.application.inventory.required.InventoryUseCase
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
@RequestMapping("/api/admin/products/{productId}/inventory")
@Tag(name = "Inventory", description = "재고 운영 API")
class InventoryController(
    private val inventoryUseCase: InventoryUseCase,
    private val inventoryQueryUseCase: InventoryQueryUseCase,
) {
    @PostMapping
    @Operation(summary = "초기 재고 생성")
    fun createInventory(
        @PathVariable productId: Long,
        @RequestBody request: CreateInventoryRequest,
    ): ResponseEntity<ApiResponse<InventoryResponse>> =
        inventoryUseCase
            .createInventory(request.copy().also { it.productId = productId })
            .toApplicationResponse(ApplicationResponseType.CREATED)

    @GetMapping
    @Operation(summary = "현재 재고 조회")
    fun getInventory(@PathVariable productId: Long): ResponseEntity<ApiResponse<InventoryResponse>> =
        inventoryQueryUseCase.getInventory(productId).toApplicationResponse()

    @PostMapping("/increase")
    @Operation(summary = "재고 증가")
    fun increaseInventory(
        @PathVariable productId: Long,
        @RequestBody request: AdjustInventoryRequest,
    ): ResponseEntity<ApiResponse<InventoryResponse>> =
        inventoryUseCase
            .increaseInventory(request.copy().also { it.productId = productId })
            .toApplicationResponse()

    @PostMapping("/decrease")
    @Operation(summary = "재고 차감")
    fun decreaseInventory(
        @PathVariable productId: Long,
        @RequestBody request: AdjustInventoryRequest,
    ): ResponseEntity<ApiResponse<InventoryResponse>> =
        inventoryUseCase
            .decreaseInventory(request.copy().also { it.productId = productId })
            .toApplicationResponse()
}
