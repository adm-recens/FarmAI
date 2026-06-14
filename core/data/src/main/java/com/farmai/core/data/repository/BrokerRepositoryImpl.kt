package com.farmai.core.data.repository

import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.local.entity.BrokerEntity
import com.farmai.core.domain.model.Broker
import com.farmai.core.domain.repository.BrokerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrokerRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : BrokerRepository {
    override suspend fun getAllBrokers(): List<Broker> {
        return database.brokerDao().getAllBrokers().first().map { it.toDomain() }
    }

    override suspend fun getBrokerById(id: String): Broker? {
        return database.brokerDao().getBrokerById(id).first()?.toDomain()
    }

    override fun observeAllBrokers(): Flow<List<Broker>> {
        return database.brokerDao().getAllBrokers().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeBroker(id: String): Flow<Broker?> {
        return database.brokerDao().getBrokerById(id).map { it?.toDomain() }
    }

    override suspend fun saveBroker(broker: Broker) {
        database.brokerDao().insertBroker(BrokerEntity.fromDomain(broker.copy(updatedAt = System.currentTimeMillis())))
    }

    override suspend fun deleteBroker(id: String) {
        database.brokerDao().deleteBrokerById(id)
    }

    override suspend fun searchBrokers(query: String): List<Broker> {
        val searchQuery = "%$query%"
        return database.brokerDao().searchBrokers(searchQuery).map { it.toDomain() }
    }
}
