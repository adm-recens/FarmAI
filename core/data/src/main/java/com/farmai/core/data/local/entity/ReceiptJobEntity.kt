package com.farmai.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipt_jobs",
    foreignKeys = [
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("batchId"),
        Index("receiptId"),
        Index("status"),
        Index("createdAt")
    ]
)
data class ReceiptJobEntity(
    @PrimaryKey val id: String,
    val batchId: String,
    val receiptId: String? = null,
    val status: String = ReceiptJobStatus.QUEUED.name,
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
)

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
