package com.farmai.core.data.repository

import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.local.entity.FarmerEntity
import com.farmai.core.domain.model.Farmer
import com.farmai.core.domain.repository.FarmerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmerRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : FarmerRepository {
    override suspend fun getAllFarmers(): List<Farmer> {
        return database.farmerDao().getAllFarmers().first().map { it.toDomain() }
    }

    override suspend fun getFarmerById(id: String): Farmer? {
        return database.farmerDao().getFarmerById(id).first()?.toDomain()
    }

    override suspend fun getFarmerByCode(code: String): Farmer? {
        return database.farmerDao().getFarmerByCode(code)?.toDomain()
    }

    override fun observeAllFarmers(): Flow<List<Farmer>> {
        return database.farmerDao().getAllFarmers().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeFarmer(id: String): Flow<Farmer?> {
        return database.farmerDao().getFarmerById(id).map { it?.toDomain() }
    }

    override suspend fun saveFarmer(farmer: Farmer) {
        database.farmerDao().insertFarmer(FarmerEntity.fromDomain(farmer.copy(updatedAt = System.currentTimeMillis())))
    }

    override suspend fun deleteFarmer(id: String) {
        database.farmerDao().deleteFarmerById(id)
    }

    override suspend fun searchFarmers(query: String): List<Farmer> {
        val searchQuery = "%$query%"
        return database.farmerDao().searchFarmers(searchQuery).map { it.toDomain() }
    }
}
