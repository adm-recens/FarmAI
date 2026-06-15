package com.farmai.feature.receipt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.model.Batch
import com.farmai.core.domain.model.ReceiptJob
import com.farmai.core.domain.model.ReceiptJobStatus
import com.farmai.core.domain.usecase.batch.AddReceiptJobParams
import com.farmai.core.domain.usecase.batch.AddReceiptJobUseCase
import com.farmai.core.domain.usecase.batch.DeleteBatchUseCase
import com.farmai.core.domain.usecase.batch.DeleteJobUseCase
import com.farmai.core.domain.usecase.batch.MarkJobFailedParams
import com.farmai.core.domain.usecase.batch.MarkJobFailedUseCase
import com.farmai.core.domain.usecase.batch.ObserveBatchByIdUseCase
import com.farmai.core.domain.usecase.batch.ObserveJobsByBatchUseCase
import com.farmai.core.domain.usecase.batch.UpdateJobStatusParams
import com.farmai.core.domain.usecase.batch.UpdateJobStatusUseCase
import com.farmai.feature.receipt.ui.ExportShare
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class BatchDetailViewModel @Inject constructor(
    private val observeBatchByIdUseCase: ObserveBatchByIdUseCase,
    private val observeJobsByBatchUseCase: ObserveJobsByBatchUseCase,
    private val addReceiptJobUseCase: AddReceiptJobUseCase,
    private val updateJobStatusUseCase: UpdateJobStatusUseCase,
    private val markJobFailedUseCase: MarkJobFailedUseCase,
    private val deleteJobUseCase: DeleteJobUseCase,
    private val deleteBatchUseCase: DeleteBatchUseCase
) : ViewModel() {
    private val _batch = MutableStateFlow<Batch?>(null)
    val batch: StateFlow<Batch?> = _batch

    private val _jobs = MutableStateFlow<List<ReceiptJob>>(emptyList())
    val jobs: StateFlow<List<ReceiptJob>> = _jobs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadBatch(batchId: String) {
        viewModelScope.launch {
            observeBatchByIdUseCase(batchId).collect { batchData ->
                _batch.value = batchData
            }
        }
        viewModelScope.launch {
            observeJobsByBatchUseCase(batchId).collect { jobList ->
                _jobs.value = jobList
            }
        }
    }

    fun addReceiptJob(batchId: String, imagePath: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                addReceiptJobUseCase(AddReceiptJobParams(batchId, imagePath))
                _message.value = "Receipt image added to queue."
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateJobStatus(jobId: String, status: ReceiptJobStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                updateJobStatusUseCase(UpdateJobStatusParams(jobId, status))
                _message.value = "Job updated to ${status.name}."
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markJobFailed(jobId: String, error: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                markJobFailedUseCase(MarkJobFailedParams(jobId, error))
                _message.value = "Job marked as failed."
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                deleteJobUseCase(jobId)
                _message.value = "Job deleted."
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBatch(batchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                deleteBatchUseCase(batchId)
                _message.value = "Batch deleted."
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun shareBatchCsv(context: Context, batch: Batch, jobs: List<ReceiptJob>) {
        try {
            ExportShare.shareBatchCsv(context, batch, jobs)
            _message.value = "Batch CSV export started."
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to export batch CSV: ${e.message}"
        }
    }

    fun shareBatchPdf(context: Context, batch: Batch, jobs: List<ReceiptJob>) {
        try {
            ExportShare.shareBatchPdf(context, batch, jobs)
            _message.value = "Batch PDF export started."
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to export batch PDF: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessage() {
        _message.value = null
    }
}
