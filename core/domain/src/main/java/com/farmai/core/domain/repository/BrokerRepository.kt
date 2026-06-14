package com.farmai.core.domain.repository

import com.farmai.core.domain.model.Broker
import kotlinx.coroutines.flow.Flow

interface BrokerRepository {
    suspend fun getAllBrokers(): List<Broker>
    suspend fun getBrokerById(id: String): Broker?
    fun observeAllBrokers(): Flow<List<Broker>>
    fun observeBroker(id: String): Flow<Broker?>
    suspend fun saveBroker(broker: Broker)
    suspend fun deleteBroker(id: String)
    suspend fun searchBrokers(query: String): List<Broker>
}