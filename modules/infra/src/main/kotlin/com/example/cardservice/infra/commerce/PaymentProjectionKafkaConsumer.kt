package com.example.cardservice.infra.commerce

import com.example.cardservice.application.commerce.PaymentOperationalEventPayload
import com.example.cardservice.application.commerce.PaymentOperationalEventType
import com.example.cardservice.application.commerce.provided.PaymentOperationalProjectionRepository
import com.example.cardservice.application.commerce.provided.ProcessedOutboxEventRepository
import com.example.cardservice.domain.commerce.model.outbox.ProcessedOutboxEvent
import com.example.cardservice.domain.commerce.model.projection.PaymentOperationalProjection
import com.example.cardservice.domain.commerce.model.projection.PaymentOperationalProjectionType
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Kafka에서 결제 운영 이벤트를 소비해 projection/audit row를 idempotent하게 생성한다.
 */
@Component
@ConditionalOnProperty(prefix = "commerce.outbox.kafka", name = ["enabled"], havingValue = "true")
class PaymentProjectionKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val projectionRepository: PaymentOperationalProjectionRepository,
    private val processedOutboxEventRepository: ProcessedOutboxEventRepository,
) {
    @KafkaListener(
        topics = ["\${commerce.outbox.kafka.topic:commerce.order-events.v1}"],
        groupId = "\${commerce.outbox.kafka.consumer-group:card-service-payment-projection}",
    )
    @Transactional
    fun consume(payload: String) {
        val event = objectMapper.readValue(payload, PaymentOperationalEventPayload::class.java)
        if (processedOutboxEventRepository.existsByEventKey(event.eventKey)) {
            return
        }

        when (event.eventType) {
            PaymentOperationalEventType.PAYMENT_AUTHORIZED -> {
                if (projectionRepository.countByOperationTypeAndOrderId(
                        PaymentOperationalProjectionType.PAYMENT_AUTHORIZED,
                        event.orderId,
                    ) == 0L
                ) {
                    projectionRepository.save(
                        PaymentOperationalProjection.paymentAuthorized(
                            orderId = event.orderId,
                            paymentId = event.paymentId,
                            memberId = event.memberId,
                            amount = event.amount,
                            currency = event.currency,
                            issuedCouponCount = event.issuedCouponCount,
                        ),
                    )
                }
            }

            PaymentOperationalEventType.PAYMENT_REFUNDED -> {
                if (projectionRepository.countByOperationTypeAndOrderId(
                        PaymentOperationalProjectionType.PAYMENT_REFUNDED,
                        event.orderId,
                    ) == 0L
                ) {
                    projectionRepository.save(
                        PaymentOperationalProjection.paymentRefunded(
                            orderId = event.orderId,
                            paymentId = event.paymentId,
                            memberId = event.memberId,
                            amount = event.amount,
                            currency = event.currency,
                            voidedCouponCount = event.voidedCouponCount,
                        ),
                    )
                }
            }
        }

        processedOutboxEventRepository.save(ProcessedOutboxEvent.record(event.eventKey))
    }
}
