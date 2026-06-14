package com.farmai.core.data.repository

import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.local.dao.BrokerSettlementEntity
import com.farmai.core.data.local.dao.DeductionAnalysisEntity
import com.farmai.core.data.local.dao.FarmerSummaryEntity
import com.farmai.core.data.local.dao.MonthlyTrendEntity
import com.farmai.core.data.local.dao.ReceiptExportRowEntity
import com.farmai.core.data.local.dao.ReportDao
import com.farmai.core.domain.model.BrokerSettlement
import com.farmai.core.domain.model.DeductionAnalysis
import com.farmai.core.domain.model.DeductionType
import com.farmai.core.domain.model.FarmerSummary
import com.farmai.core.domain.model.MonthlyTrend
import com.farmai.core.domain.model.ReceiptExportRow
import com.farmai.core.domain.repository.ReportRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ReportRepository {
    private val reportDao: ReportDao = database.reportDao()

    override suspend fun getFarmerSummaries(startDate: Long?, endDate: Long?): List<FarmerSummary> {
        return reportDao.getFarmerSummaries(startDate, endDate).first().map { it.toDomain() }
    }

    override suspend fun getBrokerSettlements(startDate: Long?, endDate: Long?): List<BrokerSettlement> {
        return reportDao.getBrokerSettlements(startDate, endDate).first().map { it.toDomain() }
    }

    override suspend fun getMonthlyTrends(startDate: Long?, endDate: Long?): List<MonthlyTrend> {
        return reportDao.getMonthlyTrends(startDate, endDate).first().map { it.toDomain() }
    }

    override suspend fun getDeductionAnalysis(startDate: Long?, endDate: Long?): List<DeductionAnalysis> {
        return reportDao.getDeductionAnalysis(startDate, endDate).first().map { it.toDomain() }
    }

    override suspend fun getAllReceiptsForExport(startDate: Long?, endDate: Long?): List<ReceiptExportRow> {
        return reportDao.getAllReceiptsForExport(startDate, endDate).first().map { it.toDomain() }
    }
}

private fun FarmerSummaryEntity.toDomain(): FarmerSummary = FarmerSummary(
    farmerId = farmerId,
    farmerName = farmerName,
    farmerCode = farmerCode,
    totalQuantity = totalQuantity,
    totalGrossAmount = totalGrossAmount,
    totalDeductions = totalDeductions,
    totalNetAmount = totalNetAmount,
    averagePrice = averagePrice,
    receiptCount = receiptCount
)

private fun BrokerSettlementEntity.toDomain(): BrokerSettlement = BrokerSettlement(
    brokerId = brokerId,
    brokerName = brokerName,
    totalQuantity = totalQuantity,
    totalGrossAmount = totalGrossAmount,
    totalCommission = totalCommission,
    totalNetToFarmers = totalNetToFarmers,
    receiptCount = receiptCount
)

private fun MonthlyTrendEntity.toDomain(): MonthlyTrend = MonthlyTrend(
    month = month,
    year = year,
    totalQuantity = totalQuantity,
    totalGrossAmount = totalGrossAmount,
    totalNetAmount = totalNetAmount,
    receiptCount = receiptCount
)

private fun DeductionAnalysisEntity.toDomain(): DeductionAnalysis = DeductionAnalysis(
    type = DeductionType.valueOf(type),
    totalAmount = totalAmount,
    count = count,
    percentageOfTotal = percentageOfTotal
)

private fun ReceiptExportRowEntity.toDomain(): ReceiptExportRow = ReceiptExportRow(
    voucherNumber = voucherNumber,
    voucherDate = voucherDate,
    farmerCode = farmerCode,
    farmerName = farmerName,
    brokerName = brokerName,
    quantity = quantity,
    grossAmount = grossAmount,
    commission = commission,
    damages = damages,
    unloading = unloading,
    advance = advance,
    otherDeductions = otherDeductions,
    netAmount = netAmount
)
