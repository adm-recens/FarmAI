package com.farmai.feature.receipt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.model.Batch
import com.farmai.core.domain.usecase.batch.CreateBatchParams
import com.farmai.core.domain.usecase.batch.CreateBatchUseCase
import com.farmai.core.domain.usecase.batch.ObserveAllBatchesUseCase
import com.farmai.core.domain.usecase.NoParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class BatchListViewModel @Inject constructor(
    private val observeAllBatchesUseCase: ObserveAllBatchesUseCase,
    private val createBatchUseCase: CreateBatchUseCase
) : ViewModel() {
    private val _batches = MutableStateFlow<List<Batch>>(emptyList())
    val batches: StateFlow<List<Batch>> = _batches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeBatches()
    }

    private fun observeBatches() {
        viewModelScope.launch {
            observeAllBatchesUseCase(NoParams.INSTANCE).collect { batches ->
                _batches.value = batches
            }
        }
    }

    fun createBatch(name: String, notes: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                createBatchUseCase(CreateBatchParams(name, notes))
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
