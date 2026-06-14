package com.farmai.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.farmai.core.domain.model.ReceiptLineItem

@Entity(
    tableName = "receipt_line_items",
    foreignKeys = [
        ForeignKey(entity = ReceiptEntity::class, parentColumns = ["id"], childColumns = ["receiptId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("receiptId")]
)
data class ReceiptLineItemEntity(
    @PrimaryKey val id: String,
    val receiptId: String,
    val quantity: Double,
    val pricePerUnit: Double,
    val amount: Double,
    val grade: String? = null,
    val sortOrder: Int = 0
) {
    fun toDomain(): ReceiptLineItem = ReceiptLineItem(
        id = id,
        receiptId = receiptId,
        quantity = quantity,
        pricePerUnit = pricePerUnit,
        amount = amount,
        grade = grade,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(item: ReceiptLineItem): ReceiptLineItemEntity = ReceiptLineItemEntity(
            id = item.id,
            receiptId = item.receiptId,
            quantity = item.quantity,
            pricePerUnit = item.pricePerUnit,
            amount = item.amount,
            grade = item.grade,
            sortOrder = item.sortOrder
        )
    }
}