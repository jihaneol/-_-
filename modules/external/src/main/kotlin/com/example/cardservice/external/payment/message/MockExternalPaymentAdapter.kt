package com.example.cardservice.external.payment.message

import com.example.cardservice.application.payment.ExternalPaymentApproval
import com.example.cardservice.application.payment.ExternalPaymentRequest
import com.example.cardservice.application.payment.provided.ExternalPaymentPort
import org.springframework.stereotype.Component

/**
 * ExternalPaymentPort를 0.3초 지연 mock 승인으로 구현하는 external adapter다.
 */
@Component
class MockExternalPaymentAdapter : ExternalPaymentPort {
    override fun approve(request: ExternalPaymentRequest): ExternalPaymentApproval {
        Thread.sleep(APPROVAL_DELAY_MILLIS)
        return ExternalPaymentApproval(
            approvalKey = "mock_${request.orderId.value}",
        )
    }

    private companion object {
        const val APPROVAL_DELAY_MILLIS = 300L
    }
}
