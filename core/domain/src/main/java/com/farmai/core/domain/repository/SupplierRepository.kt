package com.farmai.core.domain.repository

import com.farmai.core.domain.model.Supplier
import com.farmai.core.domain.model.SupplierMatch
import com.farmai.core.domain.model.SupplierMergeSuggestion
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    suspend fun getAllSuppliers(): List<Supplier>
    suspend fun getSupplierById(id: String): Supplier?
    fun observeAllSuppliers(): Flow<List<Supplier>>
    fun observeSupplier(id: String): Flow<Supplier?>
    suspend fun saveSupplier(supplier: Supplier)
    suspend fun deleteSupplier(id: String)
    suspend fun searchSuppliers(query: String): List<Supplier>
    suspend fun suggestSupplierMatches(sourceText: String, limit: Int = 5): List<SupplierMatch>
    suspend fun suggestMergeCandidates(limit: Int = 10): List<SupplierMergeSuggestion>
}
