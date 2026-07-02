package com.example.cardservice.application.order.provided

import com.example.cardservice.domain.order.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.Repository

/**
 * Order entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface OrderRepository : Repository<Order, Long> {
    fun save(order: Order): Order
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<Order>
    fun findByIdAndDeletedAtIsNull(id: Long): Order?
}
