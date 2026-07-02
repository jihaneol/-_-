package com.example.cardservice.application.dashboard.provided

import com.example.cardservice.application.dashboard.DashboardSummaryResponse

interface DashboardQueryPort {
    fun getSummary(): DashboardSummaryResponse
}
