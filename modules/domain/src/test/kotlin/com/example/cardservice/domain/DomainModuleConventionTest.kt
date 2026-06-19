package com.example.cardservice.domain

import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readText

class DomainModuleConventionTest {
    @Test
    fun `domain 모듈은 repository와 adapter 기술을 직접 알지 않는다`() {
        val root = findProjectRoot()
        val violations = Files.walk(root.resolve("modules/domain/src/main/kotlin"))
            .filter { it.name.endsWith(".kt") }
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
            "org.springframework.stereotype.",
            "org.springframework.data.repository",
            "org.springframework.data.jpa.repository",
            "com.querydsl.",
            "Repository",
            "JpaRepository",
            "Querydsl",
            "QueryDSL",
        )
    }
}
