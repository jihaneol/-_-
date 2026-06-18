package com.example.cardservice.payment.adapter.out.message

import com.example.cardservice.payment.application.port.out.ExternalPaymentApproval
import com.example.cardservice.payment.application.port.out.ExternalPaymentPort
import com.example.cardservice.payment.application.port.out.ExternalPaymentRequest
import org.springframework.stereotype.Component

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
