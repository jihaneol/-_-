package com.example.cardservice.infra.inventory

import com.example.cardservice.application.inventory.provided.InventoryMutationPort
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

/**
 * 결제/환불 경로의 재고 수량 변경을 단일 SQL update로 수행한다.
 */
@Component
class JpaInventoryMutationAdapter(
    private val entityManager: EntityManager,
) : InventoryMutationPort {
    override fun decreaseQuantityIfEnough(productId: Long, quantity: Long): Boolean {
        val updatedCount = entityManager
            .createQuery(
                """
                update Inventory i
                set i.quantity = i.quantity - :quantity
                where i.productId = :productId
                  and i.quantity >= :quantity
                """.trimIndent(),
            )
            .setParameter("productId", productId)
            .setParameter("quantity", quantity)
            .executeUpdate()
        return updatedCount == 1
    }

    override fun increaseQuantity(productId: Long, quantity: Long): Boolean {
        val updatedCount = entityManager
            .createQuery(
                """
                update Inventory i
                set i.quantity = i.quantity + :quantity
                where i.productId = :productId
                """.trimIndent(),
            )
            .setParameter("productId", productId)
            .setParameter("quantity", quantity)
            .executeUpdate()
        return updatedCount == 1
    }
}
