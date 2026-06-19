package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CommerceLockPort
import com.example.cardservice.application.commerce.provided.CommerceOrderRepository
import com.example.cardservice.application.commerce.provided.CouponHistoryRepository
import com.example.cardservice.application.commerce.provided.CouponRepository
import com.example.cardservice.application.commerce.provided.InventoryRepository
import com.example.cardservice.application.commerce.required.OrderPaymentUseCase
import com.example.cardservice.application.payment.provided.PaymentRepository
import com.example.cardservice.domain.commerce.model.CommerceOrder
import com.example.cardservice.domain.commerce.model.Coupon
import com.example.cardservice.domain.commerce.model.CouponHistory
import com.example.cardservice.domain.commerce.model.OrderStatus
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.MerchantId
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.Payment
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 결제, 재고 차감, 쿠폰 발급, 전체 환불을 조율하는 application facade다.
 */
@Service
class OrderPaymentFacade(
    private val orderRepository: CommerceOrderRepository,
    private val commerceLockPort: CommerceLockPort,
    private val inventoryRepository: InventoryRepository,
    private val paymentRepository: PaymentRepository,
    private val couponRepository: CouponRepository,
    private val couponHistoryRepository: CouponHistoryRepository,
) : OrderPaymentUseCase {
    @Transactional
    override fun payOrder(orderId: Long, input: PayOrderInput): PayOrderResult {
        val order = loadOrderForUpdate(orderId)
        val existingPayment = paymentRepository.findByIdempotencyKeyValue(input.idempotencyKey)
        if (existingPayment != null) {
            require(existingPayment.orderId.value == orderId.toString()) { "같은 중복 요청 방지 키로 다른 요청 본문이 들어오면 거절한다." }
            val paymentId = requireNotNull(existingPayment.id)
            if (order.status == OrderStatus.CREATED) {
                order.pay(paymentId)
                orderRepository.save(order)
            }
            return order.toPayResult(existingPayment, couponRepository.countByOrderId(orderId).toInt())
        }

        order.lines.forEach { line ->
            val inventory = commerceLockPort.loadInventoryForUpdate(line.productId) ?: throw IllegalArgumentException("재고를 찾을 수 없습니다.")
            inventory.decrease(line.quantity)
            inventoryRepository.save(inventory)
        }

        val payment = savePaymentOrLoadDuplicate(
            Payment.authorize(
                merchantId = COMMERCE_MERCHANT_ID,
                orderId = OrderId(orderId.toString()),
                idempotencyKey = IdempotencyKey(input.idempotencyKey),
                money = order.money(),
            ),
        )
        val paymentId = requireNotNull(payment.id)
        order.pay(paymentId)
        orderRepository.save(order)

        val coupons = (1..Coupon.issueCount(order.totalAmount))
            .map { Coupon.issue(memberId = order.memberId, orderId = orderId, paymentId = paymentId) }
            .let { couponRepository.saveAll(it).toList() }
        couponHistoryRepository.saveAll(coupons.map { CouponHistory.issued(it) })

        return order.toPayResult(payment, coupons.size)
    }

    @Transactional
    override fun refundOrder(orderId: Long): RefundOrderResult {
        val order = loadOrderForUpdate(orderId)
        val paymentId = requireNotNull(order.paymentId) { "결제 전 주문은 환불할 수 없습니다." }
        val payment = paymentRepository.findById(paymentId).orElseThrow { IllegalArgumentException("결제를 찾을 수 없습니다.") }

        order.lines.forEach { line ->
            val inventory = commerceLockPort.loadInventoryForUpdate(line.productId) ?: throw IllegalArgumentException("재고를 찾을 수 없습니다.")
            inventory.increase(line.quantity)
            inventoryRepository.save(inventory)
        }
        order.refund()
        payment.refund()

        val coupons = couponRepository.findAllByOrderId(orderId)
        coupons.forEach { it.void() }
        couponRepository.saveAll(coupons)
        couponHistoryRepository.saveAll(coupons.map { CouponHistory.voided(it) })

        orderRepository.save(order)
        paymentRepository.save(payment)

        return RefundOrderResult(
            orderId = orderId,
            paymentId = paymentId,
            orderStatus = order.status,
            paymentStatus = payment.status.name,
            voidedCouponCount = coupons.size,
        )
    }

    private fun loadOrderForUpdate(orderId: Long): CommerceOrder =
        commerceLockPort.loadOrderForUpdate(orderId) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다.")

    private fun savePaymentOrLoadDuplicate(payment: Payment): Payment =
        try {
            paymentRepository.save(payment)
        } catch (exception: DataIntegrityViolationException) {
            paymentRepository.findByIdempotencyKeyValue(payment.idempotencyKey.value)
                ?: throw exception
        }

    private fun CommerceOrder.toPayResult(payment: Payment, issuedCouponCount: Int): PayOrderResult =
        PayOrderResult(
            orderId = requireNotNull(id),
            paymentId = requireNotNull(payment.id),
            orderStatus = status,
            paymentStatus = payment.status.name,
            paidAmount = totalAmount,
            issuedCouponCount = issuedCouponCount,
        )

    private companion object {
        val COMMERCE_MERCHANT_ID = MerchantId("commerce-merchant")
    }
}
