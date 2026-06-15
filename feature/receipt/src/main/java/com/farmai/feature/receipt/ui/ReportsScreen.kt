package com.farmai.feature.receipt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.farmai.core.domain.model.BrokerSettlement
import com.farmai.core.domain.model.DeductionAnalysis
import com.farmai.core.domain.model.FarmerSummary
import com.farmai.core.domain.model.MonthlyTrend
import com.farmai.core.domain.model.ReceiptExportRow
import com.farmai.feature.receipt.R
import com.farmai.feature.receipt.viewmodel.ReportType
import com.farmai.feature.receipt.viewmodel.ReportsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val reportType by viewModel.reportType.collectAsState()
    val startDateText by viewModel.startDateText.collectAsState()
    val endDateText by viewModel.endDateText.collectAsState()
    val farmerSummaries by viewModel.farmerSummaries.collectAsState()
    val brokerSettlements by viewModel.brokerSettlements.collectAsState()
    val monthlyTrends by viewModel.monthlyTrends.collectAsState()
    val deductionAnalysis by viewModel.deductionAnalysis.collectAsState()
    val exportRows by viewModel.exportRows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val exportHistory by viewModel.exportHistory.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(context) {
        viewModel.loadExportHistory(context)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.reports_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.reports_date_range), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.reports_date_hint), modifier = Modifier.padding(top = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startDateText,
                                onValueChange = viewModel::setStartDate,
                                modifier = Modifier.weight(1f),
                                label = { Text(stringResource(R.string.reports_start_date)) },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endDateText,
                                onValueChange = viewModel::setEndDate,
                                modifier = Modifier.weight(1f),
                                label = { Text(stringResource(R.string.reports_end_date)) },
                                singleLine = true
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReportType.values().forEach { type ->
                                Button(
                                    onClick = { viewModel.setReportType(type) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    Text(type.label())
                                }
                            }
                        }
                        Button(
                            onClick = viewModel::loadReports,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            enabled = !isLoading
                        ) {
                            Text(stringResource(R.string.reports_apply_filters))
                        }
                        Button(
                            onClick = { viewModel.exportCurrentReport(context, asPdf = false) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            enabled = !isLoading
                        ) {
                            Text(stringResource(R.string.reports_export_csv_excel))
                        }
                        Button(
                            onClick = { viewModel.exportCurrentReport(context, asPdf = true) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            enabled = !isLoading
                        ) {
                            Text(stringResource(R.string.reports_export_pdf))
                        }
                    }
                }
            }

            if (exportHistory.isNotEmpty()) {
                item {
                    ExportHistorySection(exportHistory)
                }
            }

            item {
                if (isLoading) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (reportType) {
                        ReportType.FARMERS -> FarmerReportSection(farmerSummaries)
                        ReportType.BROKERS -> BrokerReportSection(brokerSettlements)
                        ReportType.MONTHLY -> MonthlyReportSection(monthlyTrends)
                        ReportType.DEDUCTIONS -> DeductionReportSection(deductionAnalysis)
                        ReportType.EXPORT_ROWS -> ExportRowsSection(exportRows)
                    }
                }
            }
        }
    }

    exportMessage?.let { currentMessage ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = currentMessage,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    error?.let { currentError ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                text = currentError,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun FarmerReportSection(items: List<FarmerSummary>) {
    ReportSectionTitle(stringResource(R.string.reports_farmer_summaries))
    if (items.isEmpty()) {
        EmptyReportMessage()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(item.farmerName, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.reports_farmer_code, item.farmerCode))
                        MetricGrid(
                            quantity = item.totalQuantity,
                            gross = item.totalGrossAmount,
                            deductions = item.totalDeductions,
                            net = item.totalNetAmount,
                            average = item.averagePrice,
                            count = item.receiptCount
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrokerReportSection(items: List<BrokerSettlement>) {
    ReportSectionTitle(stringResource(R.string.reports_broker_settlements))
    if (items.isEmpty()) {
        EmptyReportMessage()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(item.brokerName, fontWeight = FontWeight.Bold)
                        MetricGrid(
                            quantity = item.totalQuantity,
                            gross = item.totalGrossAmount,
                            deductions = item.totalCommission,
                            net = item.totalNetToFarmers,
                            average = 0.0,
                            count = item.receiptCount
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyReportSection(items: List<MonthlyTrend>) {
    ReportSectionTitle(stringResource(R.string.reports_monthly_trends))
    if (items.isEmpty()) {
        EmptyReportMessage()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${item.year}-${item.month}", fontWeight = FontWeight.Bold)
                        MetricGrid(
                            quantity = item.totalQuantity,
                            gross = item.totalGrossAmount,
                            deductions = item.totalGrossAmount - item.totalNetAmount,
                            net = item.totalNetAmount,
                            average = 0.0,
                            count = item.receiptCount
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeductionReportSection(items: List<DeductionAnalysis>) {
    ReportSectionTitle(stringResource(R.string.reports_deduction_analysis))
    if (items.isEmpty()) {
        EmptyReportMessage()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(item.type.name, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.reports_deduction_count, item.count))
                        Text(stringResource(R.string.reports_amount_percent, item.totalAmount.formatAmount(), item.percentageOfTotal.formatAmount()))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportRowsSection(items: List<ReceiptExportRow>) {
    ReportSectionTitle(stringResource(R.string.reports_export_rows))
    if (items.isEmpty()) {
        EmptyReportMessage()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(item.voucherNumber, fontWeight = FontWeight.Bold)
                        Text(item.voucherDate)
                        Text(stringResource(R.string.reports_farmer_broker, item.farmerName, item.brokerName))
                        MetricGrid(
                            quantity = item.quantity,
                            gross = item.grossAmount,
                            deductions = item.commission + item.damages + item.unloading + item.advance + item.otherDeductions,
                            net = item.netAmount,
                            average = 0.0,
                            count = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportHistorySection(items: List<ExportShare.ExportHistoryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.reports_export_history), fontWeight = FontWeight.Bold)
            items.forEach { item ->
                Text(
                    text = stringResource(
                        R.string.export_history_item,
                        item.timestamp,
                        item.title,
                        item.fileName,
                        item.rowCount
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReportSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyReportMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Text(
            text = stringResource(R.string.reports_no_data),
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricGrid(
    quantity: Double,
    gross: Double,
    deductions: Double,
    net: Double,
    average: Double,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricChip(stringResource(R.string.reports_qty, quantity.formatAmount()), Modifier.weight(1f))
        MetricChip(stringResource(R.string.reports_gross, gross.formatAmount()), Modifier.weight(1f))
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricChip(stringResource(R.string.reports_deductions_amount, deductions.formatAmount()), Modifier.weight(1f))
        MetricChip(stringResource(R.string.reports_net, net.formatAmount()), Modifier.weight(1f))
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricChip(stringResource(R.string.reports_avg_price, average.formatAmount()), Modifier.weight(1f))
        MetricChip(stringResource(R.string.reports_receipts, count), Modifier.weight(1f))
    }
}

@Composable
private fun MetricChip(label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(label, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

private fun ReportType.label(): String = when (this) {
    ReportType.FARMERS -> "Farmers"
    ReportType.BROKERS -> "Brokers"
    ReportType.MONTHLY -> "Monthly"
    ReportType.DEDUCTIONS -> "Deductions"
    ReportType.EXPORT_ROWS -> "Export"
}

private fun Double.formatAmount(): String = String.format(Locale.US, "%.2f", this)
