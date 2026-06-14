package com.farmai.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "batches",
    indices = [
        Index("status"),
        Index("createdAt"),
        Index("updatedAt")
    ]
)
data class BatchEntity(
    @PrimaryKey val id: String,
    val name: String,
    val status: String = BatchStatus.DRAFT.name,
    val totalImages: Int = 0,
    val processedCount: Int = 0,
    val validatedCount: Int = 0,
    val failedCount: Int = 0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BatchStatus {
    DRAFT,
    PROCESSING,
    NEEDS_VALIDATION,
    COMPLETED,
    EXPORTED,
    SYNCED,
    FAILED
}
