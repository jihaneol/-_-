package com.example.cardservice.application.order

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.common.toPageable
import com.example.cardservice.application.order.provided.OrderRepository
import com.example.cardservice.application.order.required.OrderQueryUseCase
import com.example.cardservice.domain.order.Order
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 조회 흐름을 조율하는 application query service다.
 */
@Service
class OrderQueryService(
    private val orderRepository: OrderRepository,
) : OrderQueryUseCase {
    @Transactional(readOnly = true)
    override fun listOrders(pagination: Pagination): OrderPageResponse =
        orderRepository.findAllByDeletedAtIsNull(pagination.toPageable()).toPageResponse()

    @Transactional(readOnly = true)
    override fun getOrder(orderId: Long): OrderResponse =
        (orderRepository.findByIdAndDeletedAtIsNull(orderId) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다."))
            .toResponse()

    private fun Page<Order>.toPageResponse(): OrderPageResponse =
        OrderPageResponse(
            items = content.map { it.toResponse() },
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext(),
        )
}
