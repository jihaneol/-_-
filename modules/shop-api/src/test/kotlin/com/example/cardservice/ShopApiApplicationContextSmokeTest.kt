package com.example.cardservice

import io.kotest.core.spec.style.BehaviorSpec
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@EnabledIfSystemProperty(named = "testcontainers.enabled", matches = "true")
class ShopApiApplicationContextSmokeTest : BehaviorSpec({
    given("the shop Spring Boot application") {
        `when`("the test context starts") {
            then("it loads with the MySQL test container") {
                // Context startup is the assertion for this runtime smoke test.
            }
        }
    }
})
