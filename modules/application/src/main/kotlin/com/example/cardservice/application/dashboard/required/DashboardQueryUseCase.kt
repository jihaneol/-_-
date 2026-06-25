package com.example.cardservice.application.dashboard.required

import com.example.cardservice.application.dashboard.DashboardSummaryResult

interface DashboardQueryUseCase {
    fun getSummary(): DashboardSummaryResult
}
