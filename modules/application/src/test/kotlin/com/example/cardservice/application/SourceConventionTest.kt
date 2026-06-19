package com.example.cardservice.application

import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readText

class SourceConventionTest {
    @Test
    fun `프로젝트 코드 규칙에서 금지한 패턴을 사용하지 않는다`() {
        val root = findProjectRoot()
        val violations = Files.walk(root.resolve("modules"))
            .filter { it.name.endsWith(".kt") }
            .filter { !it.name.endsWith("ConventionTest.kt") }
            .filter { !it.toString().contains("/build/") }
            .flatMap { path ->
                val text = path.readText()
                forbiddenPatterns
                    .filter { pattern -> text.contains(pattern) }
                    .map { pattern -> "${root.relativize(path)} contains $pattern" }
                    .stream()
            }
            .toList()

        violations.shouldBeEmpty()
    }

    private fun findProjectRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (!Files.exists(current.resolve("settings.gradle.kts"))) {
            current = requireNotNull(current.parent) { "프로젝트 루트를 찾을 수 없습니다." }
        }
        return current
    }

    private companion object {
        val forbiddenPatterns = listOf(
            "import jakarta.validation.Valid",
            "import jakarta.validation.constraints.",
            "@Valid",
            "@get:NotBlank",
            "@field:NotBlank",
            "@get:Min",
            "@field:Min",
            "import org.springframework.data.jpa.repository.JpaRepository",
            "JpaRepository<",
            "saveAndFlush(",
            "@get:Transient",
            "payment_sequence",
            "payment_id",
            "PlaceCouponOrder",
            "CreateCouponOrderService",
            "CreateCouponOrderUseCase",
        )
    }
}
