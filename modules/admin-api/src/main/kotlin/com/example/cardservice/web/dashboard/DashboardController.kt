package com.example.cardservice.web.dashboard

import com.example.cardservice.application.dashboard.DashboardSummaryResponse
import com.example.cardservice.application.dashboard.required.DashboardQueryUseCase
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
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
    fun getSummary(): ResponseEntity<ApiResponse<DashboardSummaryResponse>> =
        dashboardQueryUseCase.getSummary().toApplicationResponse()
}
