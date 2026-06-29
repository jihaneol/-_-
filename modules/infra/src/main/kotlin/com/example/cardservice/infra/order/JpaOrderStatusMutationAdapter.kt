package com.example.cardservice.infra.order

import com.example.cardservice.application.order.provided.OrderStatusMutationPort
import com.example.cardservice.domain.order.OrderStatus
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

/**
 * 주문 결제 상태 전이를 단일 조건부 update로 수행한다.
 */
@Component
class JpaOrderStatusMutationAdapter(
    private val entityManager: EntityManager,
) : OrderStatusMutationPort {
    override fun markPaidIfCreated(orderId: Long, paymentId: Long): Boolean {
        val updatedCount = entityManager
            .createQuery(
                """
                update Order o
                set o.status = :paidStatus,
                    o.paymentId = :paymentId
                where o.id = :orderId
                  and o.status = :createdStatus
                  and o.deletedAt is null
                """.trimIndent(),
            )
            .setParameter("paidStatus", OrderStatus.PAID)
            .setParameter("paymentId", paymentId)
            .setParameter("orderId", orderId)
            .setParameter("createdStatus", OrderStatus.CREATED)
            .executeUpdate()
        return updatedCount == 1
    }
}
