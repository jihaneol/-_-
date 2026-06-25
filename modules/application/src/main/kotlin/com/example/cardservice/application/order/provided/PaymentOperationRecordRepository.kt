package com.example.cardservice.application.order.provided

import com.example.cardservice.domain.payment.operation.PaymentOperationRecord
import com.example.cardservice.domain.payment.operation.PaymentOperationType
import org.springframework.data.repository.Repository

/**
 * PaymentOperationRecord 저장을 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface PaymentOperationRecordRepository : Repository<PaymentOperationRecord, Long> {
    fun save(record: PaymentOperationRecord): PaymentOperationRecord
    fun countByOperationTypeAndOrderId(operationType: PaymentOperationType, orderId: Long): Long
}
