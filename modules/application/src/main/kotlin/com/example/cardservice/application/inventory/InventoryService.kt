package com.example.cardservice.application.inventory

import com.example.cardservice.application.inventory.provided.InventoryRepository
import com.example.cardservice.application.product.provided.ProductRepository
import com.example.cardservice.application.inventory.required.InventoryUseCase
import com.example.cardservice.domain.inventory.Inventory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 재고 생성, 증감 흐름을 조율하는 application service다.
 */
@Service
class InventoryService(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
) : InventoryUseCase {
    @Transactional
    override fun createInventory(input: CreateInventoryInput): InventoryResult {
        productRepository.findByIdAndDeletedAtIsNull(input.productId) ?: throw IllegalArgumentException("상품을 찾을 수 없습니다.")
        return inventoryRepository.save(Inventory.create(input.productId, input.quantity)).toResult()
    }

    @Transactional
    override fun increaseInventory(productId: Long, input: AdjustInventoryInput): InventoryResult {
        val inventory = loadInventory(productId)
        inventory.increase(input.quantity)
        return inventoryRepository.save(inventory).toResult()
    }

    @Transactional
    override fun decreaseInventory(productId: Long, input: AdjustInventoryInput): InventoryResult {
        val inventory = loadInventory(productId)
        inventory.decrease(input.quantity)
        return inventoryRepository.save(inventory).toResult()
    }

    private fun loadInventory(productId: Long): Inventory =
        inventoryRepository.findByProductId(productId) ?: throw IllegalArgumentException("재고를 찾을 수 없습니다.")
}

internal fun Inventory.toResult(): InventoryResult =
    InventoryResult(id = requireNotNull(id), productId = productId, quantity = quantity)
