package com.farmai.core.domain.model

data class Batch(
    val id: String,
    val name: String,
    val status: BatchStatus = BatchStatus.DRAFT,
    val totalImages: Int = 0,
    val processedCount: Int = 0,
    val validatedCount: Int = 0,
    val failedCount: Int = 0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateId() = java.util.UUID.randomUUID().toString()
    }
}

enum class BatchStatus {
    DRAFT,
    PROCESSING,
    NEEDS_VALIDATION,
    COMPLETED,
    EXPORTED,
    SYNCED,
    FAILED
}
