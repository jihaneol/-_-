package com.example.cardservice.web.coupon

import com.example.cardservice.application.coupon.ApproveCouponExchangeResult
import com.example.cardservice.application.coupon.CouponConsistencyReportResult
import com.example.cardservice.application.coupon.CouponExchangeResult
import com.example.cardservice.application.coupon.CouponHistoryPageResult
import com.example.cardservice.application.coupon.CouponHistoryResult
import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.coupon.CouponPageResult
import com.example.cardservice.application.coupon.CouponResult
import com.example.cardservice.application.coupon.MemberCouponConsistencyResult
import com.example.cardservice.application.coupon.OrderCouponConsistencyResult
import com.example.cardservice.application.coupon.required.CouponExchangeUseCase
import com.example.cardservice.application.coupon.required.CouponQueryUseCase
import com.example.cardservice.web.coupon.CouponController
import com.example.cardservice.domain.coupon.CouponHistoryType
import com.example.cardservice.domain.coupon.CouponStatus
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
    fun `회원 쿠폰 조회 API는 페이지 메타데이터와 쿠폰 목록을 반환한다`() {
        given(couponQueryUseCase.listCoupons(3L, Pagination(0, 2, "id,desc"))).willReturn(
            CouponPageResult(
                items = listOf(
                    CouponResult(id = 10L, memberId = 3L, orderId = 7L, paymentId = 9L, status = CouponStatus.ISSUED),
                ),
                page = 0,
                size = 2,
                totalElements = 3L,
                totalPages = 2,
                hasNext = true,
            ),
        )

        mockMvc.get("/api/admin/members/3/coupons?page=0&size=2&sort=id,desc")
            .andExpect {
                status { isOk() }
                jsonPath("$.result.code") { value("OK") }
                jsonPath("$.payload.items[0].id") { value(10) }
                jsonPath("$.payload.items[0].status") { value("ISSUED") }
                jsonPath("$.payload.page") { value(0) }
                jsonPath("$.payload.size") { value(2) }
                jsonPath("$.payload.totalElements") { value(3) }
                jsonPath("$.payload.totalPages") { value(2) }
                jsonPath("$.payload.hasNext") { value(true) }
            }
    }

    @Test
    fun `회원 쿠폰 히스토리 조회 API는 페이지 메타데이터와 히스토리 목록을 반환한다`() {
        given(couponQueryUseCase.listMemberCouponHistories(3L, Pagination(1, 5, "id,asc"))).willReturn(
            CouponHistoryPageResult(
                items = listOf(
                    CouponHistoryResult(
                        id = 20L,
                        couponId = 10L,
                        memberId = 3L,
                        orderId = 7L,
                        paymentId = 9L,
                        type = CouponHistoryType.ISSUED,
                    ),
                ),
                page = 1,
                size = 5,
                totalElements = 6L,
                totalPages = 2,
                hasNext = false,
            ),
        )

        mockMvc.get("/api/admin/members/3/coupon-histories?page=1&size=5&sort=id,asc")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.items[0].type") { value("ISSUED") }
                jsonPath("$.payload.page") { value(1) }
                jsonPath("$.payload.totalPages") { value(2) }
                jsonPath("$.payload.hasNext") { value(false) }
            }
    }

    @Test
    fun `주문 쿠폰 히스토리 조회 API는 페이지 쿼리로 조회한다`() {
        given(couponQueryUseCase.listOrderCouponHistories(7L, Pagination(0, 20, "id,desc"))).willReturn(
            CouponHistoryPageResult(
                items = emptyList(),
                page = 0,
                size = 20,
                totalElements = 0L,
                totalPages = 0,
                hasNext = false,
            ),
        )

        mockMvc.get("/api/admin/orders/7/coupon-histories")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.items") { isArray() }
                jsonPath("$.payload.size") { value(20) }
                jsonPath("$.payload.totalElements") { value(0) }
            }
    }

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
                jsonPath("$.result.code") { value("OK") }
                jsonPath("$.payload.coupon.status") { value("EXCHANGED") }
                jsonPath("$.payload.history.type") { value("EXCHANGED") }
            }
    }

    @Test
    fun `쿠폰 교환 승인 API는 회원 상품과 교환 쿠폰 수를 반환한다`() {
        given(couponExchangeUseCase.approveCouponExchange(3L, com.example.cardservice.application.coupon.ApproveCouponExchangeInput(8L))).willReturn(
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
                jsonPath("$.result.code") { value("OK") }
                jsonPath("$.payload.productName") { value("Americano") }
                jsonPath("$.payload.exchangedCouponCount") { value(10) }
                jsonPath("$.payload.remainingIssuedCouponCount") { value(0) }
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
                jsonPath("$.result.code") { value("OK") }
                jsonPath("$.payload.consistent") { value(true) }
                jsonPath("$.payload.totalExchangeHistoryCount") { value(10) }
                jsonPath("$.payload.memberRows[0].memberId") { value(3) }
                jsonPath("$.payload.memberRows[0].remainingToNextExchange") { value(9) }
                jsonPath("$.payload.orderRows[0].orderId") { value(7) }
            }
    }
}
