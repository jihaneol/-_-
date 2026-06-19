package com.example.cardservice.web.payment

import com.example.cardservice.application.payment.CreateCouponOrderInput
import com.example.cardservice.application.payment.CreateCouponOrderResult
import com.example.cardservice.application.payment.required.CouponOrderUseCase
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.PaymentId
import com.example.cardservice.domain.payment.model.PaymentStatus
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(CouponOrderController::class)
class CouponOrderControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var couponOrderUseCase: CouponOrderUseCase

    @Test
    fun `쿠폰 주문을 생성한다`() {
        given(couponOrderUseCase.create(any<CreateCouponOrderInput>())).willReturn(
            CreateCouponOrderResult(
                orderId = OrderId("order-1"),
                paymentId = PaymentId(1),
                paymentStatus = PaymentStatus.AUTHORIZED,
                amount = 10_000,
                currency = "KRW",
                couponIds = listOf("coupon_1", "coupon_2"),
            ),
        )

        mockMvc.post("/api/coupon-orders") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerId": "customer-1",
                  "orderId": "order-1",
                  "idempotencyKey": "idem-1",
                  "quantity": 2
                }
            """.trimIndent()
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.code") { value("SUCCESS") }
                jsonPath("$.message") { value("요청이 성공했습니다.") }
                jsonPath("$.data.orderId") { value("order-1") }
                jsonPath("$.data.paymentId") { value("1") }
                jsonPath("$.data.paymentStatus") { value("AUTHORIZED") }
                jsonPath("$.data.paymentStatusLabel") { value("승인 완료") }
                jsonPath("$.data.amount") { value(10000) }
                jsonPath("$.data.currency") { value("KRW") }
                jsonPath("$.data.couponIds", hasSize<String>(2))
            }
    }

    @Test
    fun `use case에서 잘못된 요청 예외가 발생하면 bad request를 반환한다`() {
        given(couponOrderUseCase.create(any<CreateCouponOrderInput>()))
            .willThrow(IllegalArgumentException("쿠폰 수량은 1개 이상이어야 합니다."))

        mockMvc.post("/api/coupon-orders") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerId": "customer-1",
                  "orderId": "order-1",
                  "idempotencyKey": "idem-1",
                  "quantity": 2
                }
            """.trimIndent()
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("BAD_REQUEST") }
                jsonPath("$.message") { value("쿠폰 수량은 1개 이상이어야 합니다.") }
            }
    }
}
