package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptLineItem(
    val id: String,
    val receiptId: String,
    val quantity: Double,
    val pricePerUnit: Double,
    val amount: Double,
    val grade: String? = null,
    val sortOrder: Int = 0
) {
    companion object {
        fun generateId() = java.util.UUID.randomUUID().toString()
    }
}