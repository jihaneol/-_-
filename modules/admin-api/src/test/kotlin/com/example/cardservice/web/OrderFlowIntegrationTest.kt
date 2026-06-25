package com.example.cardservice.web

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
class OrderFlowIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun cleanDatabase() {
        jdbcTemplate.update("delete from processed_outbox_events")
        jdbcTemplate.update("delete from outbox_events")
        jdbcTemplate.update("delete from payment_operation_records")
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
                jsonPath("$.payload.orderStatus") { value("PAID") }
                jsonPath("$.payload.issuedCouponCount") { value(2) }
            }
        assertOutboxCount("PAYMENT_AUTHORIZED", orderId, 1)
        assertProjectionCount("PAYMENT_AUTHORIZED", orderId, 0)

        payOrder(orderId = orderId, idempotencyKey = "pay-integration-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.orderStatus") { value("PAID") }
                jsonPath("$.payload.issuedCouponCount") { value(2) }
            }
        assertOutboxCount("PAYMENT_AUTHORIZED", orderId, 1)
        assertProjectionCount("PAYMENT_AUTHORIZED", orderId, 0)

        mockMvc.get("/api/admin/members/$memberId/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.coupons", hasSize<Any>(2))
                jsonPath("$.payload.coupons[0].status") { value("ISSUED") }
            }

        mockMvc.post("/api/admin/orders/$orderId/refund")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.orderStatus") { value("REFUNDED") }
                jsonPath("$.payload.voidedCouponCount") { value(2) }
            }
        assertOutboxCount("PAYMENT_REFUNDED", orderId, 1)
        assertProjectionCount("PAYMENT_REFUNDED", orderId, 0)

        mockMvc.get("/api/admin/members/$memberId/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.payload.coupons", hasSize<Any>(2))
                jsonPath("$.payload.coupons[0].status") { value("VOIDED") }
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
                jsonPath("$.result.message") { value("재고가 부족합니다.") }
            }
    }

    private fun createMember(): Long =
        extractId(
            mockMvc.post("/api/admin/members") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"Kim","email":"kim@example.com"}"""
            },
        )

    private fun createProduct(price: Long): Long =
        extractId(
            mockMvc.post("/api/admin/products") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"Americano","price":$price}"""
            },
        )

    private fun createInventory(productId: Long, quantity: Long) {
        mockMvc.post("/api/admin/products/$productId/inventory") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"quantity":$quantity}"""
        }
            .andExpect { status { isCreated() } }
    }

    private fun createOrder(memberId: Long, productId: Long, quantity: Long): Long =
        extractId(
            mockMvc.post("/api/admin/orders") {
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
        mockMvc.post("/api/admin/orders/$orderId/pay") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idempotencyKey":"$idempotencyKey"}"""
        }

    private fun extractId(result: ResultActionsDsl): Long {
        val content = result
            .andExpect { status { isCreated() } }
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readTree(content).path("payload").path("id").asLong()
    }

    private fun assertProjectionCount(operationType: String, orderId: Long, expectedCount: Int) {
        val count = jdbcTemplate.queryForObject(
            """
                select count(*)
                from payment_operation_records
                where operation_type = ? and order_id = ?
            """.trimIndent(),
            Int::class.java,
            operationType,
            orderId,
        )
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(expectedCount)
    }

    private fun assertOutboxCount(eventType: String, orderId: Long, expectedCount: Int) {
        val count = jdbcTemplate.queryForObject(
            """
                select count(*)
                from outbox_events
                where event_type = ? and aggregate_id = ?
            """.trimIndent(),
            Int::class.java,
            eventType,
            orderId,
        )
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(expectedCount)
    }
}
