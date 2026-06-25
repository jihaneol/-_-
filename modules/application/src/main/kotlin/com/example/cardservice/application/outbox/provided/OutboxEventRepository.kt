package com.example.cardservice.application.outbox.provided

import com.example.cardservice.domain.outbox.OutboxEvent
import org.springframework.data.repository.Repository

/**
 * 결제 트랜잭션 안에서 outbox row를 append하기 위한 repository 계약이다.
 */
interface OutboxEventRepository : Repository<OutboxEvent, Long> {
    fun save(event: OutboxEvent): OutboxEvent
}
