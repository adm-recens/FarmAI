package com.farmai.core.domain.usecase.supplier

import com.farmai.core.domain.model.Supplier
import com.farmai.core.domain.model.SupplierMatch
import com.farmai.core.domain.model.SupplierMergeSuggestion
import com.farmai.core.domain.repository.SupplierRepository
import com.farmai.core.domain.usecase.FlowUseCase
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.UseCase
import javax.inject.Inject

class GetAllSuppliersUseCase @Inject constructor(
    private val repository: SupplierRepository
) : UseCase<NoParams, List<Supplier>> {
    override suspend operator fun invoke(params: NoParams): List<Supplier> {
        return repository.getAllSuppliers()
    }
}

class ObserveAllSuppliersUseCase @Inject constructor(
    private val repository: SupplierRepository
) : FlowUseCase<NoParams, List<Supplier>> {
    override operator fun invoke(params: NoParams) = repository.observeAllSuppliers()
}

class GetSupplierByIdUseCase @Inject constructor(
    private val repository: SupplierRepository
) : UseCase<String, Supplier?> {
    override suspend operator fun invoke(id: String): Supplier? {
        return repository.getSupplierById(id)
    }
}

class ObserveSupplierUseCase @Inject constructor(
    private val repository: SupplierRepository
) : FlowUseCase<String, Supplier?> {
    override operator fun invoke(id: String) = repository.observeSupplier(id)
}

class SaveSupplierUseCase @Inject constructor(
    private val repository: SupplierRepository
) : UseCase<Supplier, Unit> {
    override suspend operator fun invoke(supplier: Supplier): Unit {
        repository.saveSupplier(supplier)
    }
}

class DeleteSupplierUseCase @Inject constructor(
    private val repository: SupplierRepository
) : UseCase<String, Unit> {
    override suspend operator fun invoke(id: String): Unit {
        repository.deleteSupplier(id)
    }
}

class SearchSuppliersUseCase @Inject constructor(
    private val repository: SupplierRepository
) : UseCase<String, List<Supplier>> {
    override suspend operator fun invoke(query: String): List<Supplier> {
        return repository.searchSuppliers(query)
    }
}

class SuggestSupplierMatchesUseCase @Inject constructor(
    private val repository: SupplierRepository
) : UseCase<String, List<SupplierMatch>> {
    override suspend operator fun invoke(sourceText: String): List<SupplierMatch> {
        return repository.suggestSupplierMatches(sourceText)
    }
}

class SuggestMergeCandidatesUseCase @Inject constructor(
    private val repository: SupplierRepository
) : UseCase<NoParams, List<SupplierMergeSuggestion>> {
    override suspend operator fun invoke(params: NoParams): List<SupplierMergeSuggestion> {
        return repository.suggestMergeCandidates()
    }
}
