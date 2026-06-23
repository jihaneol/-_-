package com.example.cardservice.infra.commerce

import com.example.cardservice.application.commerce.CouponHistoryPageResult
import com.example.cardservice.application.commerce.CouponHistoryResult
import com.example.cardservice.application.commerce.CouponPageQuery
import com.example.cardservice.application.commerce.CouponPageResult
import com.example.cardservice.application.commerce.CouponResult
import com.example.cardservice.application.commerce.MemberCouponHistoryPageQuery
import com.example.cardservice.application.commerce.OrderCouponHistoryPageQuery
import com.example.cardservice.application.commerce.SortDirection
import com.example.cardservice.application.commerce.provided.CouponQueryPort
import com.example.cardservice.domain.commerce.model.Coupon
import com.example.cardservice.domain.commerce.model.CouponHistory
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

/**
 * 쿠폰 조회 전용 페이지 쿼리를 QueryDSL로 수행하는 read adapter다.
 */
@Component
class QueryDslCouponQueryAdapter(
    entityManager: EntityManager,
) : CouponQueryPort {
    private val queryFactory = JPAQueryFactory(entityManager)
    private val coupon = PathBuilder(Coupon::class.java, "coupon")
    private val couponHistory = PathBuilder(CouponHistory::class.java, "couponHistory")

    override fun searchCoupons(query: CouponPageQuery): CouponPageResult {
        val memberId = coupon.getNumber("memberId", Long::class.javaObjectType)
        val id = coupon.getNumber("id", Long::class.javaObjectType)
        val items = queryFactory
            .selectFrom(coupon)
            .where(memberId.eq(query.memberId))
            .orderBy(OrderSpecifier(orderOf(query.sortDirection), id))
            .offset(query.offset)
            .limit(query.normalizedSize.toLong())
            .fetch()
            .map { it.toCouponResult() }
        val totalElements = queryFactory
            .select(coupon.count())
            .from(coupon)
            .where(memberId.eq(query.memberId))
            .fetchOne() ?: 0L

        return CouponPageResult(
            items = items,
            page = query.normalizedPage,
            size = query.normalizedSize,
            totalElements = totalElements,
            totalPages = totalPages(totalElements, query.normalizedSize),
            hasNext = hasNext(query.normalizedPage, query.normalizedSize, totalElements),
        )
    }

    override fun searchMemberCouponHistories(query: MemberCouponHistoryPageQuery): CouponHistoryPageResult {
        val memberId = couponHistory.getNumber("memberId", Long::class.javaObjectType)
        return searchHistories(
            page = query.normalizedPage,
            size = query.normalizedSize,
            direction = query.sortDirection,
            where = memberId.eq(query.memberId),
        )
    }

    override fun searchOrderCouponHistories(query: OrderCouponHistoryPageQuery): CouponHistoryPageResult {
        val orderId = couponHistory.getNumber("orderId", Long::class.javaObjectType)
        return searchHistories(
            page = query.normalizedPage,
            size = query.normalizedSize,
            direction = query.sortDirection,
            where = orderId.eq(query.orderId),
        )
    }

    private fun searchHistories(
        page: Int,
        size: Int,
        direction: SortDirection,
        where: com.querydsl.core.types.Predicate,
    ): CouponHistoryPageResult {
        val id = couponHistory.getNumber("id", Long::class.javaObjectType)
        val items = queryFactory
            .selectFrom(couponHistory)
            .where(where)
            .orderBy(OrderSpecifier(orderOf(direction), id))
            .offset(page.toLong() * size)
            .limit(size.toLong())
            .fetch()
            .map { it.toCouponHistoryResult() }
        val totalElements = queryFactory
            .select(couponHistory.count())
            .from(couponHistory)
            .where(where)
            .fetchOne() ?: 0L

        return CouponHistoryPageResult(
            items = items,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages(totalElements, size),
            hasNext = hasNext(page, size, totalElements),
        )
    }

    private val CouponPageQuery.offset: Long
        get() = normalizedPage.toLong() * normalizedSize

    private fun orderOf(direction: SortDirection): Order =
        if (direction == SortDirection.ASC) Order.ASC else Order.DESC

    private fun totalPages(totalElements: Long, size: Int): Int =
        if (totalElements == 0L) 0 else ((totalElements + size - 1) / size).toInt()

    private fun hasNext(page: Int, size: Int, totalElements: Long): Boolean =
        (page + 1).toLong() * size < totalElements
}

private fun Coupon.toCouponResult(): CouponResult =
    CouponResult(
        id = requireNotNull(id),
        memberId = memberId,
        orderId = orderId,
        paymentId = paymentId,
        status = status,
    )

private fun CouponHistory.toCouponHistoryResult(): CouponHistoryResult =
    CouponHistoryResult(
        id = requireNotNull(id),
        couponId = couponId,
        memberId = memberId,
        orderId = orderId,
        paymentId = paymentId,
        type = type,
    )
