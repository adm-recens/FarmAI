package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Supplier(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val farmerCode: String? = null,
    val confidenceThreshold: Float = 0.82f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateId(name: String): String {
            val normalized = name
                .trim()
                .uppercase()
                .replace(Regex("[^A-Z0-9]+"), "_")
                .trim('_')
            return if (normalized.isNotBlank()) {
                "SUPPLIER_${normalized}_${normalized.hashCode().toString(16).uppercase()}"
            } else {
                "SUPPLIER_${java.util.UUID.randomUUID()}"
            }
        }
    }
}

@Serializable
data class SupplierMatch(
    val supplier: Supplier,
    val matchedText: String,
    val confidence: Float,
    val isAlias: Boolean,
    val sourceText: String
)

@Serializable
data class SupplierMergeSuggestion(
    val leftSupplier: Supplier,
    val rightSupplier: Supplier,
    val confidence: Float,
    val reason: String
)
