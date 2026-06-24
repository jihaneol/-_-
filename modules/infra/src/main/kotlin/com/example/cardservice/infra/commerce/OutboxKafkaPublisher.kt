package com.example.cardservice.infra.commerce

import com.example.cardservice.domain.commerce.model.outbox.OutboxEvent
import com.example.cardservice.domain.commerce.model.outbox.OutboxEventStatus
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

/**
 * DB outbox row를 Kafka topic으로 발행하고 발행 상태를 갱신한다.
 */
@Component
@ConditionalOnProperty(prefix = "commerce.outbox.kafka", name = ["enabled"], havingValue = "true")
class OutboxKafkaPublisher(
    private val entityManager: EntityManager,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${commerce.outbox.kafka.topic:commerce.order-events.v1}")
    private val topic: String,
    @Value("\${commerce.outbox.kafka.publisher-batch-size:100}")
    private val batchSize: Int,
) {
    @Scheduled(fixedDelayString = "\${commerce.outbox.kafka.publisher-delay-ms:1000}")
    @Transactional
    fun publishPending() {
        val events = entityManager
            .createQuery(
                """
                select e
                from OutboxEvent e
                where e.status in :statuses
                order by e.id asc
                """.trimIndent(),
                OutboxEvent::class.java,
            )
            .setParameter("statuses", listOf(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED))
            .setMaxResults(batchSize)
            .resultList

        events.forEach { event ->
            try {
                kafkaTemplate.send(topic, event.eventKey, event.payload).get(5, TimeUnit.SECONDS)
                event.markPublished()
            } catch (exception: Exception) {
                event.markFailed(exception.message ?: exception::class.java.simpleName)
            }
        }
    }
}
