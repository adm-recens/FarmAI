package com.farmai.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.farmai.core.domain.model.Broker

@Entity(tableName = "brokers")
data class BrokerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val defaultCommissionPercent: Double = 4.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Broker = Broker(
        id = id,
        name = name,
        address = address,
        phone = phone,
        defaultCommissionPercent = defaultCommissionPercent,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(broker: Broker): BrokerEntity = BrokerEntity(
            id = broker.id,
            name = broker.name,
            address = broker.address,
            phone = broker.phone,
            defaultCommissionPercent = broker.defaultCommissionPercent,
            createdAt = broker.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }
}