package com.example.cardservice.application.inventory

import com.example.cardservice.application.inventory.provided.InventoryRepository
import com.example.cardservice.application.inventory.required.InventoryQueryUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 재고 조회 흐름을 조율하는 application query service다.
 */
@Service
class InventoryQueryService(
    private val inventoryRepository: InventoryRepository,
) : InventoryQueryUseCase {
    @Transactional(readOnly = true)
    override fun getInventory(productId: Long): InventoryResponse =
        (inventoryRepository.findByProductId(productId) ?: throw IllegalArgumentException("재고를 찾을 수 없습니다."))
            .toResponse()
}
