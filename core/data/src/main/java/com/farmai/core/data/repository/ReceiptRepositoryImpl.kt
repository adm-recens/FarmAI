package com.farmai.core.data.repository

import androidx.room.withTransaction
import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.local.dao.ReceiptDao
import com.farmai.core.data.local.entity.DeductionEntity
import com.farmai.core.data.local.entity.ReceiptEntity
import com.farmai.core.data.local.entity.ReceiptLineItemEntity
import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.ParsedLineItem
import com.farmai.core.domain.model.ParsedReceiptData
import com.farmai.core.domain.model.ParsedDeduction
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ReceiptStatus
import com.farmai.core.domain.repository.ReceiptParserRepository
import com.farmai.core.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ReceiptRepository {
    private val receiptDao: ReceiptDao = database.receiptDao()

    override suspend fun getAllReceipts(): List<Receipt> {
        return receiptDao.getAllReceipts().first().map { it.toDomain() }
    }

    override suspend fun getReceiptById(id: String): Receipt? {
        return receiptDao.getReceiptById(id).first()?.toDomain()
    }

    override suspend fun getReceiptsByFarmer(farmerId: String): List<Receipt> {
        return receiptDao.getReceiptsByFarmer(farmerId).first().map { it.toDomain() }
    }

    override suspend fun getReceiptsByBroker(brokerId: String): List<Receipt> {
        return receiptDao.getReceiptsByBroker(brokerId).first().map { it.toDomain() }
    }

    override suspend fun getReceiptsByDateRange(startDate: Long, endDate: Long): List<Receipt> {
        return receiptDao.getReceiptsByDateRange(startDate, endDate).first().map { it.toDomain() }
    }

    override suspend fun getReceiptsByStatus(status: ReceiptStatus): List<Receipt> {
        return receiptDao.getReceiptsByStatus(status.name).first().map { it.toDomain() }
    }

    override fun observeAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeReceiptsByFarmer(farmerId: String): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByFarmer(farmerId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeReceiptsByDateRange(startDate: Long, endDate: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByDateRange(startDate, endDate).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveReceipt(receipt: Receipt) {
        receiptDao.insertReceipt(ReceiptEntity.fromDomain(receipt.copy(updatedAt = System.currentTimeMillis())))
    }

    override suspend fun saveReceiptWithDetails(receipt: Receipt, lineItems: List<ReceiptLineItem>, deductions: List<Deduction>) {
        database.withTransaction {
            val receiptEntity = ReceiptEntity.fromDomain(receipt.copy(updatedAt = System.currentTimeMillis()))
            receiptDao.insertReceipt(receiptEntity)

            receiptDao.deleteLineItemsByReceiptId(receipt.id)
            receiptDao.deleteDeductionsByReceiptId(receipt.id)

            if (lineItems.isNotEmpty()) {
                receiptDao.insertLineItems(lineItems.map { ReceiptLineItemEntity.fromDomain(it) })
            }
            if (deductions.isNotEmpty()) {
                receiptDao.insertDeductions(deductions.map { DeductionEntity.fromDomain(it) })
            }
        }
    }

    override suspend fun updateReceiptStatus(id: String, status: ReceiptStatus) {
        receiptDao.updateReceiptStatus(id, status.name, System.currentTimeMillis())
    }

    override suspend fun deleteReceipt(id: String) {
        database.withTransaction {
            receiptDao.deleteLineItemsByReceiptId(id)
            receiptDao.deleteDeductionsByReceiptId(id)
            receiptDao.deleteReceiptById(id)
        }
    }

    override suspend fun getLineItems(receiptId: String): List<ReceiptLineItem> {
        return receiptDao.getLineItems(receiptId).first().map { it.toDomain() }
    }

    override suspend fun getDeductions(receiptId: String): List<Deduction> {
        return receiptDao.getDeductions(receiptId).first().map { it.toDomain() }
    }

    override suspend fun searchReceipts(query: String): List<Receipt> {
        val searchQuery = "%$query%"
        return receiptDao.searchReceipts(searchQuery).map { it.toDomain() }
    }
}

@Singleton
class ReceiptParserRepositoryImpl @Inject constructor() : ReceiptParserRepository {
    override suspend fun parseReceiptImage(imagePath: String): ParsedReceiptData {
        return ParsedReceiptData()
    }

    override suspend fun parseReceiptText(rawText: String): ParsedReceiptData {
        return ReceiptOcrParser.parse(rawText)
    }
}

object ReceiptOcrParser {
    fun parse(rawText: String): ParsedReceiptData {
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        var brokerName: String? = null
        var brokerAddress: String? = null
        var brokerPhone: String? = null
        var voucherNumber: String? = null
        var voucherDate: Long? = null
        var supplierCode: String? = null
        val lineItems = mutableListOf<ParsedLineItem>()
        var commissionPercent: Double? = null
        var damagesAmount: Double? = null
        var unloadingAmount: Double? = null
        var advanceAmount: Double? = null
        val otherDeductions = mutableListOf<ParsedDeduction>()

        for (i in lines.indices) {
            val line = lines[i]
            val upperLine = line.uppercase()

            if (i < 3 && (upperLine.contains("BROS") || upperLine.contains("BRO") || upperLine.contains("AGENT"))) {
                brokerName = if (brokerName == null) line else "$brokerName $line"
            } else if (i < 4 && (upperLine.contains("ROAD") || upperLine.contains("HYDERABAD"))) {
                brokerAddress = line
            } else if (i < 5 && (upperLine.contains("PHONE") || upperLine.contains("040"))) {
                brokerPhone = line
            }

            if (upperLine.contains("VOUCHER NUMBER") || upperLine.contains("YOUCHER NUMBER")) {
                voucherNumber = extractAfterColon(line)?.trim()
            }

            if (upperLine.contains("VOUCHER DATE") || upperLine.contains("YOUCHER DATE")) {
                val dateStr = extractAfterColon(line)?.trim()
                voucherDate = parseReceiptDate(dateStr)
            }

            if (upperLine.contains("SUPP NAME") || upperLine.contains("SUPP NANE")) {
                supplierCode = extractAfterColon(line)?.trim()
            }

            if (upperLine.contains("COMM") && upperLine.contains("%")) {
                commissionPercent = extractPercentage(line)
            }

            if (upperLine.contains("DAMAGE") || upperLine.contains("LESS FOR")) {
                damagesAmount = extractAmount(line)
            }

            if (upperLine.contains("UNLOAD")) {
                unloadingAmount = extractAmount(line)
            }

            if (upperLine.contains("L/F") || upperLine.contains("CASH") && upperLine.contains("AND")) {
                advanceAmount = extractAmount(line)
            }
        }

        parseLineItems(lines, lineItems)

        return ParsedReceiptData(
            brokerName = brokerName,
            brokerAddress = brokerAddress,
            brokerPhone = brokerPhone,
            voucherNumber = voucherNumber,
            voucherDate = voucherDate,
            supplierCode = supplierCode,
            lineItems = lineItems,
            commissionPercent = commissionPercent,
            damagesAmount = damagesAmount,
            unloadingAmount = unloadingAmount,
            advanceAmount = advanceAmount,
            otherDeductions = otherDeductions
        )
    }

    private fun parseLineItems(lines: List<String>, lineItems: MutableList<ParsedLineItem>) {
        var inTable = false
        for (line in lines) {
            val upperLine = line.uppercase()
            if (upperLine.contains("QTY") && upperLine.contains("PRICE") && upperLine.contains("AMOUNT")) {
                inTable = true
                continue
            }
            if (inTable) {
                if (upperLine.contains("TOTAL") || upperLine.contains("COMM") || upperLine.contains("DAMAGE") || upperLine.contains("UNLOAD") || upperLine.contains("L/F") || upperLine.contains("GRAND")) {
                    inTable = false
                    continue
                }
                val parts = line.split(Regex("\\s+")).filter { it.isNotBlank() }
                if (parts.size >= 3) {
                    val qty = parts[0].toDoubleOrNull()
                    val price = parts[1].toDoubleOrNull()
                    val amount = parts[2].toDoubleOrNull()
                    if (qty != null && price != null && amount != null) {
                        lineItems.add(ParsedLineItem(qty, price, amount))
                    }
                }
            }
        }
    }

    private fun extractAfterColon(line: String): String? {
        val idx = line.indexOf(':')
        return if (idx >= 0 && idx < line.length - 1) line.substring(idx + 1) else null
    }

    private fun extractAmount(line: String): Double? {
        val regex = Regex("([0-9]+(?:\\.[0-9]+)?)")
        val matches = regex.findAll(line)
        val numbers = matches.map { it.groupValues[1].toDouble() }.toList()
        return numbers.lastOrNull()
    }

    private fun extractPercentage(line: String): Double? {
        val regex = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%")
        val match = regex.find(line)
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun parseReceiptDate(dateStr: String?): Long? {
        if (dateStr == null || dateStr.isBlank()) return null
        try {
            val formats = listOf(
                "dd/MM/yyyy",
                "dd-MM-yyyy",
                "ddMMyyyy",
                "yyyy-MM-dd"
            )
            for (format in formats) {
                try {
                    return java.text.SimpleDateFormat(format, java.util.Locale.getDefault()).parse(dateStr)?.time ?: continue
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
        }
        return null
    }
}
