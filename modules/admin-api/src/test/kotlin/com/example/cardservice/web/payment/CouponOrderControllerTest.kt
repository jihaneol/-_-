package com.example.cardservice.web.payment

import com.example.cardservice.application.payment.CreateCouponOrderInput
import com.example.cardservice.application.payment.CreateCouponOrderResult
import com.example.cardservice.application.payment.required.CouponOrderUseCase
import com.example.cardservice.web.payment.couponorder.CouponOrderController
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
                orderId = OrderId(10L),
                paymentId = PaymentId(1),
                paymentStatus = PaymentStatus.AUTHORIZED,
                amount = 10_000,
                currency = "KRW",
                couponIds = listOf("coupon_1", "coupon_2"),
            ),
        )

        mockMvc.post("/api/admin/coupon-orders") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerId": 1,
                  "orderId": 10,
                  "idempotencyKey": "idem-1",
                  "quantity": 2
                }
            """.trimIndent()
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.result.code") { value("OK") }
                jsonPath("$.result.message") { value("요청이 성공했습니다.") }
                jsonPath("$.payload.orderId") { value(10) }
                jsonPath("$.payload.paymentId") { value(1) }
                jsonPath("$.payload.paymentStatus") { value("AUTHORIZED") }
                jsonPath("$.payload.paymentStatusLabel") { value("승인 완료") }
                jsonPath("$.payload.amount") { value(10000) }
                jsonPath("$.payload.currency") { value("KRW") }
                jsonPath("$.payload.couponIds", hasSize<String>(2))
            }
    }

    @Test
    fun `use case에서 잘못된 요청 예외가 발생하면 bad request를 반환한다`() {
        given(couponOrderUseCase.create(any<CreateCouponOrderInput>()))
            .willThrow(IllegalArgumentException("쿠폰 수량은 1개 이상이어야 합니다."))

        mockMvc.post("/api/admin/coupon-orders") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerId": 1,
                  "orderId": 10,
                  "idempotencyKey": "idem-1",
                  "quantity": 2
                }
            """.trimIndent()
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.result.code") { value("BAD_REQUEST") }
                jsonPath("$.result.message") { value("쿠폰 수량은 1개 이상이어야 합니다.") }
            }
    }
}
