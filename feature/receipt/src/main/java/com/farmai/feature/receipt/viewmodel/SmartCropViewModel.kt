package com.farmai.feature.receipt.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmai.core.domain.model.CropBox
import com.farmai.core.domain.model.CropBoxJson
import com.farmai.core.domain.model.ReceiptJob
import com.farmai.core.domain.model.SmartCropProfile
import com.farmai.core.domain.model.cropBox
import com.farmai.core.domain.usecase.crop.GenerateAutoCropBoxParams
import com.farmai.core.domain.usecase.crop.GenerateAutoCropBoxUseCase
import com.farmai.core.domain.usecase.crop.ObserveJobByIdUseCase
import com.farmai.core.domain.usecase.crop.UpdateJobCropBoxParams
import com.farmai.core.domain.usecase.crop.UpdateJobCropBoxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.io.File
import java.io.FileOutputStream

@HiltViewModel
class SmartCropViewModel @Inject constructor(
    private val observeJobByIdUseCase: ObserveJobByIdUseCase,
    private val updateJobCropBoxUseCase: UpdateJobCropBoxUseCase,
    private val generateAutoCropBoxUseCase: GenerateAutoCropBoxUseCase
) : ViewModel() {
    private val _job = MutableStateFlow<ReceiptJob?>(null)
    val job: StateFlow<ReceiptJob?> = _job

    private val _cropBox = MutableStateFlow<CropBox?>(null)
    val cropBox: StateFlow<CropBox?> = _cropBox

    private val _imageWidth = MutableStateFlow(0)
    val imageWidth: StateFlow<Int> = _imageWidth

    private val _imageHeight = MutableStateFlow(0)
    val imageHeight: StateFlow<Int> = _imageHeight

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadJob(jobId: String) {
        viewModelScope.launch {
            observeJobByIdUseCase(jobId).collect { jobData ->
                _job.value = jobData
                val path = jobData?.imagePath
                if (path != null) {
                    val dimensions = readImageDimensions(path)
                    if (dimensions != null) {
                        _imageWidth.value = dimensions.first
                        _imageHeight.value = dimensions.second
                        _cropBox.value = jobData.cropBox()
                            ?: CropBoxJson.autoDetect(dimensions.first, dimensions.second)
                    } else {
                        _error.value = "Unable to read receipt image dimensions."
                    }
                }
            }
        }
    }

    fun setCropBox(cropBox: CropBox) {
        _cropBox.value = cropBox
        _error.value = null
    }

    fun runAutoCrop() {
        val jobData = _job.value ?: return
        val path = jobData.imagePath ?: run {
            _error.value = "Receipt job does not have an image."
            return
        }
        val dimensions = readImageDimensions(path) ?: run {
            _error.value = "Unable to read receipt image dimensions."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _imageWidth.value = dimensions.first
                _imageHeight.value = dimensions.second
                val profile = generateSmartCropProfile(path, dimensions.first, dimensions.second)
                _cropBox.value = generateAutoCropBoxUseCase(
                    GenerateAutoCropBoxParams(
                        imageWidth = dimensions.first,
                        imageHeight = dimensions.second,
                        profile = profile
                    )
                )
                _message.value = if (profile != null) {
                    "Auto crop box generated from receipt edges."
                } else {
                    "Auto crop box generated from image dimensions."
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun skipCrop() {
        val jobData = _job.value ?: return
        val path = jobData.imagePath ?: run {
            _error.value = "Receipt job does not have an image."
            return
        }
        val dimensions = readImageDimensions(path) ?: run {
            _error.value = "Unable to read receipt image dimensions."
            return
        }
        _imageWidth.value = dimensions.first
        _imageHeight.value = dimensions.second
        _cropBox.value = CropBoxJson.fullImage(dimensions.first, dimensions.second)
        saveCropBox(manualOverride = true)
    }

    fun saveCropBox(manualOverride: Boolean) {
        val jobData = _job.value ?: return
        val currentCropBox = _cropBox.value ?: run {
            _error.value = "No crop box available."
            return
        }
        val imagePath = jobData.imagePath ?: run {
            _error.value = "No image to crop."
            return
        }
        val encodedCropBox = CropBoxJson.encode(
            currentCropBox.copy(
                manualOverride = manualOverride,
                confidence = if (manualOverride) 0.90f else currentCropBox.confidence
            )
        )
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val croppedPath = withContext(Dispatchers.Default) {
                    generateCroppedImageFile(imagePath, currentCropBox)
                }
                updateJobCropBoxUseCase(
                    UpdateJobCropBoxParams(
                        jobId = jobData.id,
                        cropBoxJson = encodedCropBox,
                        confidenceScore = if (manualOverride) 0.90 else currentCropBox.confidence.toDouble(),
                        croppedImagePath = croppedPath
                    )
                )
                _message.value = "Crop box saved."
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessage() {
        _message.value = null
    }

    private suspend fun generateSmartCropProfile(imagePath: String, imageWidth: Int, imageHeight: Int): SmartCropProfile? {
        return withContext(Dispatchers.Default) {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = calculateInSampleSize(imageWidth, imageHeight, MAX_AUTO_CROP_SAMPLE_WIDTH)
            }
            val bitmap: Bitmap? = BitmapFactory.decodeFile(imagePath, options)
            if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
                bitmap?.recycle()
                return@withContext null
            }

            val scaledWidth = bitmap.width
            val scaledHeight = bitmap.height
            val rowHasContent = BooleanArray(bitmap.height)
            val columnHasContent = BooleanArray(bitmap.width)
            var contentPixels = 0
            var totalPixels = 0

            for (y in 0 until bitmap.height step 2) {
                for (x in 0 until bitmap.width step 2) {
                    val pixel = bitmap.getPixel(x, y)
                    val luminance = Color.red(pixel) * 0.299f +
                        Color.green(pixel) * 0.587f +
                        Color.blue(pixel) * 0.114f
                    if (luminance < DARK_PIXEL_THRESHOLD) {
                        rowHasContent[y] = true
                        columnHasContent[x] = true
                        contentPixels++
                    }
                    totalPixels++
                }
            }

            val top = rowHasContent.indexOfFirst { it }
            val bottom = rowHasContent.indexOfLast { it }
            val left = columnHasContent.indexOfFirst { it }
            val right = columnHasContent.indexOfLast { it }

            bitmap.recycle()

            if (top < 0 || bottom < 0 || left < 0 || right < 0) {
                return@withContext null
            }

            val contentRatio = if (totalPixels > 0) contentPixels.toFloat() / totalPixels.toFloat() else 0f
            val horizontalCoverage = (right - left).coerceAtLeast(0).toFloat() / scaledWidth.coerceAtLeast(1)
            val verticalCoverage = (bottom - top).coerceAtLeast(0).toFloat() / scaledHeight.coerceAtLeast(1)
            val confidence = (0.62f + contentRatio * 1.8f + horizontalCoverage * 0.12f + verticalCoverage * 0.12f)
                .coerceIn(0.55f, 0.92f)
            val marginRatio = 0.025f

            SmartCropProfile(
                leftMarginRatio = (left.toFloat() / scaledWidth.toFloat() - marginRatio).coerceIn(0f, 0.45f),
                topMarginRatio = (top.toFloat() / scaledHeight.toFloat() - marginRatio).coerceIn(0f, 0.45f),
                rightMarginRatio = (1f - right.toFloat() / scaledWidth.toFloat() - marginRatio).coerceIn(0f, 0.45f),
                bottomMarginRatio = (1f - bottom.toFloat() / scaledHeight.toFloat() - marginRatio).coerceIn(0f, 0.45f),
                confidence = confidence
            )
        }
    }

    private fun calculateInSampleSize(imageWidth: Int, imageHeight: Int, maxPixels: Int): Int {
        val longerSide = maxOf(imageWidth, imageHeight)
        var sampleSize = 1
        while (longerSide / sampleSize > maxPixels && sampleSize < 8) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun readImageDimensions(imagePath: String): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(imagePath, options)
        return if (options.outWidth > 0 && options.outHeight > 0) {
            options.outWidth to options.outHeight
        } else {
            null
        }
    }

    private fun generateCroppedImageFile(imagePath: String, cropBox: CropBox): String {
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: throw IllegalStateException("Cannot decode image")
        val x = cropBox.x.toInt().coerceIn(0, bitmap.width - 1)
        val y = cropBox.y.toInt().coerceIn(0, bitmap.height - 1)
        val w = cropBox.width.toInt().coerceAtMost(bitmap.width - x).coerceAtLeast(1)
        val h = cropBox.height.toInt().coerceAtMost(bitmap.height - y).coerceAtLeast(1)
        val cropped = Bitmap.createBitmap(bitmap, x, y, w, h)
        val directory = File(imagePath).parentFile ?: File(".")
        val file = File(directory, "cropped_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        cropped.recycle()
        bitmap.recycle()
        return file.absolutePath
    }
}

private const val MAX_AUTO_CROP_SAMPLE_WIDTH = 1200
private const val DARK_PIXEL_THRESHOLD = 235f
