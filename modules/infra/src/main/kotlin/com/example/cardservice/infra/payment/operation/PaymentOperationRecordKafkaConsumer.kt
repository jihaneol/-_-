package com.example.cardservice.infra.payment.operation

import com.example.cardservice.application.outbox.PaymentOperationalEventPayload
import com.example.cardservice.application.outbox.PaymentOperationalEventType
import com.example.cardservice.application.order.provided.PaymentOperationRecordRepository
import com.example.cardservice.application.outbox.provided.ProcessedOutboxEventRepository
import com.example.cardservice.domain.outbox.ProcessedOutboxEvent
import com.example.cardservice.domain.payment.operation.PaymentOperationRecord
import com.example.cardservice.domain.payment.operation.PaymentOperationType
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Kafka에서 결제 운영 이벤트를 소비해 payment operation record를 idempotent하게 생성한다.
 */
@Component
@ConditionalOnProperty(prefix = "commerce.outbox.kafka", name = ["enabled"], havingValue = "true")
class PaymentOperationRecordKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val recordRepository: PaymentOperationRecordRepository,
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
                if (recordRepository.countByOperationTypeAndOrderId(
                        PaymentOperationType.PAYMENT_AUTHORIZED,
                        event.orderId,
                    ) == 0L
                ) {
                    recordRepository.save(
                        PaymentOperationRecord.paymentAuthorized(
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
                if (recordRepository.countByOperationTypeAndOrderId(
                        PaymentOperationType.PAYMENT_REFUNDED,
                        event.orderId,
                    ) == 0L
                ) {
                    recordRepository.save(
                        PaymentOperationRecord.paymentRefunded(
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
