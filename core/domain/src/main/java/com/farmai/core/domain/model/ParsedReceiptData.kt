package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ParsedReceiptData(
    val brokerName: String? = null,
    val brokerAddress: String? = null,
    val brokerPhone: String? = null,
    val voucherNumber: String? = null,
    val voucherDate: Long? = null,
    val supplierCode: String? = null,
    val lineItems: List<ParsedLineItem> = emptyList(),
    val commissionPercent: Double? = null,
    val commissionAmount: Double? = null,
    val damagesAmount: Double? = null,
    val unloadingAmount: Double? = null,
    val advanceAmount: Double? = null,
    val otherDeductions: List<ParsedDeduction> = emptyList(),
    val confidenceScore: Double = 0.0,
    val fieldConfidence: Map<String, Double> = emptyMap()
)

@Serializable
data class ParsedLineItem(
    val quantity: Double,
    val pricePerUnit: Double,
    val amount: Double,
    val grade: String? = null,
    val confidence: Double = 1.0
)

@Serializable
data class ParsedDeduction(
    val type: DeductionType,
    val amount: Double,
    val description: String? = null,
    val isPercentage: Boolean = false,
    val percentageValue: Double? = null,
    val confidence: Double = 1.0
)
