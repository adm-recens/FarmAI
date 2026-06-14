package com.farmai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.farmai.core.data.local.entity.BatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY updatedAt DESC")
    fun observeAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id")
    fun getBatchById(id: String): Flow<BatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity)

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Query("UPDATE batches SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBatchStatus(id: String, status: String, updatedAt: Long)

    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteBatch(id: String)
}
