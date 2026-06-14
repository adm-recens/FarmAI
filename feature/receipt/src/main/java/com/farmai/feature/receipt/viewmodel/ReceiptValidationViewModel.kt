package com.farmai.feature.receipt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.ParsedReceiptData
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ValidationSnapshot
import com.farmai.core.domain.model.ValidationStatus
import com.farmai.core.domain.usecase.receipt.GetDeductionsUseCase
import com.farmai.core.domain.usecase.receipt.GetLineItemsUseCase
import com.farmai.core.domain.usecase.receipt.GetReceiptByIdUseCase
import com.farmai.core.domain.usecase.receipt.GetValidationSnapshotsUseCase
import com.farmai.core.domain.usecase.receipt.ParseReceiptTextUseCase
import com.farmai.core.domain.usecase.receipt.ReceiptWithDetailsParams
import com.farmai.core.domain.usecase.receipt.SaveReceiptWithDetailsUseCase
import com.farmai.core.domain.usecase.receipt.SaveValidationSnapshotUseCase
import com.farmai.core.domain.usecase.receipt.UpdateReceiptValidationStatusUseCase
import com.farmai.core.domain.usecase.receipt.UpdateValidationStatusParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ReceiptValidationViewModel @Inject constructor(
    private val getReceiptByIdUseCase: GetReceiptByIdUseCase,
    private val getLineItemsUseCase: GetLineItemsUseCase,
    private val getDeductionsUseCase: GetDeductionsUseCase,
    private val getValidationSnapshotsUseCase: GetValidationSnapshotsUseCase,
    private val parseReceiptTextUseCase: ParseReceiptTextUseCase,
    private val saveReceiptWithDetailsUseCase: SaveReceiptWithDetailsUseCase,
    private val saveValidationSnapshotUseCase: SaveValidationSnapshotUseCase,
    private val updateReceiptValidationStatusUseCase: UpdateReceiptValidationStatusUseCase
) : ViewModel() {
    private val json = Json { ignoreUnknownKeys = true }

    private val _receipt = MutableStateFlow<Receipt?>(null)
    val receipt: StateFlow<Receipt?> = _receipt

    private val _lineItems = MutableStateFlow<List<ReceiptLineItem>>(emptyList())
    val lineItems: StateFlow<List<ReceiptLineItem>> = _lineItems

    private val _deductions = MutableStateFlow<List<Deduction>>(emptyList())
    val deductions: StateFlow<List<Deduction>> = _deductions

    private val _validationSnapshots = MutableStateFlow<List<ValidationSnapshot>>(emptyList())
    val validationSnapshots: StateFlow<List<ValidationSnapshot>> = _validationSnapshots

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private var originalParserJson = "{}"

    fun loadReceipt(receiptId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val receiptData = getReceiptByIdUseCase(receiptId)
                _receipt.value = receiptData
                if (receiptData != null) {
                    _lineItems.value = getLineItemsUseCase(receiptId)
                    _deductions.value = getDeductionsUseCase(receiptId)
                    _validationSnapshots.value = getValidationSnapshotsUseCase(receiptId)
                    originalParserJson = receiptData.ocrRawText?.let { json.encodeToString(parseReceiptTextUseCase(it)) } ?: "{}"
                } else {
                    _lineItems.value = emptyList()
                    _deductions.value = emptyList()
                    _validationSnapshots.value = emptyList()
                    originalParserJson = "{}"
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
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
                originalParserJson = json.encodeToString(parsed)
                _message.value = "Parsed OCR text. Confidence: ${(parsed.confidenceScore * 100).toInt()}%."
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to parse receipt: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveValidatedReceipt(
        receipt: Receipt,
        lineItems: List<ReceiptLineItem>,
        deductions: List<Deduction>,
        correctedJson: String,
        status: ValidationStatus
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                saveReceiptWithDetailsUseCase(ReceiptWithDetailsParams(receipt, lineItems, deductions))
                saveValidationSnapshotUseCase(
                    ValidationSnapshot(
                        id = ValidationSnapshot.generateId(),
                        receiptId = receipt.id,
                        originalJson = originalParserJson,
                        correctedJson = correctedJson,
                        source = "manual-validation"
                    )
                )
                updateReceiptValidationStatusUseCase(UpdateValidationStatusParams(receipt.id, status))
                _message.value = "Validation saved as ${status.name}."
                _error.value = null
                loadReceipt(receipt.id)
            } catch (e: Exception) {
                _error.value = "Failed to save validation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun correctedJson(
        receipt: Receipt,
        lineItems: List<ReceiptLineItem>,
        deductions: List<Deduction>
    ): String {
        val parsed = ParsedReceiptData(
            brokerName = receipt.brokerId,
            voucherNumber = receipt.voucherNumber,
            voucherDate = receipt.voucherDate,
            supplierCode = receipt.farmerId,
            lineItems = lineItems.map { item ->
                com.farmai.core.domain.model.ParsedLineItem(
                    quantity = item.quantity,
                    pricePerUnit = item.pricePerUnit,
                    amount = item.amount,
                    grade = item.grade,
                    confidence = 1.0
                )
            },
            commissionAmount = deductions.firstOrNull { it.type == com.farmai.core.domain.model.DeductionType.COMMISSION }?.amount,
            commissionPercent = deductions.firstOrNull { it.type == com.farmai.core.domain.model.DeductionType.COMMISSION }?.percentageValue,
            damagesAmount = deductions.firstOrNull { it.type == com.farmai.core.domain.model.DeductionType.DAMAGES }?.amount,
            unloadingAmount = deductions.firstOrNull { it.type == com.farmai.core.domain.model.DeductionType.UNLOADING }?.amount,
            advanceAmount = deductions.firstOrNull { it.type == com.farmai.core.domain.model.DeductionType.ADVANCE }?.amount,
            otherDeductions = deductions.filter { it.type == com.farmai.core.domain.model.DeductionType.OTHER }.map { deduction ->
                com.farmai.core.domain.model.ParsedDeduction(
                    type = deduction.type,
                    amount = deduction.amount,
                    description = deduction.description,
                    isPercentage = deduction.isPercentage,
                    percentageValue = deduction.percentageValue,
                    confidence = 1.0
                )
            },
            confidenceScore = 1.0,
            fieldConfidence = mapOf("corrected" to 1.0)
        )
        return json.encodeToString(parsed)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessage() {
        _message.value = null
    }
}
