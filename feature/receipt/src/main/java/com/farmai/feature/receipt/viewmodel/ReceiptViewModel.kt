package com.farmai.feature.receipt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.mapper.ParsedReceiptMapper
import com.farmai.core.domain.model.Broker
import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.DeductionType
import com.farmai.core.domain.model.Farmer
import com.farmai.core.domain.model.ParsedReceiptData
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ReceiptStatus
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.receipt.*
import com.farmai.feature.receipt.ui.ExportShare
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ReceiptListViewModel @Inject constructor(
    private val observeAllReceiptsUseCase: ObserveAllReceiptsUseCase,
    private val searchReceiptsUseCase: SearchReceiptsUseCase,
    private val deleteReceiptUseCase: DeleteReceiptUseCase,
    private val updateReceiptStatusUseCase: UpdateReceiptStatusUseCase
) : ViewModel() {

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    val receipts: StateFlow<List<Receipt>> = _receipts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeReceipts()
    }

    private fun observeReceipts() {
        viewModelScope.launch {
            observeAllReceiptsUseCase(NoParams.INSTANCE).collect { receiptList ->
                _receipts.value = receiptList
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        if (query.isBlank()) {
            observeReceipts()
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val results = searchReceiptsUseCase(query)
                    _receipts.value = results
                } catch (e: Exception) {
                    _error.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            try {
                deleteReceiptUseCase(receipt.id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete receipt: ${e.message}"
            }
        }
    }

    fun updateReceiptStatus(receiptId: String, status: ReceiptStatus) {
        viewModelScope.launch {
            try {
                updateReceiptStatusUseCase(UpdateStatusParams(receiptId, status))
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update receipt status: ${e.message}"
            }
        }
    }

    fun refresh() {
        observeReceipts()
    }
}

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val getReceiptByIdUseCase: GetReceiptByIdUseCase,
    private val getLineItemsUseCase: GetLineItemsUseCase,
    private val getDeductionsUseCase: GetDeductionsUseCase,
    private val updateReceiptStatusUseCase: UpdateReceiptStatusUseCase,
    private val deleteReceiptUseCase: DeleteReceiptUseCase
) : ViewModel() {

    private val _receipt = MutableStateFlow<Receipt?>(null)
    val receipt: StateFlow<Receipt?> = _receipt

    private val _lineItems = MutableStateFlow<List<ReceiptLineItem>>(emptyList())
    val lineItems: StateFlow<List<ReceiptLineItem>> = _lineItems

    private val _deductions = MutableStateFlow<List<Deduction>>(emptyList())
    val deductions: StateFlow<List<Deduction>> = _deductions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadReceipt(receiptId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val receiptData = getReceiptByIdUseCase(receiptId)
                _receipt.value = receiptData
                if (receiptData != null) {
                    _lineItems.value = getLineItemsUseCase(receiptId)
                    _deductions.value = getDeductionsUseCase(receiptId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReceiptStatus(receiptId: String, status: ReceiptStatus) {
        viewModelScope.launch {
            try {
                updateReceiptStatusUseCase(UpdateStatusParams(receiptId, status))
                loadReceipt(receiptId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update receipt status: ${e.message}"
            }
        }
    }

    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            try {
                deleteReceiptUseCase(receiptId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete receipt: ${e.message}"
            }
        }
    }

    fun shareReceiptCsv(
        context: Context,
        receipt: Receipt,
        lineItems: List<ReceiptLineItem>,
        deductions: List<Deduction>
    ) {
        try {
            ExportShare.shareReceiptCsv(context, receipt, lineItems, deductions)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to export receipt CSV: ${e.message}"
        }
    }

    fun shareReceiptPdf(
        context: Context,
        receipt: Receipt,
        lineItems: List<ReceiptLineItem>,
        deductions: List<Deduction>
    ) {
        try {
            ExportShare.shareReceiptPdf(context, receipt, lineItems, deductions)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to export receipt PDF: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun buildParseMessage(parsed: ParsedReceiptData): String {
        val parts = mutableListOf<String>()
        if (parsed.voucherNumber != null) parts += "voucher"
        if (parsed.voucherDate != null) parts += "date"
        if (parsed.supplierCode != null) parts += "farmer code"
        if (parsed.brokerName != null) parts += "broker"
        if (parsed.lineItems.isNotEmpty()) parts += "${parsed.lineItems.size} line item(s)"
        if (parsed.commissionPercent != null || parsed.damagesAmount != null || parsed.unloadingAmount != null || parsed.advanceAmount != null || parsed.otherDeductions.isNotEmpty()) parts += "deductions"
        return if (parts.isEmpty()) {
            "No receipt fields were parsed. Review the OCR text and try again."
        } else {
            "Parsed ${parts.joinToString(", ")}. Confidence: ${(parsed.confidenceScore * 100).toInt()}%."
        }
    }
}

@HiltViewModel
class ReceiptEntryViewModel @Inject constructor(
    private val saveReceiptWithDetailsUseCase: SaveReceiptWithDetailsUseCase,
    private val parseReceiptTextUseCase: ParseReceiptTextUseCase,
    private val getReceiptByIdUseCase: GetReceiptByIdUseCase,
    private val getLineItemsUseCase: GetLineItemsUseCase,
    private val getDeductionsUseCase: GetDeductionsUseCase,
    private val getAllFarmersUseCase: com.farmai.core.domain.usecase.farmer.GetAllFarmersUseCase,
    private val getAllBrokersUseCase: com.farmai.core.domain.usecase.broker.GetAllBrokersUseCase
) : ViewModel() {

    private val _receipt = MutableStateFlow<Receipt?>(null)
    val receipt: StateFlow<Receipt?> = _receipt

    private val _lineItems = MutableStateFlow<List<ReceiptLineItem>>(emptyList())
    val lineItems: StateFlow<List<ReceiptLineItem>> = _lineItems

    private val _deductions = MutableStateFlow<List<Deduction>>(emptyList())
    val deductions: StateFlow<List<Deduction>> = _deductions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _parsedReceiptData = MutableStateFlow<ParsedReceiptData?>(null)
    val parsedReceiptData: StateFlow<ParsedReceiptData?> = _parsedReceiptData

    private val _parseMessage = MutableStateFlow<String?>(null)
    val parseMessage: StateFlow<String?> = _parseMessage

    private val _farmers = MutableStateFlow<List<Farmer>>(emptyList())
    val farmers: StateFlow<List<Farmer>> = _farmers

    private val _brokers = MutableStateFlow<List<Broker>>(emptyList())
    val brokers: StateFlow<List<Broker>> = _brokers

    fun loadReceipt(receiptId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val receiptData = getReceiptByIdUseCase(receiptId)
                _receipt.value = receiptData
                _parsedReceiptData.value = null
                _parseMessage.value = null
                if (receiptData != null) {
                    _lineItems.value = getLineItemsUseCase(receiptId)
                    _deductions.value = getDeductionsUseCase(receiptId)
                } else {
                    _lineItems.value = emptyList()
                    _deductions.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveReceipt(receipt: Receipt, lineItems: List<ReceiptLineItem>, deductions: List<Deduction>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                saveReceiptWithDetailsUseCase(ReceiptWithDetailsParams(receipt, lineItems, deductions))
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to save receipt: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun parseReceiptText(rawText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val parsed = parseReceiptTextUseCase(rawText)
                _parsedReceiptData.value = parsed
                _parseMessage.value = buildParseMessage(parsed)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to parse receipt: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyParsedReceiptData(parsed: ParsedReceiptData) {
        val receiptId = _receipt.value?.id ?: ""
        val lineItems = ParsedReceiptMapper.toLineItems(parsed, receiptId)
        val deductions = ParsedReceiptMapper.toDeductions(parsed, receiptId)
        if (lineItems.isNotEmpty()) {
            _lineItems.value = lineItems
        }
        if (deductions.isNotEmpty()) {
            _deductions.value = deductions
        }
        _parseMessage.value = "Parsed data applied. Review fields before saving."
    }

    fun addLineItem() {
        val current = _lineItems.value
        _lineItems.value = current + ReceiptLineItem(
            id = ReceiptLineItem.generateId(),
            receiptId = _receipt.value?.id ?: "",
            quantity = 0.0,
            pricePerUnit = 0.0,
            amount = 0.0,
            sortOrder = current.size
        )
    }

    fun removeLineItem(index: Int) {
        val current = _lineItems.value.toMutableList()
        if (index >= 0 && index < current.size) {
            current.removeAt(index)
            _lineItems.value = current
        }
    }

    fun addDeduction(type: DeductionType, amount: Double) {
        val current = _deductions.value
        _deductions.value = current + Deduction(
            id = Deduction.generateId(),
            receiptId = _receipt.value?.id ?: "",
            type = type,
            amount = amount
        )
    }

    fun removeDeduction(index: Int) {
        val current = _deductions.value.toMutableList()
        if (index >= 0 && index < current.size) {
            current.removeAt(index)
            _deductions.value = current
        }
    }

    fun updateLineItem(index: Int, quantity: Double, pricePerUnit: Double) {
        val current = _lineItems.value.toMutableList()
        if (index >= 0 && index < current.size) {
            current[index] = current[index].copy(
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                amount = quantity * pricePerUnit
            )
            _lineItems.value = current
        }
    }

    fun updateDeduction(index: Int, amount: Double) {
        val current = _deductions.value.toMutableList()
        if (index >= 0 && index < current.size) {
            current[index] = current[index].copy(amount = amount)
            _deductions.value = current
        }
    }

    fun loadFarmersAndBrokers() {
        viewModelScope.launch {
            try { _farmers.value = getAllFarmersUseCase(NoParams.INSTANCE) } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { _brokers.value = getAllBrokersUseCase(NoParams.INSTANCE) } catch (_: Exception) {}
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun buildParseMessage(parsed: ParsedReceiptData): String {
        val parts = mutableListOf<String>()
        if (parsed.voucherNumber != null) parts += "voucher"
        if (parsed.voucherDate != null) parts += "date"
        if (parsed.supplierCode != null) parts += "farmer code"
        if (parsed.brokerName != null) parts += "broker"
        if (parsed.lineItems.isNotEmpty()) parts += "${parsed.lineItems.size} line item(s)"
        if (parsed.commissionPercent != null || parsed.damagesAmount != null || parsed.unloadingAmount != null || parsed.advanceAmount != null || parsed.otherDeductions.isNotEmpty()) parts += "deductions"
        return if (parts.isEmpty()) {
            "No receipt fields were parsed. Review the OCR text and try again."
        } else {
            "Parsed ${parts.joinToString(", ")}. Confidence: ${(parsed.confidenceScore * 100).toInt()}%."
        }
    }
}
