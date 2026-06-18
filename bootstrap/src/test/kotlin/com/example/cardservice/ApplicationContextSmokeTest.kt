package com.example.cardservice

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class ApplicationContextSmokeTest : BehaviorSpec({
    given("the Spring Boot application") {
        `when`("the test context starts") {
            then("it loads with the MySQL test container") {
                // Context startup is the assertion for this scaffold smoke test.
            }
        }
    }
})
