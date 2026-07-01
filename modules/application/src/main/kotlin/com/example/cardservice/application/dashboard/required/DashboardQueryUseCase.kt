package com.example.cardservice.application.dashboard.required

import com.example.cardservice.application.dashboard.DashboardSummaryResponse

interface DashboardQueryUseCase {
    fun getSummary(): DashboardSummaryResponse
}
