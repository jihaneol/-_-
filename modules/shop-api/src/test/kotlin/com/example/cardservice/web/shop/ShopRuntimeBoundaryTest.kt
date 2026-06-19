package com.example.cardservice.web.shop

import com.example.cardservice.application.commerce.MemberResult
import com.example.cardservice.application.commerce.required.CouponQueryUseCase
import com.example.cardservice.application.commerce.required.MemberUseCase
import com.example.cardservice.application.commerce.required.OrderPaymentUseCase
import com.example.cardservice.application.commerce.required.OrderQueryUseCase
import com.example.cardservice.application.commerce.required.OrderUseCase
import com.example.cardservice.application.commerce.required.ProductQueryUseCase
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
}
