package com.farmai.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CropBoxTest {
    @Test
    fun autoDetectReturnsCenteredReceiptBox() {
        val cropBox = CropBoxJson.autoDetect(1000, 1500)

        assertEquals(70f, cropBox.x, 0.01f)
        assertEquals(105f, cropBox.y, 0.01f)
        assertEquals(860f, cropBox.width, 0.01f)
        assertEquals(1290f, cropBox.height, 0.01f)
        assertFalse(cropBox.manualOverride)
        assertTrue(cropBox.confidence > 0.7f)
    }

    @Test
    fun cropBoxCanBeEncodedAndDecoded() {
        val cropBox = CropBox(10f, 20f, 300f, 400f, confidence = 0.8f, manualOverride = true)

        val encoded = CropBoxJson.encode(cropBox)
        val decoded = CropBoxJson.decode(encoded)

        assertNotNull(decoded)
        assertEquals(cropBox, decoded)
    }

    @Test
    fun profileBasedCropUsesDetectedMargins() {
        val profile = SmartCropProfile(
            leftMarginRatio = 0.1f,
            topMarginRatio = 0.08f,
            rightMarginRatio = 0.12f,
            bottomMarginRatio = 0.1f,
            confidence = 0.82f
        )
        val cropBox = CropBoxJson.fromProfile(1000, 1500, profile)

        assertEquals(100f, cropBox.x, 0.01f)
        assertEquals(120f, cropBox.y, 0.01f)
        assertEquals(780f, cropBox.width, 0.01f)
        assertEquals(1230f, cropBox.height, 0.01f)
        assertEquals(0.82f, cropBox.confidence, 0.01f)
        assertFalse(cropBox.manualOverride)
    }

    @Test
    fun clampedCropBoxStaysInsideImageBounds() {
        val cropBox = CropBox(900f, 1200f, 300f, 400f, confidence = 0.8f, manualOverride = true)

        val clamped = cropBox.clamped(1000, 1500)

        assertTrue(clamped.x >= 0f)
        assertTrue(clamped.y >= 0f)
        assertTrue(clamped.x + clamped.width <= 1000f)
        assertTrue(clamped.y + clamped.height <= 1500f)
    }
}
