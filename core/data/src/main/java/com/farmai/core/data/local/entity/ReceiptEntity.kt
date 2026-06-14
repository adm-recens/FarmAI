package com.farmai.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptStatus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(entity = FarmerEntity::class, parentColumns = ["id"], childColumns = ["farmerId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BrokerEntity::class, parentColumns = ["id"], childColumns = ["brokerId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("farmerId"),
        Index("brokerId"),
        Index("voucherDate"),
        Index("status")
    ]
)
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val farmerId: String,
    val brokerId: String,
    val voucherNumber: String,
    val voucherDate: Long,
    val imagePathsJson: String = "[]",
    val ocrRawText: String? = null,
    val status: String = ReceiptStatus.DRAFT.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Receipt = Receipt(
        id = id,
        farmerId = farmerId,
        brokerId = brokerId,
        voucherNumber = voucherNumber,
        voucherDate = voucherDate,
        imagePaths = Json.decodeFromString(imagePathsJson),
        ocrRawText = ocrRawText,
        status = ReceiptStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(receipt: Receipt): ReceiptEntity {
            val imagePathsJson = Json.encodeToString(receipt.imagePaths)
            return ReceiptEntity(
                id = receipt.id,
                farmerId = receipt.farmerId,
                brokerId = receipt.brokerId,
                voucherNumber = receipt.voucherNumber,
                voucherDate = receipt.voucherDate,
                imagePathsJson = imagePathsJson,
                ocrRawText = receipt.ocrRawText,
                status = receipt.status.name,
                createdAt = receipt.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}