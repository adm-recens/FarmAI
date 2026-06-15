package com.farmai.core.data.repository

import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.local.entity.SupplierEntity
import com.farmai.core.domain.model.Supplier
import com.farmai.core.domain.model.SupplierMatch
import com.farmai.core.domain.model.SupplierMergeSuggestion
import com.farmai.core.domain.repository.SupplierRepository
import com.farmai.core.domain.usecase.supplier.SupplierMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : SupplierRepository {
    override suspend fun getAllSuppliers(): List<Supplier> {
        return database.supplierDao().getAllSuppliers().first().map { it.toDomain() }
    }

    override suspend fun getSupplierById(id: String): Supplier? {
        return database.supplierDao().getSupplierById(id).first()?.toDomain()
    }

    override fun observeAllSuppliers(): Flow<List<Supplier>> {
        return database.supplierDao().getAllSuppliers().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeSupplier(id: String): Flow<Supplier?> {
        return database.supplierDao().getSupplierById(id).map { it?.toDomain() }
    }

    override suspend fun saveSupplier(supplier: Supplier) {
        database.supplierDao().insertSupplier(SupplierEntity.fromDomain(supplier.copy(updatedAt = System.currentTimeMillis())))
    }

    override suspend fun deleteSupplier(id: String) {
        database.supplierDao().deleteSupplierById(id)
    }

    override suspend fun searchSuppliers(query: String): List<Supplier> {
        val normalizedQuery = SupplierMatcher.normalize(query)
        return if (normalizedQuery.isBlank()) {
            getAllSuppliers()
        } else {
            val searchQuery = "%$query%"
            val daoResults = database.supplierDao().searchSuppliers(searchQuery).map { it.toDomain() }
            val aliasResults = getAllSuppliers()
                .filter { supplier ->
                    supplier.aliases.any { SupplierMatcher.normalize(it).contains(normalizedQuery) }
                }
            (daoResults + aliasResults)
                .distinctBy { it.id }
                .sortedWith(compareBy<Supplier> { it.name.contains(query, ignoreCase = true) }.thenBy { it.name })
        }
    }

    override suspend fun suggestSupplierMatches(sourceText: String, limit: Int): List<SupplierMatch> {
        return SupplierMatcher.suggestMatches(sourceText, getAllSuppliers(), limit)
    }

    override suspend fun suggestMergeCandidates(limit: Int): List<SupplierMergeSuggestion> {
        return SupplierMatcher.suggestMergeCandidates(getAllSuppliers(), limit)
    }
}
