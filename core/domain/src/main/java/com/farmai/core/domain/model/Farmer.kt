package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Farmer(
    val id: String,
    val name: String,
    val code: String,
    val phone: String? = null,
    val village: String? = null,
    val primaryCrop: String = "LEMON",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateId(code: String) = "FARMER_${code.uppercase()}"
    }
}