package com.example.cardservice.application.inventory.required

import com.example.cardservice.application.inventory.AdjustInventoryRequest
import com.example.cardservice.application.inventory.CreateInventoryRequest
import com.example.cardservice.application.inventory.InventoryResponse

/**
 * 재고 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface InventoryUseCase {
    fun createInventory(request: CreateInventoryRequest): InventoryResponse
    fun increaseInventory(request: AdjustInventoryRequest): InventoryResponse
    fun decreaseInventory(request: AdjustInventoryRequest): InventoryResponse
}

/**
 * 재고 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface InventoryQueryUseCase {
    fun getInventory(productId: Long): InventoryResponse
}
