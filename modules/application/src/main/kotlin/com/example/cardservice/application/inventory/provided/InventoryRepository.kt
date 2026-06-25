package com.example.cardservice.application.inventory.provided

import com.example.cardservice.domain.inventory.Inventory
import org.springframework.data.repository.Repository

/**
 * Inventory entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface InventoryRepository : Repository<Inventory, Long> {
    fun save(inventory: Inventory): Inventory
    fun findByProductId(productId: Long): Inventory?
}
