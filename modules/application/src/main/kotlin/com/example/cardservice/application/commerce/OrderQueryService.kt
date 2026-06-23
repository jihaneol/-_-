package com.example.cardservice.application.commerce

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.common.toPageable
import com.example.cardservice.application.commerce.provided.CommerceOrderRepository
import com.example.cardservice.application.commerce.required.OrderQueryUseCase
import com.example.cardservice.domain.commerce.model.order.CommerceOrder
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 조회 흐름을 조율하는 application query service다.
 */
@Service
class OrderQueryService(
    private val orderRepository: CommerceOrderRepository,
) : OrderQueryUseCase {
    @Transactional(readOnly = true)
    override fun listOrders(pagination: Pagination): OrderPageResult =
        orderRepository.findAllByDeletedAtIsNull(pagination.toPageable()).toPageResult()

    @Transactional(readOnly = true)
    override fun getOrder(orderId: Long): OrderResult =
        (orderRepository.findByIdAndDeletedAtIsNull(orderId) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다."))
            .toResult()

    private fun Page<CommerceOrder>.toPageResult(): OrderPageResult =
        OrderPageResult(
            items = content.map { it.toResult() },
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext(),
        )
}
