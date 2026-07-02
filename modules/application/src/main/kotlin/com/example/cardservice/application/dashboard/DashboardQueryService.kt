package com.example.cardservice.application.dashboard

import com.example.cardservice.application.dashboard.provided.DashboardQueryPort
import com.example.cardservice.application.dashboard.required.DashboardQueryUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DashboardQueryService(
    private val dashboardQueryPort: DashboardQueryPort,
) : DashboardQueryUseCase {
    @Transactional(readOnly = true)
    override fun getSummary(): DashboardSummaryResponse =
        dashboardQueryPort.getSummary()
}
