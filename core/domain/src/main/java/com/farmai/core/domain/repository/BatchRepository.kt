package com.farmai.core.domain.repository

import com.farmai.core.domain.model.Batch
import com.farmai.core.domain.model.ReceiptJob
import com.farmai.core.domain.model.ReceiptJobStatus
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    fun observeAllBatches(): Flow<List<Batch>>
    fun observeBatchById(id: String): Flow<Batch?>
    fun observeJobById(id: String): Flow<ReceiptJob?>
    fun observeJobsByBatch(batchId: String): Flow<List<ReceiptJob>>
    suspend fun getBatchById(id: String): Batch?
    suspend fun getJobById(id: String): ReceiptJob?
    suspend fun createBatch(name: String, notes: String?): Batch
    suspend fun addReceiptJob(batchId: String, imagePath: String): ReceiptJob
    suspend fun updateJobStatus(jobId: String, status: ReceiptJobStatus)
    suspend fun updateJobCropBox(jobId: String, cropBoxJson: String, confidenceScore: Double)
    suspend fun markJobFailed(jobId: String, error: String?)
    suspend fun deleteJob(jobId: String)
    suspend fun deleteBatch(batchId: String)
}
