package com.example.cardservice.application.inventory.required

import com.example.cardservice.application.inventory.AdjustInventoryInput
import com.example.cardservice.application.inventory.CreateInventoryInput
import com.example.cardservice.application.inventory.InventoryResult

/**
 * 재고 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface InventoryUseCase {
    fun createInventory(input: CreateInventoryInput): InventoryResult
    fun increaseInventory(productId: Long, input: AdjustInventoryInput): InventoryResult
    fun decreaseInventory(productId: Long, input: AdjustInventoryInput): InventoryResult
}

/**
 * 재고 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface InventoryQueryUseCase {
    fun getInventory(productId: Long): InventoryResult
}
