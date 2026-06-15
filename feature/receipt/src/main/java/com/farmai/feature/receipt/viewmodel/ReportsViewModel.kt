package com.farmai.feature.receipt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.model.BrokerSettlement
import com.farmai.core.domain.model.DeductionAnalysis
import com.farmai.core.domain.model.FarmerSummary
import com.farmai.core.domain.model.MonthlyTrend
import com.farmai.core.domain.model.ReceiptExportRow
import com.farmai.core.domain.usecase.report.DateRangeParams
import com.farmai.core.domain.usecase.report.GetBrokerSettlementsUseCase
import com.farmai.core.domain.usecase.report.GetDeductionAnalysisUseCase
import com.farmai.core.domain.usecase.report.GetFarmerSummariesUseCase
import com.farmai.core.domain.usecase.report.GetAllReceiptsForExportUseCase
import com.farmai.core.domain.usecase.report.GetMonthlyTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getFarmerSummariesUseCase: GetFarmerSummariesUseCase,
    private val getBrokerSettlementsUseCase: GetBrokerSettlementsUseCase,
    private val getMonthlyTrendsUseCase: GetMonthlyTrendsUseCase,
    private val getDeductionAnalysisUseCase: GetDeductionAnalysisUseCase,
    private val getAllReceiptsForExportUseCase: GetAllReceiptsForExportUseCase
) : ViewModel() {
    private val _reportType = MutableStateFlow(ReportType.FARMERS)
    val reportType: StateFlow<ReportType> = _reportType

    private val _startDateText = MutableStateFlow("")
    val startDateText: StateFlow<String> = _startDateText

    private val _endDateText = MutableStateFlow("")
    val endDateText: StateFlow<String> = _endDateText

    private val _farmerSummaries = MutableStateFlow<List<FarmerSummary>>(emptyList())
    val farmerSummaries: StateFlow<List<FarmerSummary>> = _farmerSummaries

    private val _brokerSettlements = MutableStateFlow<List<BrokerSettlement>>(emptyList())
    val brokerSettlements: StateFlow<List<BrokerSettlement>> = _brokerSettlements

    private val _monthlyTrends = MutableStateFlow<List<MonthlyTrend>>(emptyList())
    val monthlyTrends: StateFlow<List<MonthlyTrend>> = _monthlyTrends

    private val _deductionAnalysis = MutableStateFlow<List<DeductionAnalysis>>(emptyList())
    val deductionAnalysis: StateFlow<List<DeductionAnalysis>> = _deductionAnalysis

    private val _exportRows = MutableStateFlow<List<ReceiptExportRow>>(emptyList())
    val exportRows: StateFlow<List<ReceiptExportRow>> = _exportRows

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setStartDate(value: String) {
        _startDateText.value = value
    }

    fun setEndDate(value: String) {
        _endDateText.value = value
    }

    fun setReportType(type: ReportType) {
        _reportType.value = type
    }

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val range = parseDateRange()
                when (_reportType.value) {
                    ReportType.FARMERS -> _farmerSummaries.value = getFarmerSummariesUseCase(range)
                    ReportType.BROKERS -> _brokerSettlements.value = getBrokerSettlementsUseCase(range)
                    ReportType.MONTHLY -> _monthlyTrends.value = getMonthlyTrendsUseCase(range)
                    ReportType.DEDUCTIONS -> _deductionAnalysis.value = getDeductionAnalysisUseCase(range)
                    ReportType.EXPORT_ROWS -> _exportRows.value = getAllReceiptsForExportUseCase(range)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseDateRange(): DateRangeParams {
        return DateRangeParams(
            startDate = parseEpochMillis(_startDateText.value),
            endDate = parseEpochMillis(_endDateText.value)
        )
    }

    private fun parseEpochMillis(value: String): Long? {
        if (value.isBlank()) return null
        return runCatching {
            LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrElse { error ->
            throw IllegalArgumentException("Use YYYY-MM-DD date format.", error)
        }
    }

    fun clearError() {
        _error.value = null
    }
}

enum class ReportType {
    FARMERS,
    BROKERS,
    MONTHLY,
    DEDUCTIONS,
    EXPORT_ROWS
}
