package com.example.cardservice.web.order

import com.example.cardservice.application.order.PayOrderRequest
import com.example.cardservice.application.order.PayOrderResponse
import com.example.cardservice.application.member.provided.MemberRepository
import com.example.cardservice.application.order.required.OrderPaymentUseCase
import com.example.cardservice.web.order.OrderPaymentController
import com.example.cardservice.domain.order.OrderStatus
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(OrderPaymentController::class)
@AutoConfigureMockMvc(addFilters = false)
class OrderPaymentControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var orderPaymentUseCase: OrderPaymentUseCase

    @MockitoBean
    lateinit var memberRepository: MemberRepository

    @Test
    fun `주문 결제는 결제 결과와 발급 쿠폰 수를 반환한다`() {
        given(orderPaymentUseCase.payOrder(any<PayOrderRequest>())).willReturn(
            PayOrderResponse(
                orderId = 1L,
                paymentId = 1L,
                orderStatus = OrderStatus.PAID,
                paymentStatus = "AUTHORIZED",
                paidAmount = 12_000L,
                issuedCouponCount = 2,
            ),
        )

        mockMvc.post("/api/admin/orders/1/pay") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "idempotencyKey": "pay-20260619-0001"
                }
            """.trimIndent()
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.result.code") { value("OK") }
                jsonPath("$.payload.orderId") { value(1) }
                jsonPath("$.payload.paymentId") { value(1) }
                jsonPath("$.payload.orderStatus") { value("PAID") }
                jsonPath("$.payload.paymentStatus") { value("AUTHORIZED") }
                jsonPath("$.payload.paidAmount") { value(12000) }
                jsonPath("$.payload.issuedCouponCount") { value(2) }
            }
    }
}
