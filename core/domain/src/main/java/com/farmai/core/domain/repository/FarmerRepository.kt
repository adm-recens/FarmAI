package com.farmai.core.domain.repository

import com.farmai.core.domain.model.Farmer
import kotlinx.coroutines.flow.Flow

interface FarmerRepository {
    suspend fun getAllFarmers(): List<Farmer>
    suspend fun getFarmerById(id: String): Farmer?
    suspend fun getFarmerByCode(code: String): Farmer?
    fun observeAllFarmers(): Flow<List<Farmer>>
    fun observeFarmer(id: String): Flow<Farmer?>
    suspend fun saveFarmer(farmer: Farmer)
    suspend fun deleteFarmer(id: String)
    suspend fun searchFarmers(query: String): List<Farmer>
}