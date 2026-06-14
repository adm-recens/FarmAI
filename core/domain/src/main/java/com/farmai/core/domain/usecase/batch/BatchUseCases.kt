package com.farmai.core.domain.usecase.batch

import com.farmai.core.domain.model.Batch
import com.farmai.core.domain.model.ReceiptJob
import com.farmai.core.domain.model.ReceiptJobStatus
import com.farmai.core.domain.repository.BatchRepository
import com.farmai.core.domain.usecase.FlowUseCase
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllBatchesUseCase @Inject constructor(
    private val repository: BatchRepository
) : FlowUseCase<NoParams, List<Batch>> {
    override operator fun invoke(params: NoParams): Flow<List<Batch>> = repository.observeAllBatches()
}

class ObserveBatchByIdUseCase @Inject constructor(
    private val repository: BatchRepository
) : FlowUseCase<String, Batch?> {
    override operator fun invoke(id: String): Flow<Batch?> = repository.observeBatchById(id)
}

class ObserveJobsByBatchUseCase @Inject constructor(
    private val repository: BatchRepository
) : FlowUseCase<String, List<ReceiptJob>> {
    override operator fun invoke(batchId: String): Flow<List<ReceiptJob>> = repository.observeJobsByBatch(batchId)
}

class CreateBatchUseCase @Inject constructor(
    private val repository: BatchRepository
) : UseCase<CreateBatchParams, Batch> {
    override suspend operator fun invoke(params: CreateBatchParams): Batch {
        return repository.createBatch(params.name, params.notes)
    }
}

class AddReceiptJobUseCase @Inject constructor(
    private val repository: BatchRepository
) : UseCase<AddReceiptJobParams, ReceiptJob> {
    override suspend operator fun invoke(params: AddReceiptJobParams): ReceiptJob {
        return repository.addReceiptJob(params.batchId, params.imagePath)
    }
}

class UpdateJobStatusUseCase @Inject constructor(
    private val repository: BatchRepository
) : UseCase<UpdateJobStatusParams, Unit> {
    override suspend operator fun invoke(params: UpdateJobStatusParams): Unit {
        repository.updateJobStatus(params.jobId, params.status)
    }
}

class MarkJobFailedUseCase @Inject constructor(
    private val repository: BatchRepository
) : UseCase<MarkJobFailedParams, Unit> {
    override suspend operator fun invoke(params: MarkJobFailedParams): Unit {
        repository.markJobFailed(params.jobId, params.error)
    }
}

class DeleteJobUseCase @Inject constructor(
    private val repository: BatchRepository
) : UseCase<String, Unit> {
    override suspend operator fun invoke(jobId: String): Unit {
        repository.deleteJob(jobId)
    }
}

class DeleteBatchUseCase @Inject constructor(
    private val repository: BatchRepository
) : UseCase<String, Unit> {
    override suspend operator fun invoke(batchId: String): Unit {
        repository.deleteBatch(batchId)
    }
}

data class CreateBatchParams(
    val name: String,
    val notes: String? = null
)

data class AddReceiptJobParams(
    val batchId: String,
    val imagePath: String
)

data class UpdateJobStatusParams(
    val jobId: String,
    val status: ReceiptJobStatus
)

data class MarkJobFailedParams(
    val jobId: String,
    val error: String? = null
)
