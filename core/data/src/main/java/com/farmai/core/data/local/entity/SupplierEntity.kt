package com.farmai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.farmai.core.domain.model.Supplier
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(
    tableName = "suppliers",
    indices = [
        Index("name"),
        Index("farmerCode"),
        Index("updatedAt")
    ]
)
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(defaultValue = "[]") val aliasesJson: String,
    val farmerCode: String? = null,
    @ColumnInfo(defaultValue = "0.82") val confidenceThreshold: Float = 0.82f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Supplier = Supplier(
        id = id,
        name = name,
        aliases = aliases,
        farmerCode = farmerCode,
        confidenceThreshold = confidenceThreshold,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private val aliases: List<String>
        get() = runCatching {
            Json.decodeFromString(ListSerializer(String.serializer()), aliasesJson)
        }.getOrDefault(emptyList())

    companion object {
        fun fromDomain(supplier: Supplier): SupplierEntity = SupplierEntity(
            id = supplier.id,
            name = supplier.name,
            aliasesJson = Json.encodeToString(ListSerializer(String.serializer()), supplier.aliases),
            farmerCode = supplier.farmerCode,
            confidenceThreshold = supplier.confidenceThreshold,
            createdAt = supplier.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }
}
