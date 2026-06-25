package com.example.cardservice.domain.outbox

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Access(AccessType.FIELD)
@Table(
    name = "processed_outbox_events",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_processed_outbox_events_event_key", columnNames = ["event_key"]),
    ],
)
class ProcessedOutboxEvent protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "event_key", nullable = false, length = 120)
    var eventKey: String = ""
        protected set

    @Column(name = "processed_at", nullable = false)
    var processedAt: LocalDateTime = LocalDateTime.now()
        protected set

    private constructor(eventKey: String, processedAt: LocalDateTime) : this() {
        require(eventKey.isNotBlank()) { "이벤트 키는 비어 있을 수 없습니다." }
        this.eventKey = eventKey
        this.processedAt = processedAt
    }

    companion object {
        fun record(eventKey: String): ProcessedOutboxEvent =
            ProcessedOutboxEvent(eventKey = eventKey, processedAt = LocalDateTime.now())
    }
}
