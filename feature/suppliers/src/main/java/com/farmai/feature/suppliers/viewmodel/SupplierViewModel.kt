package com.farmai.feature.suppliers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.model.Supplier
import com.farmai.core.domain.model.SupplierMergeSuggestion
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.supplier.DeleteSupplierUseCase
import com.farmai.core.domain.usecase.supplier.GetAllSuppliersUseCase
import com.farmai.core.domain.usecase.supplier.GetSupplierByIdUseCase
import com.farmai.core.domain.usecase.supplier.ObserveAllSuppliersUseCase
import com.farmai.core.domain.usecase.supplier.ObserveSupplierUseCase
import com.farmai.core.domain.usecase.supplier.SaveSupplierUseCase
import com.farmai.core.domain.usecase.supplier.SearchSuppliersUseCase
import com.farmai.core.domain.usecase.supplier.SuggestMergeCandidatesUseCase
import com.farmai.core.domain.usecase.supplier.SuggestSupplierMatchesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierListViewModel @Inject constructor(
    private val getAllSuppliersUseCase: GetAllSuppliersUseCase,
    private val observeAllSuppliersUseCase: ObserveAllSuppliersUseCase,
    private val searchSuppliersUseCase: SearchSuppliersUseCase,
    private val deleteSupplierUseCase: DeleteSupplierUseCase,
    private val suggestSupplierMatchesUseCase: SuggestSupplierMatchesUseCase,
    private val suggestMergeCandidatesUseCase: SuggestMergeCandidatesUseCase
) : ViewModel() {

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers

    private val _mergeSuggestions = MutableStateFlow<List<SupplierMergeSuggestion>>(emptyList())
    val mergeSuggestions: StateFlow<List<SupplierMergeSuggestion>> = _mergeSuggestions

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeSuppliers()
        refreshMergeSuggestions()
    }

    private fun observeSuppliers() {
        viewModelScope.launch {
            observeAllSuppliersUseCase(NoParams.INSTANCE).collect { supplierList ->
                _suppliers.value = supplierList
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            observeSuppliers()
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val results = searchSuppliersUseCase(query)
                    _suppliers.value = results
                } catch (e: Exception) {
                    _error.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun onSupplierMatchQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            try {
                suggestSupplierMatchesUseCase(query)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                deleteSupplierUseCase(supplier.id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete supplier: ${e.message}"
            }
        }
    }

    fun refresh() {
        observeSuppliers()
        refreshMergeSuggestions()
    }

    private fun refreshMergeSuggestions() {
        viewModelScope.launch {
            try {
                _mergeSuggestions.value = suggestMergeCandidatesUseCase(NoParams.INSTANCE)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

@HiltViewModel
class SupplierDetailViewModel @Inject constructor(
    private val getSupplierByIdUseCase: GetSupplierByIdUseCase,
    private val observeSupplierUseCase: ObserveSupplierUseCase,
    private val saveSupplierUseCase: SaveSupplierUseCase
) : ViewModel() {

    private val _supplier = MutableStateFlow<Supplier?>(null)
    val supplier: StateFlow<Supplier?> = _supplier

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    fun loadSupplier(supplierId: String?) {
        if (supplierId == null || supplierId.isBlank()) {
            _supplier.value = null
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                observeSupplierUseCase(supplierId).collect { supplier ->
                    _supplier.value = supplier
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                saveSupplierUseCase(supplier)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to save supplier: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
