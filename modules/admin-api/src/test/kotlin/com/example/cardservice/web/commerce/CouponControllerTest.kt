package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.ApproveCouponExchangeResult
import com.example.cardservice.application.commerce.CouponConsistencyReportResult
import com.example.cardservice.application.commerce.CouponExchangeResult
import com.example.cardservice.application.commerce.CouponHistoryResult
import com.example.cardservice.application.commerce.CouponResult
import com.example.cardservice.application.commerce.MemberCouponConsistencyResult
import com.example.cardservice.application.commerce.OrderCouponConsistencyResult
import com.example.cardservice.application.commerce.required.CouponExchangeUseCase
import com.example.cardservice.application.commerce.required.CouponQueryUseCase
import com.example.cardservice.domain.commerce.model.CouponHistoryType
import com.example.cardservice.domain.commerce.model.CouponStatus
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(CouponController::class)
class CouponControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var couponQueryUseCase: CouponQueryUseCase

    @MockitoBean
    lateinit var couponExchangeUseCase: CouponExchangeUseCase

    @Test
    fun `쿠폰 교환 API는 교환된 쿠폰과 히스토리를 반환한다`() {
        given(couponExchangeUseCase.exchangeCoupon(10L)).willReturn(
            CouponExchangeResult(
                coupon = CouponResult(
                    id = 10L,
                    memberId = 3L,
                    orderId = 7L,
                    paymentId = 9L,
                    status = CouponStatus.EXCHANGED,
                ),
                history = CouponHistoryResult(
                    id = 20L,
                    couponId = 10L,
                    memberId = 3L,
                    orderId = 7L,
                    paymentId = 9L,
                    type = CouponHistoryType.EXCHANGED,
                ),
            ),
        )

        mockMvc.post("/api/admin/coupons/10/exchange")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value("SUCCESS") }
                jsonPath("$.data.coupon.status") { value("EXCHANGED") }
                jsonPath("$.data.history.type") { value("EXCHANGED") }
            }
    }

    @Test
    fun `쿠폰 교환 승인 API는 회원 상품과 교환 쿠폰 수를 반환한다`() {
        given(couponExchangeUseCase.approveCouponExchange(3L, com.example.cardservice.application.commerce.ApproveCouponExchangeInput(8L))).willReturn(
            ApproveCouponExchangeResult(
                memberId = 3L,
                productId = 8L,
                productName = "Americano",
                exchangedCouponCount = 10,
                remainingIssuedCouponCount = 0L,
                exchangedCouponIds = (1L..10L).toList(),
            ),
        )

        mockMvc.post("/api/admin/members/3/coupon-exchanges") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"productId":8}"""
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value("SUCCESS") }
                jsonPath("$.data.productName") { value("Americano") }
                jsonPath("$.data.exchangedCouponCount") { value(10) }
                jsonPath("$.data.remainingIssuedCouponCount") { value(0) }
            }
    }

    @Test
    fun `쿠폰 정합성 리포트 API는 회원과 주문 단위 상태를 반환한다`() {
        given(couponQueryUseCase.getCouponConsistencyReport()).willReturn(
            CouponConsistencyReportResult(
                consistent = true,
                totalCouponCount = 12L,
                totalIssueHistoryCount = 12L,
                totalVoidHistoryCount = 1L,
                totalExchangeHistoryCount = 10L,
                memberRows = listOf(
                    MemberCouponConsistencyResult(
                        memberId = 3L,
                        issuedCouponCount = 1L,
                        voidedCouponCount = 1L,
                        exchangedCouponCount = 10L,
                        issueHistoryCount = 12L,
                        voidHistoryCount = 1L,
                        exchangeHistoryCount = 10L,
                        exchangeableSetCount = 0L,
                        remainingToNextExchange = 9L,
                        consistent = true,
                    ),
                ),
                orderRows = listOf(
                    OrderCouponConsistencyResult(
                        orderId = 7L,
                        memberId = 3L,
                        issuedCouponCount = 1L,
                        voidedCouponCount = 1L,
                        exchangedCouponCount = 10L,
                        issueHistoryCount = 12L,
                        voidHistoryCount = 1L,
                        exchangeHistoryCount = 10L,
                        consistent = true,
                    ),
                ),
            ),
        )

        mockMvc.get("/api/admin/coupon-consistency")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value("SUCCESS") }
                jsonPath("$.data.consistent") { value(true) }
                jsonPath("$.data.totalExchangeHistoryCount") { value(10) }
                jsonPath("$.data.memberRows[0].memberId") { value(3) }
                jsonPath("$.data.memberRows[0].remainingToNextExchange") { value(9) }
                jsonPath("$.data.orderRows[0].orderId") { value(7) }
            }
    }
}
