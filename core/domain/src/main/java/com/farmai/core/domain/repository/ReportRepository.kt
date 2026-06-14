package com.farmai.core.domain.repository

import com.farmai.core.domain.model.DeductionAnalysis
import com.farmai.core.domain.model.FarmerSummary
import com.farmai.core.domain.model.MonthlyTrend
import com.farmai.core.domain.model.BrokerSettlement
import com.farmai.core.domain.model.ReceiptExportRow

interface ReportRepository {
    suspend fun getFarmerSummaries(startDate: Long?, endDate: Long?): List<FarmerSummary>
    suspend fun getBrokerSettlements(startDate: Long?, endDate: Long?): List<BrokerSettlement>
    suspend fun getMonthlyTrends(startDate: Long?, endDate: Long?): List<MonthlyTrend>
    suspend fun getDeductionAnalysis(startDate: Long?, endDate: Long?): List<DeductionAnalysis>
    suspend fun getAllReceiptsForExport(startDate: Long?, endDate: Long?): List<ReceiptExportRow>
}