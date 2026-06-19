package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.required.DashboardQueryUseCase
import com.example.cardservice.application.commerce.response.toResponse
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Dashboard", description = "운영 메인 페이지 조회 API")
class DashboardController(
    private val dashboardQueryUseCase: DashboardQueryUseCase,
) {
    @GetMapping("/summary")
    @Operation(summary = "커머스 운영 요약 조회")
    fun getSummary(): ApiResponse<Any> =
        ApiResponse.success(dashboardQueryUseCase.getSummary().toResponse())
}
