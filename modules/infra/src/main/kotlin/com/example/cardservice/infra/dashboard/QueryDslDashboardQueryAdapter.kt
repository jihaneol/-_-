package com.example.cardservice.infra.dashboard

import com.example.cardservice.application.dashboard.DashboardSummaryResponse
import com.example.cardservice.application.dashboard.provided.DashboardQueryPort
import com.example.cardservice.domain.coupon.CouponStatus
import com.example.cardservice.domain.coupon.QCoupon.coupon
import com.example.cardservice.domain.member.QMember.member
import com.example.cardservice.domain.order.OrderStatus
import com.example.cardservice.domain.order.QOrder.order
import com.example.cardservice.domain.product.QProduct.product
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class QueryDslDashboardQueryAdapter(
    entityManager: EntityManager,
) : DashboardQueryPort {
    private val queryFactory = JPAQueryFactory(entityManager)

    override fun getSummary(): DashboardSummaryResponse {
        val memberCount = countActiveMembers()
        val productCount = countActiveProducts()
        val orderCounts = countActiveOrdersByStatus()
        val issuedCouponCount = countIssuedCoupons()

        return DashboardSummaryResponse(
            memberCount = memberCount,
            productCount = productCount,
            orderCount = orderCounts.total,
            paidOrderCount = orderCounts.paid,
            refundedOrderCount = orderCounts.refunded,
            issuedCouponCount = issuedCouponCount,
        )
    }

    private fun countActiveMembers(): Long =
        queryFactory
            .select(member.id.count())
            .from(member)
            .where(member.deletedAt.isNull)
            .fetchOne() ?: 0L

    private fun countActiveProducts(): Long =
        queryFactory
            .select(product.id.count())
            .from(product)
            .where(product.deletedAt.isNull)
            .fetchOne() ?: 0L

    private fun countActiveOrdersByStatus(): OrderDashboardCounts {
        val paidCount = CaseBuilder()
            .`when`(order.status.eq(OrderStatus.PAID))
            .then(1L)
            .otherwise(0L)
            .sum()
        val refundedCount = CaseBuilder()
            .`when`(order.status.eq(OrderStatus.REFUNDED))
            .then(1L)
            .otherwise(0L)
            .sum()

        val row = queryFactory
            .select(order.id.count(), paidCount, refundedCount)
            .from(order)
            .where(order.deletedAt.isNull)
            .fetchOne()

        return OrderDashboardCounts(
            total = row?.get(order.id.count()) ?: 0L,
            paid = row?.get(paidCount) ?: 0L,
            refunded = row?.get(refundedCount) ?: 0L,
        )
    }

    private fun countIssuedCoupons(): Long =
        queryFactory
            .select(coupon.id.count())
            .from(coupon)
            .where(coupon.status.eq(CouponStatus.ISSUED))
            .fetchOne() ?: 0L
}

private data class OrderDashboardCounts(
    val total: Long,
    val paid: Long,
    val refunded: Long,
)
