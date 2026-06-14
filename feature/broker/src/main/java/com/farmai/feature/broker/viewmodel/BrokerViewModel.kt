package com.farmai.feature.broker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.model.Broker
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.broker.DeleteBrokerUseCase
import com.farmai.core.domain.usecase.broker.GetAllBrokersUseCase
import com.farmai.core.domain.usecase.broker.GetBrokerByIdUseCase
import com.farmai.core.domain.usecase.broker.ObserveAllBrokersUseCase
import com.farmai.core.domain.usecase.broker.ObserveBrokerUseCase
import com.farmai.core.domain.usecase.broker.SaveBrokerUseCase
import com.farmai.core.domain.usecase.broker.SearchBrokersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class BrokerListViewModel @Inject constructor(
    private val getAllBrokersUseCase: GetAllBrokersUseCase,
    private val observeAllBrokersUseCase: ObserveAllBrokersUseCase,
    private val searchBrokersUseCase: SearchBrokersUseCase,
    private val deleteBrokerUseCase: DeleteBrokerUseCase
) : ViewModel() {

    private val _brokers = MutableStateFlow<List<Broker>>(emptyList())
    val brokers: StateFlow<List<Broker>> = _brokers

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeBrokers()
    }

    private fun observeBrokers() {
        viewModelScope.launch {
            observeAllBrokersUseCase(NoParams.INSTANCE).collect { brokerList ->
                _brokers.value = brokerList
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            observeBrokers()
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val results = searchBrokersUseCase(query)
                    _brokers.value = results
                } catch (e: Exception) {
                    _error.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun deleteBroker(broker: Broker) {
        viewModelScope.launch {
            try {
                deleteBrokerUseCase(broker.id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete broker: ${e.message}"
            }
        }
    }

    fun refresh() {
        observeBrokers()
    }
}

@HiltViewModel
class BrokerDetailViewModel @Inject constructor(
    private val getBrokerByIdUseCase: GetBrokerByIdUseCase,
    private val observeBrokerUseCase: ObserveBrokerUseCase,
    private val saveBrokerUseCase: SaveBrokerUseCase
) : ViewModel() {

    private val _broker = MutableStateFlow<Broker?>(null)
    val broker: StateFlow<Broker?> = _broker

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    fun loadBroker(brokerId: String?) {
        if (brokerId == null || brokerId.isBlank()) {
            _broker.value = null
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                observeBrokerUseCase(brokerId).collect { broker ->
                    _broker.value = broker
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveBroker(broker: Broker) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                saveBrokerUseCase(broker)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to save broker: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}