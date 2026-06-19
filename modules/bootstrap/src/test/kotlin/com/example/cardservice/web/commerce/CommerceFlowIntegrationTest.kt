package com.example.cardservice.web.commerce

import com.example.cardservice.TestcontainersConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "testcontainers.enabled", matches = "true")
class CommerceFlowIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun cleanDatabase() {
        jdbcTemplate.update("delete from coupon_histories")
        jdbcTemplate.update("delete from coupons")
        jdbcTemplate.update("delete from order_lines")
        jdbcTemplate.update("delete from commerce_orders")
        jdbcTemplate.update("delete from payments")
        jdbcTemplate.update("delete from inventories")
        jdbcTemplate.update("delete from products")
        jdbcTemplate.update("delete from members")
    }

    @Test
    fun `주문 결제는 쿠폰을 한 번만 발급하고 전체 환불은 쿠폰을 무효화한다`() {
        val memberId = createMember()
        val productId = createProduct(price = 12_000)
        createInventory(productId = productId, quantity = 3)
        val orderId = createOrder(memberId = memberId, productId = productId, quantity = 1)

        payOrder(orderId = orderId, idempotencyKey = "pay-integration-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.orderStatus") { value("PAID") }
                jsonPath("$.data.issuedCouponCount") { value(2) }
            }

        payOrder(orderId = orderId, idempotencyKey = "pay-integration-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.orderStatus") { value("PAID") }
                jsonPath("$.data.issuedCouponCount") { value(2) }
            }

        mockMvc.get("/api/members/$memberId/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.data", hasSize<Any>(2))
                jsonPath("$.data[0].status") { value("ISSUED") }
            }

        mockMvc.post("/api/orders/$orderId/refund")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.orderStatus") { value("REFUNDED") }
                jsonPath("$.data.voidedCouponCount") { value(2) }
            }

        mockMvc.get("/api/members/$memberId/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.data", hasSize<Any>(2))
                jsonPath("$.data[0].status") { value("VOIDED") }
            }
    }

    @Test
    fun `재고가 부족하면 주문 결제를 거절한다`() {
        val memberId = createMember()
        val productId = createProduct(price = 5_000)
        createInventory(productId = productId, quantity = 0)
        val orderId = createOrder(memberId = memberId, productId = productId, quantity = 1)

        payOrder(orderId = orderId, idempotencyKey = "pay-no-stock")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("재고가 부족합니다.") }
            }
    }

    private fun createMember(): Long =
        extractId(
            mockMvc.post("/api/members") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"Kim","email":"kim@example.com"}"""
            },
        )

    private fun createProduct(price: Long): Long =
        extractId(
            mockMvc.post("/api/products") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"Americano","price":$price}"""
            },
        )

    private fun createInventory(productId: Long, quantity: Long) {
        mockMvc.post("/api/products/$productId/inventory") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"quantity":$quantity}"""
        }
            .andExpect { status { isCreated() } }
    }

    private fun createOrder(memberId: Long, productId: Long, quantity: Long): Long =
        extractId(
            mockMvc.post("/api/orders") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                      "memberId": $memberId,
                      "lines": [{"productId": $productId, "quantity": $quantity}]
                    }
                """.trimIndent()
            },
        )

    private fun payOrder(orderId: Long, idempotencyKey: String): ResultActionsDsl =
        mockMvc.post("/api/orders/$orderId/pay") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idempotencyKey":"$idempotencyKey"}"""
        }

    private fun extractId(result: ResultActionsDsl): Long {
        val content = result
            .andExpect { status { isCreated() } }
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readTree(content).path("data").path("id").asLong()
    }
}
