package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Receipt(
    val id: String,
    val farmerId: String,
    val brokerId: String,
    val voucherNumber: String,
    val voucherDate: Long,
    val imagePaths: List<String> = emptyList(),
    val ocrRawText: String? = null,
    val status: ReceiptStatus = ReceiptStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateId() = java.util.UUID.randomUUID().toString()
    }
}

@Serializable
enum class ReceiptStatus {
    DRAFT, CONFIRMED, SYNCED
}