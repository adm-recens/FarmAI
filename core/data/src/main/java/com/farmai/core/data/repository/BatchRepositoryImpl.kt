package com.farmai.core.data.repository

import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.local.dao.BatchDao
import com.farmai.core.data.local.dao.ReceiptJobDao
import com.farmai.core.data.local.entity.BatchEntity
import com.farmai.core.data.local.entity.BatchStatus as DataBatchStatus
import com.farmai.core.data.local.entity.ReceiptJobEntity
import com.farmai.core.data.local.entity.ReceiptJobStatus as DataReceiptJobStatus
import com.farmai.core.domain.model.Batch
import com.farmai.core.domain.model.BatchStatus
import com.farmai.core.domain.model.ReceiptJob
import com.farmai.core.domain.model.ReceiptJobStatus
import com.farmai.core.domain.repository.BatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : BatchRepository {
    private val batchDao: BatchDao = database.batchDao()
    private val receiptJobDao: ReceiptJobDao = database.receiptJobDao()

    override fun observeAllBatches(): Flow<List<Batch>> {
        return batchDao.observeAllBatches().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeBatchById(id: String): Flow<Batch?> {
        return batchDao.getBatchById(id).map { it?.toDomain() }
    }

    override fun observeJobsByBatch(batchId: String): Flow<List<ReceiptJob>> {
        return receiptJobDao.observeJobsByBatch(batchId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getBatchById(id: String): Batch? {
        return batchDao.getBatchById(id).first()?.toDomain()
    }

    override suspend fun createBatch(name: String, notes: String?): Batch {
        val batch = BatchEntity(
            id = Batch.generateId(),
            name = name.ifBlank { "Untitled batch" },
            notes = notes,
            status = DataBatchStatus.DRAFT.name
        )
        batchDao.insertBatch(batch)
        return batch.toDomain()
    }

    override suspend fun addReceiptJob(batchId: String, imagePath: String): ReceiptJob {
        val job = ReceiptJobEntity(
            id = ReceiptJob.generateId(),
            batchId = batchId,
            imagePath = imagePath,
            status = DataReceiptJobStatus.QUEUED.name
        )
        receiptJobDao.insertJob(job)
        return job.toDomain()
    }

    override suspend fun updateJobStatus(jobId: String, status: ReceiptJobStatus) {
        receiptJobDao.updateJobStatus(jobId, status.name, System.currentTimeMillis())
    }

    override suspend fun markJobFailed(jobId: String, error: String?) {
        receiptJobDao.markJobFailed(jobId, error, System.currentTimeMillis())
    }

    override suspend fun deleteJob(jobId: String) {
        receiptJobDao.deleteJob(jobId)
    }

    override suspend fun deleteBatch(batchId: String) {
        receiptJobDao.deleteJobsByBatch(batchId)
        batchDao.deleteBatch(batchId)
    }
}

private fun BatchEntity.toDomain(): Batch = Batch(
    id = id,
    name = name,
    status = BatchStatus.valueOf(status),
    totalImages = totalImages,
    processedCount = processedCount,
    validatedCount = validatedCount,
    failedCount = failedCount,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun ReceiptJobEntity.toDomain(): ReceiptJob = ReceiptJob(
    id = id,
    batchId = batchId,
    receiptId = receiptId,
    status = ReceiptJobStatus.valueOf(status),
    imagePath = imagePath,
    cropBoxJson = cropBoxJson,
    ocrRawText = ocrRawText,
    ocrLayoutJson = ocrLayoutJson,
    parserJson = parserJson,
    confidenceScore = confidenceScore,
    error = error,
    attemptCount = attemptCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)
