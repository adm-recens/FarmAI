package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FarmerSummary(
    val farmerId: String,
    val farmerName: String,
    val farmerCode: String,
    val totalQuantity: Double,
    val totalGrossAmount: Double,
    val totalDeductions: Double,
    val totalNetAmount: Double,
    val averagePrice: Double,
    val receiptCount: Int
)

@Serializable
data class BrokerSettlement(
    val brokerId: String,
    val brokerName: String,
    val totalQuantity: Double,
    val totalGrossAmount: Double,
    val totalCommission: Double,
    val totalNetToFarmers: Double,
    val receiptCount: Int
)

@Serializable
data class MonthlyTrend(
    val month: String,
    val year: Int,
    val totalQuantity: Double,
    val totalGrossAmount: Double,
    val totalNetAmount: Double,
    val receiptCount: Int
)

@Serializable
data class DeductionAnalysis(
    val type: DeductionType,
    val totalAmount: Double,
    val count: Int,
    val percentageOfTotal: Double
)

@Serializable
data class ReceiptExportRow(
    val voucherNumber: String,
    val voucherDate: String,
    val farmerCode: String,
    val farmerName: String,
    val brokerName: String,
    val quantity: Double,
    val grossAmount: Double,
    val commission: Double,
    val damages: Double,
    val unloading: Double,
    val advance: Double,
    val otherDeductions: Double,
    val netAmount: Double
)