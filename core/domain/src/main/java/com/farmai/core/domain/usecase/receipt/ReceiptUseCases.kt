package com.farmai.core.domain.usecase.receipt

import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.ParsedReceiptData
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ReceiptStatus
import com.farmai.core.domain.repository.ReceiptParserRepository
import com.farmai.core.domain.repository.ReceiptRepository
import com.farmai.core.domain.usecase.FlowUseCase
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.UseCase
import javax.inject.Inject

class GetAllReceiptsUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<NoParams, List<Receipt>> {
    override suspend operator fun invoke(params: NoParams): List<Receipt> {
        return repository.getAllReceipts()
    }
}

class ObserveAllReceiptsUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : FlowUseCase<NoParams, List<Receipt>> {
    override operator fun invoke(params: NoParams) = repository.observeAllReceipts()
}

class GetReceiptByIdUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<String, Receipt?> {
    override suspend operator fun invoke(id: String): Receipt? {
        return repository.getReceiptById(id)
    }
}

class GetReceiptsByFarmerUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<String, List<Receipt>> {
    override suspend operator fun invoke(farmerId: String): List<Receipt> {
        return repository.getReceiptsByFarmer(farmerId)
    }
}

class ObserveReceiptsByFarmerUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : FlowUseCase<String, List<Receipt>> {
    override operator fun invoke(farmerId: String) = repository.observeReceiptsByFarmer(farmerId)
}

class GetReceiptsByDateRangeUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<DateRangeParams, List<Receipt>> {
    override suspend operator fun invoke(params: DateRangeParams): List<Receipt> {
        return repository.getReceiptsByDateRange(params.startDate, params.endDate)
    }
}

class ObserveReceiptsByDateRangeUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : FlowUseCase<DateRangeParams, List<Receipt>> {
    override operator fun invoke(params: DateRangeParams) = repository.observeReceiptsByDateRange(params.startDate, params.endDate)
}

class GetReceiptsByStatusUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<ReceiptStatus, List<Receipt>> {
    override suspend operator fun invoke(status: ReceiptStatus): List<Receipt> {
        return repository.getReceiptsByStatus(status)
    }
}

class SaveReceiptUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<Receipt, Unit> {
    override suspend operator fun invoke(receipt: Receipt): Unit {
        repository.saveReceipt(receipt)
    }
}

class SaveReceiptWithDetailsUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<ReceiptWithDetailsParams, Unit> {
    override suspend operator fun invoke(params: ReceiptWithDetailsParams): Unit {
        repository.saveReceiptWithDetails(params.receipt, params.lineItems, params.deductions)
    }
}

class UpdateReceiptStatusUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<UpdateStatusParams, Unit> {
    override suspend operator fun invoke(params: UpdateStatusParams): Unit {
        repository.updateReceiptStatus(params.id, params.status)
    }
}

class DeleteReceiptUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<String, Unit> {
    override suspend operator fun invoke(id: String): Unit {
        repository.deleteReceipt(id)
    }
}

class GetLineItemsUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<String, List<ReceiptLineItem>> {
    override suspend operator fun invoke(receiptId: String): List<ReceiptLineItem> {
        return repository.getLineItems(receiptId)
    }
}

class GetDeductionsUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<String, List<Deduction>> {
    override suspend operator fun invoke(receiptId: String): List<Deduction> {
        return repository.getDeductions(receiptId)
    }
}

class SearchReceiptsUseCase @Inject constructor(
    private val repository: ReceiptRepository
) : UseCase<String, List<Receipt>> {
    override suspend operator fun invoke(query: String): List<Receipt> {
        return repository.searchReceipts(query)
    }
}

class ParseReceiptImageUseCase @Inject constructor(
    private val repository: ReceiptParserRepository
) : UseCase<String, ParsedReceiptData> {
    override suspend operator fun invoke(imagePath: String): ParsedReceiptData {
        return repository.parseReceiptImage(imagePath)
    }
}

class ParseReceiptTextUseCase @Inject constructor(
    private val repository: ReceiptParserRepository
) : UseCase<String, ParsedReceiptData> {
    override suspend operator fun invoke(rawText: String): ParsedReceiptData {
        return repository.parseReceiptText(rawText)
    }
}

data class DateRangeParams(
    val startDate: Long,
    val endDate: Long
)

data class ReceiptWithDetailsParams(
    val receipt: Receipt,
    val lineItems: List<ReceiptLineItem>,
    val deductions: List<Deduction>
)

data class UpdateStatusParams(
    val id: String,
    val status: ReceiptStatus
)