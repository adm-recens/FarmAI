package com.farmai.core.data.repository

import androidx.room.withTransaction
import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.local.dao.ReceiptDao
import com.farmai.core.data.local.entity.DeductionEntity
import com.farmai.core.data.local.entity.ReceiptEntity
import com.farmai.core.data.local.entity.ReceiptLineItemEntity
import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.ParsedReceiptData
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ReceiptStatus
import com.farmai.core.domain.parser.ReceiptOcrParser
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
