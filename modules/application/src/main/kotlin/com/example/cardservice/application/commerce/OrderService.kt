package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CommerceLockPort
import com.example.cardservice.application.commerce.provided.CommerceOrderRepository
import com.example.cardservice.application.commerce.provided.MemberRepository
import com.example.cardservice.application.commerce.provided.ProductRepository
import com.example.cardservice.application.commerce.required.OrderUseCase
import com.example.cardservice.domain.commerce.model.order.CommerceOrder
import com.example.cardservice.domain.commerce.model.order.OrderLine
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 생성, 취소, 삭제 흐름을 조율하는 application service다.
 */
@Service
class OrderService(
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: CommerceOrderRepository,
    private val commerceLockPort: CommerceLockPort,
) : OrderUseCase {
    @Transactional
    override fun createOrder(input: CreateOrderInput): OrderResult {
        memberRepository.findByIdAndDeletedAtIsNull(input.memberId) ?: throw IllegalArgumentException("회원을 찾을 수 없습니다.")
        val lines = input.lines.map { line ->
            val product = productRepository.findByIdAndDeletedAtIsNull(line.productId) ?: throw IllegalArgumentException("상품을 찾을 수 없습니다.")
            OrderLine.create(
                productId = requireNotNull(product.id) { "상품 ID가 필요합니다." },
                productName = product.name,
                unitPrice = product.price,
                quantity = line.quantity,
            )
        }
        return orderRepository.save(CommerceOrder.create(input.memberId, lines)).toResult()
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

    private fun loadOrder(orderId: Long): CommerceOrder =
        orderRepository.findByIdAndDeletedAtIsNull(orderId) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다.")
}

internal fun CommerceOrder.toResult(): OrderResult =
    OrderResult(
        id = requireNotNull(id),
        memberId = memberId,
        status = status,
        totalAmount = totalAmount,
        currency = currency,
        paymentId = paymentId,
        lines = lines.map {
            OrderLineResult(
                productId = it.productId,
                productName = it.productName,
                unitPrice = it.unitPrice,
                quantity = it.quantity,
                lineAmount = it.lineAmount,
            )
        },
    )
