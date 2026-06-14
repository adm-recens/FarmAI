package com.farmai.core.domain.model

data class ReceiptJob(
    val id: String,
    val batchId: String,
    val receiptId: String? = null,
    val status: ReceiptJobStatus = ReceiptJobStatus.QUEUED,
    val imagePath: String? = null,
    val cropBoxJson: String? = null,
    val ocrRawText: String? = null,
    val ocrLayoutJson: String? = null,
    val parserJson: String? = null,
    val confidenceScore: Double = 0.0,
    val error: String? = null,
    val attemptCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateId() = java.util.UUID.randomUUID().toString()
    }
}

enum class ReceiptJobStatus {
    QUEUED,
    CROPPING,
    OCR_RUNNING,
    PARSED,
    NEEDS_VALIDATION,
    VALIDATED,
    FAILED,
    DELETED
}
