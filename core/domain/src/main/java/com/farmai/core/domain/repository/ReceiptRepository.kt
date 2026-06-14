package com.farmai.core.domain.repository

import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.ParsedReceiptData
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ReceiptStatus
import kotlinx.coroutines.flow.Flow

interface ReceiptRepository {
    suspend fun getAllReceipts(): List<Receipt>
    suspend fun getReceiptById(id: String): Receipt?
    suspend fun getReceiptsByFarmer(farmerId: String): List<Receipt>
    suspend fun getReceiptsByBroker(brokerId: String): List<Receipt>
    suspend fun getReceiptsByDateRange(startDate: Long, endDate: Long): List<Receipt>
    suspend fun getReceiptsByStatus(status: ReceiptStatus): List<Receipt>
    fun observeAllReceipts(): Flow<List<Receipt>>
    fun observeReceiptsByFarmer(farmerId: String): Flow<List<Receipt>>
    fun observeReceiptsByDateRange(startDate: Long, endDate: Long): Flow<List<Receipt>>
    suspend fun saveReceipt(receipt: Receipt)
    suspend fun saveReceiptWithDetails(receipt: Receipt, lineItems: List<ReceiptLineItem>, deductions: List<Deduction>)
    suspend fun updateReceiptStatus(id: String, status: ReceiptStatus)
    suspend fun deleteReceipt(id: String)
    suspend fun getLineItems(receiptId: String): List<ReceiptLineItem>
    suspend fun getDeductions(receiptId: String): List<Deduction>
    suspend fun searchReceipts(query: String): List<Receipt>
}

interface ReceiptParserRepository {
    suspend fun parseReceiptImage(imagePath: String): ParsedReceiptData
    suspend fun parseReceiptText(rawText: String): ParsedReceiptData
}