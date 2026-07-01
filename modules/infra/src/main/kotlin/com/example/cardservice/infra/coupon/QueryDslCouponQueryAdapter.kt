package com.example.cardservice.infra.coupon

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.common.SortDirection
import com.example.cardservice.application.coupon.CouponHistoryPageResponse
import com.example.cardservice.application.coupon.CouponPageResponse
import com.example.cardservice.application.coupon.provided.CouponQueryPort
import com.example.cardservice.domain.coupon.QCoupon.coupon
import com.example.cardservice.domain.coupon.QCouponHistory.couponHistory
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Component

/**
 * 쿠폰 조회 전용 페이지 쿼리를 QueryDSL로 수행하는 read adapter다.
 */
@Component
class QueryDslCouponQueryAdapter(
    entityManager: EntityManager,
) : CouponQueryPort {
    private val queryFactory = JPAQueryFactory(entityManager)

    override fun searchCoupons(memberId: Long, pagination: Pagination): CouponPageResponse {
        val rows = queryFactory
            .select(
                QCouponRow(
                    coupon.id,
                    coupon.memberId,
                    coupon.orderId,
                    coupon.paymentId,
                    coupon.status,
                ),
            )
            .from(coupon)
            .where(coupon.memberId.eq(memberId))
            .orderBy(OrderSpecifier(orderOf(pagination.sortDirection), coupon.id))
            .offset(pagination.offset)
            .limit(pagination.normalizedSize.toLong())
            .fetch()
        val page = PageableExecutionUtils.getPage(rows, pagination.pageable()) {
            queryFactory
                .select(coupon.count())
                .from(coupon)
                .where(coupon.memberId.eq(memberId))
                .fetchOne() ?: 0L
        }

        return page.toCouponPageResponse()
    }

    override fun searchMemberCouponHistories(memberId: Long, pagination: Pagination): CouponHistoryPageResponse {
        val rows = queryFactory
            .select(
                QCouponHistoryRow(
                    couponHistory.id,
                    couponHistory.couponId,
                    couponHistory.memberId,
                    couponHistory.orderId,
                    couponHistory.paymentId,
                    couponHistory.type,
                ),
            )
            .from(couponHistory)
            .where(couponHistory.memberId.eq(memberId))
            .orderBy(OrderSpecifier(orderOf(pagination.sortDirection), couponHistory.id))
            .offset(pagination.offset)
            .limit(pagination.normalizedSize.toLong())
            .fetch()
        val page = PageableExecutionUtils.getPage(rows, pagination.pageable()) {
            queryFactory
                .select(couponHistory.count())
                .from(couponHistory)
                .where(couponHistory.memberId.eq(memberId))
                .fetchOne() ?: 0L
        }

        return page.toCouponHistoryPageResponse()
    }

    override fun searchOrderCouponHistories(orderId: Long, pagination: Pagination): CouponHistoryPageResponse {
        val rows = queryFactory
            .select(
                QCouponHistoryRow(
                    couponHistory.id,
                    couponHistory.couponId,
                    couponHistory.memberId,
                    couponHistory.orderId,
                    couponHistory.paymentId,
                    couponHistory.type,
                ),
            )
            .from(couponHistory)
            .where(couponHistory.orderId.eq(orderId))
            .orderBy(OrderSpecifier(orderOf(pagination.sortDirection), couponHistory.id))
            .offset(pagination.offset)
            .limit(pagination.normalizedSize.toLong())
            .fetch()
        val page = PageableExecutionUtils.getPage(rows, pagination.pageable()) {
            queryFactory
                .select(couponHistory.count())
                .from(couponHistory)
                .where(couponHistory.orderId.eq(orderId))
                .fetchOne() ?: 0L
        }

        return page.toCouponHistoryPageResponse()
    }

    private val Pagination.offset: Long
        get() = normalizedPage.toLong() * normalizedSize

    private fun Pagination.pageable(): PageRequest =
        pageRequest(normalizedPage, normalizedSize, sortDirection)

    private fun pageRequest(page: Int, size: Int, direction: SortDirection): PageRequest =
        PageRequest.of(page, size, Sort.by(sortDirectionOf(direction), "id"))

    private fun Page<CouponRow>.toCouponPageResponse(): CouponPageResponse =
        CouponPageResponse(
            items = content.map { it.toResponse() },
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext(),
        )

    private fun Page<CouponHistoryRow>.toCouponHistoryPageResponse(): CouponHistoryPageResponse =
        CouponHistoryPageResponse(
            items = content.map { it.toResponse() },
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext(),
        )

    private fun orderOf(direction: SortDirection): Order =
        if (direction == SortDirection.ASC) Order.ASC else Order.DESC

    private fun sortDirectionOf(direction: SortDirection): Sort.Direction =
        if (direction == SortDirection.ASC) Sort.Direction.ASC else Sort.Direction.DESC
}
