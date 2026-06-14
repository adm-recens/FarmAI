package com.farmai.core.domain.mapper

import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.DeductionType
import com.farmai.core.domain.model.ParsedReceiptData
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ReceiptStatus

object ParsedReceiptMapper {
    fun toReceipt(
        parsed: ParsedReceiptData,
        existingReceipt: Receipt?,
        farmerId: String,
        brokerId: String
    ): Receipt {
        val now = System.currentTimeMillis()
        return Receipt(
            id = existingReceipt?.id ?: Receipt.generateId(),
            farmerId = farmerId.ifBlank { parsed.supplierCode.orEmpty() },
            brokerId = brokerId.ifBlank { parsed.brokerName.orEmpty() },
            voucherNumber = parsed.voucherNumber.orEmpty(),
            voucherDate = parsed.voucherDate ?: existingReceipt?.voucherDate ?: now,
            imagePaths = existingReceipt?.imagePaths.orEmpty(),
            ocrRawText = existingReceipt?.ocrRawText,
            status = existingReceipt?.status ?: ReceiptStatus.DRAFT,
            createdAt = existingReceipt?.createdAt ?: now,
            updatedAt = existingReceipt?.updatedAt ?: now
        )
    }

    fun toLineItems(parsed: ParsedReceiptData, receiptId: String): List<ReceiptLineItem> {
        return parsed.lineItems.mapIndexed { index, item ->
            ReceiptLineItem(
                id = ReceiptLineItem.generateId(),
                receiptId = receiptId,
                quantity = item.quantity,
                pricePerUnit = item.pricePerUnit,
                amount = item.amount,
                grade = item.grade,
                sortOrder = index
            )
        }
    }

    fun toDeductions(parsed: ParsedReceiptData, receiptId: String): List<Deduction> {
        val deductions = mutableListOf<Deduction>()

        if (parsed.commissionPercent != null || parsed.commissionAmount != null) {
            deductions += Deduction(
                id = Deduction.generateId(),
                receiptId = receiptId,
                type = DeductionType.COMMISSION,
                amount = parsed.commissionAmount ?: 0.0,
                isPercentage = parsed.commissionPercent != null,
                percentageValue = parsed.commissionPercent
            )
        }

        if (parsed.damagesAmount != null) {
            deductions += Deduction(
                id = Deduction.generateId(),
                receiptId = receiptId,
                type = DeductionType.DAMAGES,
                amount = parsed.damagesAmount
            )
        }

        if (parsed.unloadingAmount != null) {
            deductions += Deduction(
                id = Deduction.generateId(),
                receiptId = receiptId,
                type = DeductionType.UNLOADING,
                amount = parsed.unloadingAmount
            )
        }

        if (parsed.advanceAmount != null) {
            deductions += Deduction(
                id = Deduction.generateId(),
                receiptId = receiptId,
                type = DeductionType.ADVANCE,
                amount = parsed.advanceAmount
            )
        }

        parsed.otherDeductions.forEachIndexed { index, deduction ->
            deductions += Deduction(
                id = Deduction.generateId(),
                receiptId = receiptId,
                type = DeductionType.OTHER,
                amount = deduction.amount,
                description = deduction.description ?: "Other deduction ${index + 1}",
                isPercentage = deduction.isPercentage,
                percentageValue = deduction.percentageValue
            )
        }

        return deductions
    }
}
