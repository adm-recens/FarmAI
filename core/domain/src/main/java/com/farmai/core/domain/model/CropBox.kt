package com.farmai.core.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CropBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rotation: Float = 0f,
    val confidence: Float,
    val manualOverride: Boolean
) {
    fun clamped(imageWidth: Int, imageHeight: Int): CropBox {
        val minSize = maxOf(1f, minOf(imageWidth, imageHeight) * MIN_RELATIVE_SIZE)
        val maxX = maxOf(0f, imageWidth.toFloat() - minSize)
        val maxY = maxOf(0f, imageHeight.toFloat() - minSize)
        val safeX = x.coerceIn(0f, maxX)
        val safeY = y.coerceIn(0f, maxY)
        return copy(
            x = safeX,
            y = safeY,
            width = width.coerceIn(minSize, imageWidth.toFloat() - safeX),
            height = height.coerceIn(minSize, imageHeight.toFloat() - safeY)
        )
    }

    companion object {
        const val MIN_RELATIVE_SIZE = 0.08f
    }
}

@Serializable
data class SmartCropProfile(
    val leftMarginRatio: Float,
    val topMarginRatio: Float,
    val rightMarginRatio: Float,
    val bottomMarginRatio: Float,
    val confidence: Float
) {
    fun normalized(): SmartCropProfile {
        return copy(
            leftMarginRatio = leftMarginRatio.coerceIn(0f, 0.45f),
            topMarginRatio = topMarginRatio.coerceIn(0f, 0.45f),
            rightMarginRatio = rightMarginRatio.coerceIn(0f, 0.45f),
            bottomMarginRatio = bottomMarginRatio.coerceIn(0f, 0.45f),
            confidence = confidence.coerceIn(0f, 1f)
        )
    }
}

object CropBoxJson {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(cropBox: CropBox): String = json.encodeToString(cropBox)

    fun decode(jsonText: String?): CropBox? {
        if (jsonText.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<CropBox>(jsonText) }.getOrNull()
    }

    fun autoDetect(imageWidth: Int, imageHeight: Int, manualOverride: Boolean = false): CropBox {
        require(imageWidth > 0 && imageHeight > 0) { "Image dimensions must be positive." }
        val marginX = imageWidth * 0.07f
        val marginY = imageHeight * 0.07f
        return CropBox(
            x = marginX,
            y = marginY,
            width = imageWidth.toFloat() - marginX * 2f,
            height = imageHeight.toFloat() - marginY * 2f,
            rotation = 0f,
            confidence = 0.72f,
            manualOverride = manualOverride
        ).clamped(imageWidth, imageHeight)
    }

    fun fromProfile(
        imageWidth: Int,
        imageHeight: Int,
        profile: SmartCropProfile,
        manualOverride: Boolean = false
    ): CropBox {
        require(imageWidth > 0 && imageHeight > 0) { "Image dimensions must be positive." }
        val normalized = profile.normalized()
        val x = imageWidth.toFloat() * normalized.leftMarginRatio
        val y = imageHeight.toFloat() * normalized.topMarginRatio
        val width = imageWidth.toFloat() * (1f - normalized.leftMarginRatio - normalized.rightMarginRatio)
        val height = imageHeight.toFloat() * (1f - normalized.topMarginRatio - normalized.bottomMarginRatio)
        return CropBox(
            x = x,
            y = y,
            width = width,
            height = height,
            rotation = 0f,
            confidence = normalized.confidence,
            manualOverride = manualOverride
        ).clamped(imageWidth, imageHeight)
    }

    fun fullImage(imageWidth: Int, imageHeight: Int, manualOverride: Boolean = true): CropBox {
        require(imageWidth > 0 && imageHeight > 0) { "Image dimensions must be positive." }
        return CropBox(
            x = 0f,
            y = 0f,
            width = imageWidth.toFloat(),
            height = imageHeight.toFloat(),
            rotation = 0f,
            confidence = 0.50f,
            manualOverride = manualOverride
        )
    }
}

fun ReceiptJob.cropBox(): CropBox? = CropBoxJson.decode(cropBoxJson)
