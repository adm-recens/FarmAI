package com.farmai.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.farmai.core.domain.model.Farmer

@Entity(tableName = "farmers")
data class FarmerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val phone: String? = null,
    val village: String? = null,
    val primaryCrop: String = "LEMON",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Farmer = Farmer(
        id = id,
        name = name,
        code = code,
        phone = phone,
        village = village,
        primaryCrop = primaryCrop,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(farmer: Farmer): FarmerEntity = FarmerEntity(
            id = farmer.id,
            name = farmer.name,
            code = farmer.code,
            phone = farmer.phone,
            village = farmer.village,
            primaryCrop = farmer.primaryCrop,
            createdAt = farmer.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }
}