package com.farmai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.farmai.core.data.local.entity.ReceiptJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptJobDao {
    @Query("SELECT * FROM receipt_jobs ORDER BY createdAt DESC")
    fun observeAllJobs(): Flow<List<ReceiptJobEntity>>

    @Query("SELECT * FROM receipt_jobs WHERE batchId = :batchId ORDER BY createdAt ASC")
    fun observeJobsByBatch(batchId: String): Flow<List<ReceiptJobEntity>>

    @Query("SELECT * FROM receipt_jobs WHERE id = :id")
    fun getJobById(id: String): Flow<ReceiptJobEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: ReceiptJobEntity)

    @Update
    suspend fun updateJob(job: ReceiptJobEntity)

    @Query("UPDATE receipt_jobs SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateJobStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE receipt_jobs SET error = :error, attemptCount = attemptCount + 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markJobFailed(id: String, error: String?, updatedAt: Long)

    @Query("DELETE FROM receipt_jobs WHERE id = :id")
    suspend fun deleteJob(id: String)

    @Query("DELETE FROM receipt_jobs WHERE batchId = :batchId")
    suspend fun deleteJobsByBatch(batchId: String)
}
