package com.farmai.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Broker(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val defaultCommissionPercent: Double = 4.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateId(name: String) = "BROKER_${name.uppercase().replace(" ", "_").replace("&", "").replace(".", "")}"
    }
}