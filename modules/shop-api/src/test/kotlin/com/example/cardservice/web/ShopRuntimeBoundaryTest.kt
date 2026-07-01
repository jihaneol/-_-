package com.example.cardservice.web

import com.example.cardservice.application.member.MemberResponse
import com.example.cardservice.application.member.provided.MemberRepository
import com.example.cardservice.application.coupon.CouponHistoryPageResponse
import com.example.cardservice.application.coupon.CouponHistoryResponse
import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.coupon.CouponPageResponse
import com.example.cardservice.application.coupon.CouponResponse
import com.example.cardservice.application.coupon.CouponWalletResponse
import com.example.cardservice.application.product.ProductPageResponse
import com.example.cardservice.application.product.ProductResponse
import com.example.cardservice.application.coupon.required.CouponQueryUseCase
import com.example.cardservice.application.member.required.MemberUseCase
import com.example.cardservice.application.order.required.OrderPaymentUseCase
import com.example.cardservice.application.order.required.OrderQueryUseCase
import com.example.cardservice.application.order.required.OrderUseCase
import com.example.cardservice.application.product.required.ProductQueryUseCase
import com.example.cardservice.web.coupon.ShopCouponController
import com.example.cardservice.web.member.ShopMemberController
import com.example.cardservice.web.order.ShopOrderController
import com.example.cardservice.web.payment.ShopOrderPaymentController
import com.example.cardservice.web.product.ShopProductController
import com.example.cardservice.domain.coupon.CouponHistoryType
import com.example.cardservice.domain.coupon.CouponStatus
import com.example.cardservice.domain.member.MemberRole
import com.example.cardservice.domain.product.ProductSaleStatus
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(
    controllers = [
        ShopMemberController::class,
        ShopProductController::class,
        ShopOrderController::class,
        ShopOrderPaymentController::class,
        ShopCouponController::class,
    ],
)
@AutoConfigureMockMvc(addFilters = false)
class ShopRuntimeBoundaryTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var memberUseCase: MemberUseCase

    @MockitoBean
    lateinit var productQueryUseCase: ProductQueryUseCase

    @MockitoBean
    lateinit var orderUseCase: OrderUseCase

    @MockitoBean
    lateinit var orderQueryUseCase: OrderQueryUseCase

    @MockitoBean
    lateinit var orderPaymentUseCase: OrderPaymentUseCase

    @MockitoBean
    lateinit var couponQueryUseCase: CouponQueryUseCase

    @MockitoBean
    lateinit var memberRepository: MemberRepository

    @Test
    fun `shop runtime exposes signup under shop namespace`() {
        given(memberUseCase.createMember(any())).willReturn(
            MemberResponse(id = 1L, username = "kim", name = "Kim", email = "kim@example.com", role = MemberRole.USER),
        )

        mockMvc.post("/api/shop/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"username":"kim","password":"password1","name":"Kim","email":"kim@example.com"}"""
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.payload.id") { value(1) }
            }
    }

    @Test
    fun `shop runtime does not expose admin dashboard route`() {
        mockMvc.get("/api/admin/dashboard/summary")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `shop runtime exposes customer coupon wallet summary`() {
        given(couponQueryUseCase.getCouponWallet(1L)).willReturn(
            CouponWalletResponse(
                memberId = 1L,
                issuedCouponCount = 7L,
                exchangedCouponCount = 10L,
                voidedCouponCount = 0L,
                totalCouponCount = 17L,
                exchangeableSetCount = 0L,
                remainingToNextExchange = 3L,
                recentHistories = emptyList(),
            ),
        )

        mockMvc.get("/api/shop/members/1/coupon-wallet")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.issuedCouponCount") { value(7) }
                jsonPath("$.payload.remainingToNextExchange") { value(3) }
            }
    }

    @Test
    fun `shop runtime exposes paginated customer coupons`() {
        given(couponQueryUseCase.listCoupons(1L, Pagination(0, 20, "id,desc"))).willReturn(
            CouponPageResponse(
                items = listOf(CouponResponse(10L, 1L, 7L, 9L, CouponStatus.ISSUED)),
                page = 0,
                size = 20,
                totalElements = 1L,
                totalPages = 1,
                hasNext = false,
            ),
        )

        mockMvc.get("/api/shop/members/1/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.items[0].status") { value("ISSUED") }
                jsonPath("$.payload.totalElements") { value(1) }
                jsonPath("$.payload.hasNext") { value(false) }
            }
    }

    @Test
    fun `shop runtime exposes paginated customer coupon histories`() {
        given(couponQueryUseCase.listMemberCouponHistories(1L, Pagination(0, 20, "id,desc"))).willReturn(
            CouponHistoryPageResponse(
                items = listOf(CouponHistoryResponse(20L, 10L, 1L, 7L, 9L, CouponHistoryType.ISSUED)),
                page = 0,
                size = 20,
                totalElements = 1L,
                totalPages = 1,
                hasNext = false,
            ),
        )

        mockMvc.get("/api/shop/members/1/coupon-histories")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.items[0].type") { value("ISSUED") }
                jsonPath("$.payload.page") { value(0) }
                jsonPath("$.payload.totalPages") { value(1) }
            }
    }

    @Test
    fun `shop runtime product list exposes customer-safe coupon metadata`() {
        given(productQueryUseCase.listProducts(Pagination(0, 20, "id,desc"))).willReturn(
            ProductPageResponse(
                items = listOf(
                    ProductResponse(id = 1L, name = "Americano", price = 12_000L, saleStatus = ProductSaleStatus.ON_SALE),
                    ProductResponse(id = 2L, name = "Exchange Coffee", price = 5_000L, saleStatus = ProductSaleStatus.ON_SALE),
                ),
                page = 0,
                size = 20,
                totalElements = 2L,
                totalPages = 1,
                hasNext = false,
            ),
        )

        mockMvc.get("/api/shop/products")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.items[0].couponAccrualCount") { value(2) }
                jsonPath("$.payload.items[0].exchangeEligible") { value(false) }
                jsonPath("$.payload.items[1].couponAccrualCount") { value(1) }
                jsonPath("$.payload.items[1].exchangeEligible") { value(true) }
                jsonPath("$.payload.totalElements") { value(2) }
            }
    }
}
