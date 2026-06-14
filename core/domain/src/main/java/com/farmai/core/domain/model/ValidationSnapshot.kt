package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ValidationSnapshot(
    val id: String,
    val receiptId: String,
    val originalJson: String,
    val correctedJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "local-user",
    val source: String = "manual-validation"
) {
    companion object {
        fun generateId() = java.util.UUID.randomUUID().toString()
    }
}

@Serializable
enum class ValidationStatus {
    PENDING, NEEDS_REVIEW, VALIDATED
}
