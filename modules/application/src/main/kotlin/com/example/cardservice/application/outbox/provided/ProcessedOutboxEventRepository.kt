package com.example.cardservice.application.outbox.provided

import com.example.cardservice.domain.outbox.ProcessedOutboxEvent
import org.springframework.data.repository.Repository

/**
 * Kafka consumer 재처리를 idempotent하게 만들기 위한 처리 완료 이벤트 repository 계약이다.
 */
interface ProcessedOutboxEventRepository : Repository<ProcessedOutboxEvent, Long> {
    fun save(event: ProcessedOutboxEvent): ProcessedOutboxEvent
    fun existsByEventKey(eventKey: String): Boolean
}
