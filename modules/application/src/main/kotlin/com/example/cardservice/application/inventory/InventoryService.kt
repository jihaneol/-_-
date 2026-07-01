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
    override fun createInventory(request: CreateInventoryRequest): InventoryResponse {
        productRepository.findByIdAndDeletedAtIsNull(request.productId) ?: throw IllegalArgumentException("상품을 찾을 수 없습니다.")
        return inventoryRepository.save(Inventory.create(request.productId, request.quantity)).toResponse()
    }

    @Transactional
    override fun increaseInventory(request: AdjustInventoryRequest): InventoryResponse {
        val inventory = loadInventory(request.productId)
        inventory.increase(request.quantity)
        return inventoryRepository.save(inventory).toResponse()
    }

    @Transactional
    override fun decreaseInventory(request: AdjustInventoryRequest): InventoryResponse {
        val inventory = loadInventory(request.productId)
        inventory.decrease(request.quantity)
        return inventoryRepository.save(inventory).toResponse()
    }

    private fun loadInventory(productId: Long): Inventory =
        inventoryRepository.findByProductId(productId) ?: throw IllegalArgumentException("재고를 찾을 수 없습니다.")
}

internal fun Inventory.toResponse(): InventoryResponse =
    InventoryResponse(id = id, productId = productId, quantity = quantity)
