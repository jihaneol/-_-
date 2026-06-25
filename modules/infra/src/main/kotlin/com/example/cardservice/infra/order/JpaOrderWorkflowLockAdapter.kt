package com.example.cardservice.infra.order

import com.example.cardservice.application.order.provided.OrderWorkflowLockPort
import com.example.cardservice.domain.order.Order
import com.example.cardservice.domain.coupon.Coupon
import com.example.cardservice.domain.coupon.CouponStatus
import com.example.cardservice.domain.inventory.Inventory
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.springframework.stereotype.Component

/**
 * OrderWorkflowLockPort를 JPA pessimistic write lock 조회로 구현하는 persistence adapter다.
 */
@Component
class JpaOrderWorkflowLockAdapter(
    private val entityManager: EntityManager,
) : OrderWorkflowLockPort {
    override fun loadOrderForUpdate(orderId: Long): Order? =
        entityManager.find(Order::class.java, orderId, LockModeType.PESSIMISTIC_WRITE)
            ?.takeIf { it.deletedAt == null }

    override fun loadInventoryForUpdate(productId: Long): Inventory? =
        entityManager
            .createQuery(
                "select i from Inventory i where i.productId = :productId",
                Inventory::class.java,
            )
            .setParameter("productId", productId)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .resultList
            .firstOrNull()

    override fun loadCouponForUpdate(couponId: Long): Coupon? =
        entityManager.find(Coupon::class.java, couponId, LockModeType.PESSIMISTIC_WRITE)

    override fun loadIssuedCouponsForExchange(memberId: Long, limit: Int): List<Coupon> =
        entityManager
            .createQuery(
                """
                select c
                from Coupon c
                where c.memberId = :memberId
                  and c.status = :status
                order by c.id asc
                """.trimIndent(),
                Coupon::class.java,
            )
            .setParameter("memberId", memberId)
            .setParameter("status", CouponStatus.ISSUED)
            .setMaxResults(limit)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .resultList
}
