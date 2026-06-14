package com.farmai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.farmai.core.data.local.entity.ValidationSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ValidationSnapshotDao {
    @Query("SELECT * FROM validation_snapshots WHERE receiptId = :receiptId ORDER BY createdAt DESC")
    fun getSnapshotsByReceiptId(receiptId: String): Flow<List<ValidationSnapshotEntity>>

    @Query("SELECT * FROM validation_snapshots WHERE receiptId = :receiptId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestSnapshotByReceiptId(receiptId: String): ValidationSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: ValidationSnapshotEntity)
}
