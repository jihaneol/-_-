package com.example.cardservice.payment.adapter.`in`.web

import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderCommand
import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderResult
import com.example.cardservice.payment.application.port.`in`.PlaceStarbucksCouponOrderUseCase
import com.example.cardservice.payment.domain.model.OrderId
import com.example.cardservice.payment.domain.model.PaymentId
import com.example.cardservice.payment.domain.model.PaymentStatus
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

@WebMvcTest(StarbucksCouponOrderController::class)
class StarbucksCouponOrderControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var placeStarbucksCouponOrderUseCase: PlaceStarbucksCouponOrderUseCase

    @Test
    fun `스타벅스 쿠폰 주문을 생성한다`() {
        given(placeStarbucksCouponOrderUseCase.place(any<PlaceStarbucksCouponOrderCommand>())).willReturn(
            PlaceStarbucksCouponOrderResult(
                orderId = OrderId("order-1"),
                paymentId = PaymentId("pay_1"),
                paymentStatus = PaymentStatus.AUTHORIZED,
                amount = 10_000,
                currency = "KRW",
                couponIds = listOf("starbucks_coupon_1", "starbucks_coupon_2"),
            ),
        )

        mockMvc.post("/api/starbucks-coupon-orders") {
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
                jsonPath("$.orderId") { value("order-1") }
                jsonPath("$.paymentStatus") { value("AUTHORIZED") }
                jsonPath("$.amount") { value(10000) }
                jsonPath("$.currency") { value("KRW") }
                jsonPath("$.couponIds", hasSize<String>(2))
            }
    }
}
