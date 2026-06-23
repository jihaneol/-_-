package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.AdjustInventoryInput
import com.example.cardservice.application.commerce.CreateInventoryInput
import com.example.cardservice.application.commerce.InventoryResult
import com.example.cardservice.application.commerce.request.InventoryRequest
import com.example.cardservice.application.commerce.required.InventoryQueryUseCase
import com.example.cardservice.application.commerce.required.InventoryUseCase
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.created
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
        @RequestBody request: InventoryRequest,
    ): ResponseEntity<ApiResponse<InventoryResult>> =
        created(inventoryUseCase.createInventory(CreateInventoryInput(productId, request.quantity)))

    @GetMapping
    @Operation(summary = "현재 재고 조회")
    fun getInventory(@PathVariable productId: Long): ApiResponse<InventoryResult> =
        ApiResponse.success(inventoryQueryUseCase.getInventory(productId))

    @PostMapping("/increase")
    @Operation(summary = "재고 증가")
    fun increaseInventory(
        @PathVariable productId: Long,
        @RequestBody request: InventoryRequest,
    ): ApiResponse<InventoryResult> =
        ApiResponse.success(inventoryUseCase.increaseInventory(productId, AdjustInventoryInput(request.quantity)))

    @PostMapping("/decrease")
    @Operation(summary = "재고 차감")
    fun decreaseInventory(
        @PathVariable productId: Long,
        @RequestBody request: InventoryRequest,
    ): ApiResponse<InventoryResult> =
        ApiResponse.success(inventoryUseCase.decreaseInventory(productId, AdjustInventoryInput(request.quantity)))
}
