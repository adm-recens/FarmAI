package com.farmai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import androidx.room.Transaction
import androidx.room.Relation
import com.farmai.core.data.local.entity.DeductionEntity
import com.farmai.core.data.local.entity.ReceiptEntity
import com.farmai.core.data.local.entity.ReceiptLineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY voucherDate DESC, createdAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    fun getReceiptById(id: String): Flow<ReceiptEntity?>

    @Query("SELECT * FROM receipts WHERE farmerId = :farmerId ORDER BY voucherDate DESC")
    fun getReceiptsByFarmer(farmerId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE brokerId = :brokerId ORDER BY voucherDate DESC")
    fun getReceiptsByBroker(brokerId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE voucherDate BETWEEN :startDate AND :endDate ORDER BY voucherDate DESC")
    fun getReceiptsByDateRange(startDate: Long, endDate: Long): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE status = :status ORDER BY voucherDate DESC")
    fun getReceiptsByStatus(status: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE voucherNumber LIKE :query OR farmerId IN (SELECT id FROM farmers WHERE name LIKE :query OR code LIKE :query) ORDER BY voucherDate DESC")
    fun searchReceipts(query: String): List<ReceiptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReceipts(receipts: List<ReceiptEntity>)

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Query("UPDATE receipts SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReceiptStatus(id: String, status: String, updatedAt: Long)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceiptById(id: String)

    @Transaction
    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getReceiptWithDetails(id: String): ReceiptWithDetails?

    @Query("SELECT * FROM receipt_line_items WHERE receiptId = :receiptId ORDER BY sortOrder ASC")
    fun getLineItems(receiptId: String): Flow<List<ReceiptLineItemEntity>>

    @Query("SELECT * FROM deductions WHERE receiptId = :receiptId")
    fun getDeductions(receiptId: String): Flow<List<DeductionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineItem(item: ReceiptLineItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineItems(items: List<ReceiptLineItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeduction(deduction: DeductionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeductions(deductions: List<DeductionEntity>)

    @Query("DELETE FROM receipt_line_items WHERE receiptId = :receiptId")
    suspend fun deleteLineItemsByReceiptId(receiptId: String)

    @Query("DELETE FROM deductions WHERE receiptId = :receiptId")
    suspend fun deleteDeductionsByReceiptId(receiptId: String)
}

data class ReceiptWithDetails(
    @Embedded val receipt: ReceiptEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "receiptId"
    ) val lineItems: List<ReceiptLineItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "receiptId"
    ) val deductions: List<DeductionEntity>
)