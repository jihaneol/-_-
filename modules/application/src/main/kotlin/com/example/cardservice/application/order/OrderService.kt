package com.example.cardservice.application.order

import com.example.cardservice.application.order.provided.OrderWorkflowLockPort
import com.example.cardservice.application.order.provided.OrderRepository
import com.example.cardservice.application.member.provided.MemberRepository
import com.example.cardservice.application.product.provided.ProductRepository
import com.example.cardservice.application.order.required.OrderUseCase
import com.example.cardservice.domain.order.Order
import com.example.cardservice.domain.order.OrderItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 생성, 취소, 삭제 흐름을 조율하는 application service다.
 */
@Service
class OrderService(
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val commerceLockPort: OrderWorkflowLockPort,
) : OrderUseCase {
    @Transactional
    override fun createOrder(input: CreateOrderInput): OrderResult {
        memberRepository.findByIdAndDeletedAtIsNull(input.memberId) ?: throw IllegalArgumentException("회원을 찾을 수 없습니다.")
        val lines = input.lines.map { line ->
            val product = productRepository.findByIdAndDeletedAtIsNull(line.productId) ?: throw IllegalArgumentException("상품을 찾을 수 없습니다.")
            OrderItem.create(
                productId = product.id,
                productName = product.name,
                unitPrice = product.price,
                quantity = line.quantity,
            )
        }
        return orderRepository.save(Order.create(input.memberId, lines)).toResult()
    }

    @Transactional
    override fun cancelOrder(orderId: Long): OrderResult {
        val order = commerceLockPort.loadOrderForUpdate(orderId) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다.")
        order.cancel()
        return orderRepository.save(order).toResult()
    }

    @Transactional
    override fun deleteOrder(orderId: Long) {
        val order = loadOrder(orderId)
        order.softDelete()
        orderRepository.save(order)
    }

    private fun loadOrder(orderId: Long): Order =
        orderRepository.findByIdAndDeletedAtIsNull(orderId) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다.")
}

internal fun Order.toResult(): OrderResult =
    OrderResult(
        id = id,
        memberId = memberId,
        status = status,
        totalAmount = totalAmount,
        currency = currency,
        paymentId = paymentId,
        lines = lines.map {
            OrderItemResult(
                productId = it.productId,
                productName = it.productName,
                unitPrice = it.unitPrice,
                quantity = it.quantity,
                lineAmount = it.lineAmount,
            )
        },
    )
