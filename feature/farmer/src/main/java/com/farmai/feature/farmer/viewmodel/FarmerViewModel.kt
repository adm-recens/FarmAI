package com.farmai.feature.farmer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.model.Farmer
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.farmer.DeleteFarmerUseCase
import com.farmai.core.domain.usecase.farmer.GetAllFarmersUseCase
import com.farmai.core.domain.usecase.farmer.GetFarmerByIdUseCase
import com.farmai.core.domain.usecase.farmer.ObserveAllFarmersUseCase
import com.farmai.core.domain.usecase.farmer.ObserveFarmerUseCase
import com.farmai.core.domain.usecase.farmer.SaveFarmerUseCase
import com.farmai.core.domain.usecase.farmer.SearchFarmersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class FarmerListViewModel @Inject constructor(
    private val getAllFarmersUseCase: GetAllFarmersUseCase,
    private val observeAllFarmersUseCase: ObserveAllFarmersUseCase,
    private val searchFarmersUseCase: SearchFarmersUseCase,
    private val deleteFarmerUseCase: DeleteFarmerUseCase
) : ViewModel() {

    private val _farmers = MutableStateFlow<List<Farmer>>(emptyList())
    val farmers: StateFlow<List<Farmer>> = _farmers

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeFarmers()
    }

    private fun observeFarmers() {
        viewModelScope.launch {
            observeAllFarmersUseCase(NoParams.INSTANCE).collect { farmerList ->
                _farmers.value = farmerList
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            observeFarmers()
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val results = searchFarmersUseCase(query)
                    _farmers.value = results
                } catch (e: Exception) {
                    _error.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun deleteFarmer(farmer: Farmer) {
        viewModelScope.launch {
            try {
                deleteFarmerUseCase(farmer.id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete farmer: ${e.message}"
            }
        }
    }

    fun refresh() {
        observeFarmers()
    }
}

@HiltViewModel
class FarmerDetailViewModel @Inject constructor(
    private val getFarmerByIdUseCase: GetFarmerByIdUseCase,
    private val observeFarmerUseCase: ObserveFarmerUseCase,
    private val saveFarmerUseCase: SaveFarmerUseCase
) : ViewModel() {

    private val _farmer = MutableStateFlow<Farmer?>(null)
    val farmer: StateFlow<Farmer?> = _farmer

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    fun loadFarmer(farmerId: String?) {
        if (farmerId == null || farmerId.isBlank()) {
            _farmer.value = null
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                observeFarmerUseCase(farmerId).collect { farmer ->
                    _farmer.value = farmer
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveFarmer(farmer: Farmer) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                saveFarmerUseCase(farmer)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to save farmer: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}