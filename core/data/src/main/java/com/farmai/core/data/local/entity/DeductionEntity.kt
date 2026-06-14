package com.farmai.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.DeductionType

@Entity(
    tableName = "deductions",
    foreignKeys = [
        ForeignKey(entity = ReceiptEntity::class, parentColumns = ["id"], childColumns = ["receiptId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("receiptId")]
)
data class DeductionEntity(
    @PrimaryKey val id: String,
    val receiptId: String,
    val type: String,
    val amount: Double,
    val description: String? = null,
    val isPercentage: Boolean = false,
    val percentageValue: Double? = null
) {
    fun toDomain(): Deduction = Deduction(
        id = id,
        receiptId = receiptId,
        type = DeductionType.valueOf(type),
        amount = amount,
        description = description,
        isPercentage = isPercentage,
        percentageValue = percentageValue
    )

    companion object {
        fun fromDomain(deduction: Deduction): DeductionEntity = DeductionEntity(
            id = deduction.id,
            receiptId = deduction.receiptId,
            type = deduction.type.name,
            amount = deduction.amount,
            description = deduction.description,
            isPercentage = deduction.isPercentage,
            percentageValue = deduction.percentageValue
        )
    }
}