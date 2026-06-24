package com.example.cardservice.application.commerce.provided

import com.example.cardservice.domain.commerce.model.projection.PaymentOperationalProjection
import com.example.cardservice.domain.commerce.model.projection.PaymentOperationalProjectionType
import org.springframework.data.repository.Repository

/**
 * PaymentOperationalProjection 저장을 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface PaymentOperationalProjectionRepository : Repository<PaymentOperationalProjection, Long> {
    fun save(projection: PaymentOperationalProjection): PaymentOperationalProjection
    fun countByOperationTypeAndOrderId(operationType: PaymentOperationalProjectionType, orderId: Long): Long
}
