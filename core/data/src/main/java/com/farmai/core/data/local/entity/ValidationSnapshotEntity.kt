package com.farmai.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.farmai.core.domain.model.ValidationSnapshot

@Entity(
    tableName = "validation_snapshots",
    foreignKeys = [
        ForeignKey(entity = ReceiptEntity::class, parentColumns = ["id"], childColumns = ["receiptId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("receiptId"), Index("createdAt")]
)
data class ValidationSnapshotEntity(
    @PrimaryKey val id: String,
    val receiptId: String,
    val originalJson: String,
    val correctedJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "local-user",
    val source: String = "manual-validation"
) {
    fun toDomain(): ValidationSnapshot = ValidationSnapshot(
        id = id,
        receiptId = receiptId,
        originalJson = originalJson,
        correctedJson = correctedJson,
        createdAt = createdAt,
        createdBy = createdBy,
        source = source
    )

    companion object {
        fun fromDomain(snapshot: ValidationSnapshot): ValidationSnapshotEntity = ValidationSnapshotEntity(
            id = snapshot.id,
            receiptId = snapshot.receiptId,
            originalJson = snapshot.originalJson,
            correctedJson = snapshot.correctedJson,
            createdAt = snapshot.createdAt,
            createdBy = snapshot.createdBy,
            source = snapshot.source
        )
    }
}
