package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.CommerceDashboardSummaryResult
import com.example.cardservice.application.commerce.required.DashboardQueryUseCase
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(DashboardController::class)
class DashboardControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var dashboardQueryUseCase: DashboardQueryUseCase

    @Test
    fun `운영 메인 요약은 회원 상품 주문 쿠폰 카운트를 반환한다`() {
        given(dashboardQueryUseCase.getSummary()).willReturn(
            CommerceDashboardSummaryResult(
                memberCount = 3,
                productCount = 5,
                orderCount = 7,
                paidOrderCount = 4,
                refundedOrderCount = 1,
                issuedCouponCount = 9,
            ),
        )

        mockMvc.get("/api/dashboard/summary")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value("SUCCESS") }
                jsonPath("$.data.memberCount") { value(3) }
                jsonPath("$.data.productCount") { value(5) }
                jsonPath("$.data.orderCount") { value(7) }
                jsonPath("$.data.paidOrderCount") { value(4) }
                jsonPath("$.data.refundedOrderCount") { value(1) }
                jsonPath("$.data.issuedCouponCount") { value(9) }
            }
    }
}
