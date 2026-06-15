package com.farmai.feature.receipt.viewmodel

import android.content.Context
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
import com.farmai.feature.receipt.ui.ExportShare
import com.farmai.feature.receipt.ui.ExportShare.ExportHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
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

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage

    private val _exportHistory = MutableStateFlow<List<ExportHistoryItem>>(emptyList())
    val exportHistory: StateFlow<List<ExportHistoryItem>> = _exportHistory

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

    fun exportCurrentReport(context: Context, asPdf: Boolean) {
        val reportType = _reportType.value.name
        val csv = when (_reportType.value) {
            ReportType.FARMERS -> buildFarmerCsv(_farmerSummaries.value)
            ReportType.BROKERS -> buildBrokerCsv(_brokerSettlements.value)
            ReportType.MONTHLY -> buildMonthlyCsv(_monthlyTrends.value)
            ReportType.DEDUCTIONS -> buildDeductionCsv(_deductionAnalysis.value)
            ReportType.EXPORT_ROWS -> buildExportRowCsv(_exportRows.value)
        }
        if (asPdf) {
            ExportShare.shareReportPdf(
                context = context,
                reportType = reportType,
                lines = listOf(
                    "FarmAI $reportType Report",
                    "Generated: ${LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
                    ""
                ) + csv.lines()
            )
        } else {
            ExportShare.shareReportCsv(context, reportType, csv)
        }
        _exportMessage.value = "Report export started"
        loadExportHistory(context)
    }

    fun loadExportHistory(context: Context) {
        _exportHistory.value = ExportShare.readExportHistory(context)
    }

    private fun buildFarmerCsv(items: List<FarmerSummary>): String {
        val builder = StringBuilder("Farmer,Farmer Code,Quantity,Gross,Deductions,Net,Average Price,Receipts\n")
        items.forEach { item ->
            builder.appendLine(
                "${csvCell(item.farmerName)},${csvCell(item.farmerCode)},${item.totalQuantity.csv()},${item.totalGrossAmount.csv()},${item.totalDeductions.csv()},${item.totalNetAmount.csv()},${item.averagePrice.csv()},${item.receiptCount}"
            )
        }
        return builder.toString()
    }

    private fun buildBrokerCsv(items: List<BrokerSettlement>): String {
        val builder = StringBuilder("Broker,Quantity,Gross,Commission,Net To Farmers,Receipts\n")
        items.forEach { item ->
            builder.appendLine(
                "${csvCell(item.brokerName)},${item.totalQuantity.csv()},${item.totalGrossAmount.csv()},${item.totalCommission.csv()},${item.totalNetToFarmers.csv()},${item.receiptCount}"
            )
        }
        return builder.toString()
    }

    private fun buildMonthlyCsv(items: List<MonthlyTrend>): String {
        val builder = StringBuilder("Month,Year,Quantity,Gross,Net,Receipts\n")
        items.forEach { item ->
            builder.appendLine("${item.month},${item.year},${item.totalQuantity.csv()},${item.totalGrossAmount.csv()},${item.totalNetAmount.csv()},${item.receiptCount}")
        }
        return builder.toString()
    }

    private fun buildDeductionCsv(items: List<DeductionAnalysis>): String {
        val builder = StringBuilder("Type,Total Amount,Count,Percentage Of Total\n")
        items.forEach { item ->
            builder.appendLine("${item.type.name},${item.totalAmount.csv()},${item.count},${item.percentageOfTotal.csv()}")
        }
        return builder.toString()
    }

    private fun buildExportRowCsv(items: List<ReceiptExportRow>): String {
        val builder = StringBuilder("Voucher,Date,Farmer Code,Farmer,Broker,Quantity,Gross,Commission,Damages,Unloading,Advance,Other Deductions,Net\n")
        items.forEach { item ->
            builder.appendLine(
                "${csvCell(item.voucherNumber)},${csvCell(item.voucherDate)},${csvCell(item.farmerCode)},${csvCell(item.farmerName)},${csvCell(item.brokerName)},${item.quantity.csv()},${item.grossAmount.csv()},${item.commission.csv()},${item.damages.csv()},${item.unloading.csv()},${item.advance.csv()},${item.otherDeductions.csv()},${item.netAmount.csv()}"
            )
        }
        return builder.toString()
    }

    private fun csvCell(value: String): String {
        return if (value.containsAny(',', '"', '\n', '\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun String.containsAny(vararg chars: Char): Boolean {
        return chars.any { contains(it) }
    }

    private fun Double.csv(): String = String.format(Locale.US, "%.2f", this)

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
