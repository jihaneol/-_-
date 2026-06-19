package com.example.cardservice.application.commerce.required

import com.example.cardservice.application.commerce.CommerceDashboardSummaryResult

interface DashboardQueryUseCase {
    fun getSummary(): CommerceDashboardSummaryResult
}
