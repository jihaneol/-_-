package com.example.cardservice.domain.outbox

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Access(AccessType.FIELD)
@Table(
    name = "outbox_events",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_outbox_events_event_key", columnNames = ["event_key"]),
    ],
)
class OutboxEvent protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0L
        protected set

    @Column(name = "event_key", nullable = false, length = 120)
    var eventKey: String = ""
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 60)
    var eventType: OutboxEventType = OutboxEventType.PAYMENT_AUTHORIZED
        protected set

    @Column(name = "aggregate_type", nullable = false, length = 60)
    var aggregateType: String = "COMMERCE_ORDER"
        protected set

    @Column(name = "aggregate_id", nullable = false)
    var aggregateId: Long = 0
        protected set

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    var payload: String = ""
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: OutboxEventStatus = OutboxEventStatus.PENDING
        protected set

    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0
        protected set

    @Column(name = "last_error", columnDefinition = "TEXT")
    var lastError: String? = null
        protected set

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Column(name = "published_at")
    var publishedAt: LocalDateTime? = null
        protected set

    private constructor(
        eventKey: String,
        eventType: OutboxEventType,
        aggregateId: Long,
        payload: String,
        createdAt: LocalDateTime,
    ) : this() {
        require(eventKey.isNotBlank()) { "이벤트 키는 비어 있을 수 없습니다." }
        require(aggregateId > 0) { "aggregate ID는 0보다 커야 합니다." }
        require(payload.isNotBlank()) { "이벤트 payload는 비어 있을 수 없습니다." }
        this.eventKey = eventKey
        this.eventType = eventType
        this.aggregateId = aggregateId
        this.payload = payload
        this.createdAt = createdAt
    }

    fun markPublished(publishedAt: LocalDateTime = LocalDateTime.now()) {
        status = OutboxEventStatus.PUBLISHED
        this.publishedAt = publishedAt
        lastError = null
    }

    fun markFailed(error: String) {
        status = OutboxEventStatus.FAILED
        attemptCount += 1
        lastError = error.take(1_000)
    }

    companion object {
        fun paymentAuthorized(orderId: Long, payload: String): OutboxEvent =
            OutboxEvent(
                eventKey = "PAYMENT_AUTHORIZED:$orderId",
                eventType = OutboxEventType.PAYMENT_AUTHORIZED,
                aggregateId = orderId,
                payload = payload,
                createdAt = LocalDateTime.now(),
            )

        fun paymentRefunded(orderId: Long, payload: String): OutboxEvent =
            OutboxEvent(
                eventKey = "PAYMENT_REFUNDED:$orderId",
                eventType = OutboxEventType.PAYMENT_REFUNDED,
                aggregateId = orderId,
                payload = payload,
                createdAt = LocalDateTime.now(),
            )
    }
}

enum class OutboxEventType {
    PAYMENT_AUTHORIZED,
    PAYMENT_REFUNDED,
}

enum class OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
}
