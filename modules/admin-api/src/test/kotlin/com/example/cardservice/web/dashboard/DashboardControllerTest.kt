package com.example.cardservice.web.dashboard

import com.example.cardservice.application.dashboard.DashboardSummaryResult
import com.example.cardservice.application.dashboard.required.DashboardQueryUseCase
import com.example.cardservice.web.dashboard.DashboardController
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
            DashboardSummaryResult(
                memberCount = 3,
                productCount = 5,
                orderCount = 7,
                paidOrderCount = 4,
                refundedOrderCount = 1,
                issuedCouponCount = 9,
            ),
        )

        mockMvc.get("/api/admin/dashboard/summary")
            .andExpect {
                status { isOk() }
                jsonPath("$.result.code") { value("OK") }
                jsonPath("$.payload.memberCount") { value(3) }
                jsonPath("$.payload.productCount") { value(5) }
                jsonPath("$.payload.orderCount") { value(7) }
                jsonPath("$.payload.paidOrderCount") { value(4) }
                jsonPath("$.payload.refundedOrderCount") { value(1) }
                jsonPath("$.payload.issuedCouponCount") { value(9) }
            }
    }
}
