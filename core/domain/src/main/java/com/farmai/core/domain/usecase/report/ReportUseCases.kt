package com.farmai.core.domain.usecase.report

import com.farmai.core.domain.model.DeductionAnalysis
import com.farmai.core.domain.model.FarmerSummary
import com.farmai.core.domain.model.MonthlyTrend
import com.farmai.core.domain.model.BrokerSettlement
import com.farmai.core.domain.model.ReceiptExportRow
import com.farmai.core.domain.repository.ReportRepository
import com.farmai.core.domain.usecase.UseCase
import javax.inject.Inject

class GetFarmerSummariesUseCase @Inject constructor(
    private val repository: ReportRepository
) : UseCase<DateRangeParams?, List<FarmerSummary>> {
    override suspend operator fun invoke(params: DateRangeParams?): List<FarmerSummary> {
        return repository.getFarmerSummaries(params?.startDate, params?.endDate)
    }
}

class GetBrokerSettlementsUseCase @Inject constructor(
    private val repository: ReportRepository
) : UseCase<DateRangeParams?, List<BrokerSettlement>> {
    override suspend operator fun invoke(params: DateRangeParams?): List<BrokerSettlement> {
        return repository.getBrokerSettlements(params?.startDate, params?.endDate)
    }
}

class GetMonthlyTrendsUseCase @Inject constructor(
    private val repository: ReportRepository
) : UseCase<DateRangeParams?, List<MonthlyTrend>> {
    override suspend operator fun invoke(params: DateRangeParams?): List<MonthlyTrend> {
        return repository.getMonthlyTrends(params?.startDate, params?.endDate)
    }
}

class GetDeductionAnalysisUseCase @Inject constructor(
    private val repository: ReportRepository
) : UseCase<DateRangeParams?, List<DeductionAnalysis>> {
    override suspend operator fun invoke(params: DateRangeParams?): List<DeductionAnalysis> {
        return repository.getDeductionAnalysis(params?.startDate, params?.endDate)
    }
}

class GetAllReceiptsForExportUseCase @Inject constructor(
    private val repository: ReportRepository
) : UseCase<DateRangeParams?, List<ReceiptExportRow>> {
    override suspend operator fun invoke(params: DateRangeParams?): List<ReceiptExportRow> {
        return repository.getAllReceiptsForExport(params?.startDate, params?.endDate)
    }
}

data class DateRangeParams(
    val startDate: Long?,
    val endDate: Long?
)