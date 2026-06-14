package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Deduction(
    val id: String,
    val receiptId: String,
    val type: DeductionType,
    val amount: Double,
    val description: String? = null,
    val isPercentage: Boolean = false,
    val percentageValue: Double? = null
) {
    companion object {
        fun generateId() = java.util.UUID.randomUUID().toString()
    }
}

@Serializable
enum class DeductionType {
    COMMISSION, DAMAGES, UNLOADING, ADVANCE, OTHER
}