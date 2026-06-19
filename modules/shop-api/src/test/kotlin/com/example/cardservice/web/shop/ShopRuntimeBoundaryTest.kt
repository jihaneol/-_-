package com.example.cardservice.web.shop

import com.example.cardservice.application.commerce.MemberResult
import com.example.cardservice.application.commerce.CouponWalletResult
import com.example.cardservice.application.commerce.ProductResult
import com.example.cardservice.application.commerce.required.CouponQueryUseCase
import com.example.cardservice.application.commerce.required.MemberUseCase
import com.example.cardservice.application.commerce.required.OrderPaymentUseCase
import com.example.cardservice.application.commerce.required.OrderQueryUseCase
import com.example.cardservice.application.commerce.required.OrderUseCase
import com.example.cardservice.application.commerce.required.ProductQueryUseCase
import com.example.cardservice.domain.commerce.model.ProductSaleStatus
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
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

    @Test
    fun `shop runtime exposes signup under shop namespace`() {
        given(memberUseCase.createMember(any())).willReturn(MemberResult(id = 1L, name = "Kim", email = "kim@example.com"))

        mockMvc.post("/api/shop/members") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Kim","email":"kim@example.com"}"""
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.data.id") { value(1) }
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
            CouponWalletResult(
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
                jsonPath("$.data.issuedCouponCount") { value(7) }
                jsonPath("$.data.remainingToNextExchange") { value(3) }
            }
    }

    @Test
    fun `shop runtime product list exposes customer-safe coupon metadata`() {
        given(productQueryUseCase.listProducts()).willReturn(
            listOf(
                ProductResult(id = 1L, name = "Americano", price = 12_000L, saleStatus = ProductSaleStatus.ON_SALE),
                ProductResult(id = 2L, name = "Exchange Coffee", price = 5_000L, saleStatus = ProductSaleStatus.ON_SALE),
            ),
        )

        mockMvc.get("/api/shop/products")
            .andExpect {
                status { isOk() }
                jsonPath("$.data[0].couponAccrualCount") { value(2) }
                jsonPath("$.data[0].exchangeEligible") { value(false) }
                jsonPath("$.data[1].couponAccrualCount") { value(1) }
                jsonPath("$.data[1].exchangeEligible") { value(true) }
            }
    }
}
