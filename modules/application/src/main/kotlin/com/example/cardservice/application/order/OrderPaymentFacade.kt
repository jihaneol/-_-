package com.example.cardservice.application.order

import com.example.cardservice.application.order.provided.OrderWorkflowLockPort
import com.example.cardservice.application.order.provided.OrderRepository
import com.example.cardservice.application.order.provided.OrderStatusMutationPort
import com.example.cardservice.application.coupon.provided.CouponHistoryRepository
import com.example.cardservice.application.coupon.provided.CouponRepository
import com.example.cardservice.application.inventory.provided.InventoryMutationPort
import com.example.cardservice.application.outbox.PaymentOperationalEventPayload
import com.example.cardservice.application.outbox.PaymentOperationalEventType
import com.example.cardservice.application.outbox.provided.OutboxEventRepository
import com.example.cardservice.application.order.required.OrderPaymentUseCase
import com.example.cardservice.application.payment.provided.PaymentRepository
import com.example.cardservice.domain.order.Order
import com.example.cardservice.domain.coupon.Coupon
import com.example.cardservice.domain.coupon.CouponHistory
import com.example.cardservice.domain.order.OrderStatus
import com.example.cardservice.domain.outbox.OutboxEvent
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.MerchantId
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.Payment
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 결제, 재고 차감, 쿠폰 발급, 전체 환불을 조율하는 application facade다.
 */
@Service
class OrderPaymentFacade(
    private val orderRepository: OrderRepository,
    private val commerceLockPort: OrderWorkflowLockPort,
    private val orderStatusMutationPort: OrderStatusMutationPort,
    private val inventoryMutationPort: InventoryMutationPort,
    private val paymentRepository: PaymentRepository,
    private val couponRepository: CouponRepository,
    private val couponHistoryRepository: CouponHistoryRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
) : OrderPaymentUseCase {
    @Transactional
    override fun payOrder(request: PayOrderRequest): PayOrderResponse {
        val orderId = request.orderId
        val order = loadOrder(orderId)
        val existingPayment = paymentRepository.findByIdempotencyKeyValue(request.idempotencyKey)
        if (existingPayment != null) {
            require(existingPayment.orderId.value == orderId) { "같은 중복 요청 방지 키로 다른 요청 본문이 들어오면 거절한다." }
            val paymentId = existingPayment.id
            val resultStatus =
                if (order.status == OrderStatus.CREATED) {
                    require(orderStatusMutationPort.markPaidIfCreated(orderId, paymentId)) { "이미 처리된 주문입니다." }
                    OrderStatus.PAID
                } else {
                    order.status
                }
            return order.toPayResult(existingPayment, couponRepository.countByOrderId(orderId).toInt(), resultStatus)
        }

        val payment = savePaymentOrLoadDuplicate(
            Payment.authorize(
                merchantId = COMMERCE_MERCHANT_ID,
                orderId = OrderId(orderId),
                idempotencyKey = IdempotencyKey(request.idempotencyKey),
                money = order.money(),
            ),
        )
        val paymentId = payment.id

        order.lines.forEach { line ->
            require(inventoryMutationPort.decreaseQuantityIfEnough(line.productId, line.quantity)) { "재고가 부족합니다." }
        }

        val coupons = (1..Coupon.issueCount(order.totalAmount))
            .map { Coupon.issue(memberId = order.memberId, orderId = orderId, paymentId = paymentId) }
            .let { couponRepository.saveAll(it).toList() }
        couponHistoryRepository.saveAll(coupons.map { CouponHistory.issued(it) })

        outboxEventRepository.save(
            OutboxEvent.paymentAuthorized(
                orderId = orderId,
                payload = objectMapper.writeValueAsString(
                    PaymentOperationalEventPayload(
                        eventKey = "PAYMENT_AUTHORIZED:$orderId",
                        eventType = PaymentOperationalEventType.PAYMENT_AUTHORIZED,
                        orderId = orderId,
                        paymentId = paymentId,
                        memberId = order.memberId,
                        amount = payment.money.amount,
                        currency = payment.money.currency,
                        issuedCouponCount = coupons.size,
                        voidedCouponCount = 0,
                    ),
                ),
            ),
        )

        require(orderStatusMutationPort.markPaidIfCreated(orderId, paymentId)) { "이미 처리된 주문입니다." }

        return order.toPayResult(payment, coupons.size, OrderStatus.PAID)
    }

    @Transactional
    override fun refundOrder(orderId: Long): RefundOrderResponse {
        val order = loadOrderForUpdate(orderId)
        val paymentId = requireNotNull(order.paymentId) { "결제 전 주문은 환불할 수 없습니다." }
        val payment = paymentRepository.findById(paymentId).orElseThrow { IllegalArgumentException("결제를 찾을 수 없습니다.") }

        order.lines.forEach { line ->
            inventoryMutationPort.increaseQuantity(line.productId, line.quantity)
        }
        order.refund()
        payment.refund()

        val coupons = couponRepository.findAllByOrderId(orderId)
        coupons.forEach { it.void() }
        couponRepository.saveAll(coupons)
        couponHistoryRepository.saveAll(coupons.map { CouponHistory.voided(it) })

        orderRepository.save(order)
        paymentRepository.save(payment)
        outboxEventRepository.save(
            OutboxEvent.paymentRefunded(
                orderId = orderId,
                payload = objectMapper.writeValueAsString(
                    PaymentOperationalEventPayload(
                        eventKey = "PAYMENT_REFUNDED:$orderId",
                        eventType = PaymentOperationalEventType.PAYMENT_REFUNDED,
                        orderId = orderId,
                        paymentId = paymentId,
                        memberId = order.memberId,
                        amount = payment.money.amount,
                        currency = payment.money.currency,
                        issuedCouponCount = 0,
                        voidedCouponCount = coupons.size,
                    ),
                ),
            ),
        )

        return RefundOrderResponse(
            orderId = orderId,
            paymentId = paymentId,
            orderStatus = order.status,
            paymentStatus = payment.status.name,
            voidedCouponCount = coupons.size,
        )
    }

    private fun loadOrderForUpdate(orderId: Long): Order =
        commerceLockPort.loadOrderForUpdate(orderId) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다.")

    private fun loadOrder(orderId: Long): Order =
        orderRepository.findByIdAndDeletedAtIsNull(orderId) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다.")

    private fun savePaymentOrLoadDuplicate(payment: Payment): Payment =
        try {
            paymentRepository.save(payment)
        } catch (exception: DataIntegrityViolationException) {
            paymentRepository.findByIdempotencyKeyValue(payment.idempotencyKey.value)
                ?: throw exception
        }

    private fun Order.toPayResult(
        payment: Payment,
        issuedCouponCount: Int,
        orderStatus: OrderStatus = status,
    ): PayOrderResponse =
        PayOrderResponse(
            orderId = id,
            paymentId = payment.id,
            orderStatus = orderStatus,
            paymentStatus = payment.status.name,
            paidAmount = totalAmount,
            issuedCouponCount = issuedCouponCount,
        )

    private companion object {
        val COMMERCE_MERCHANT_ID = MerchantId(1L)
    }
}
