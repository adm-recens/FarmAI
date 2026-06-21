package com.farmai.feature.receipt.work

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.farmai.core.domain.model.CropBoxJson
import com.farmai.core.domain.model.ReceiptJobStatus
import com.farmai.core.domain.parser.ReceiptOcrParser
import com.farmai.core.domain.repository.BatchRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class BatchWorkflowWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val batchRepository: BatchRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val batchId = inputData.getString("batchId") ?: return Result.failure()
        processQueueToCrop(batchId)
        processCropToOcr(batchId)
        processParsedToReview(batchId)
        return Result.success()
    }

    private suspend fun processQueueToCrop(batchId: String) {
        val jobs = batchRepository.getQueuedJobs(batchId)
        jobs.forEach { job ->
            try {
                val imagePath = job.imagePath ?: return@forEach
                val bitmap = BitmapFactory.decodeFile(imagePath) ?: return@forEach

                val cropBox = job.cropBoxJson?.let { CropBoxJson.decode(it) }
                    ?: CropBoxJson.autoDetect(bitmap.width, bitmap.height)

                val croppedPath = generateCroppedImage(bitmap, cropBox, job.id)
                batchRepository.updateJobCropBox(
                    job.id,
                    CropBoxJson.encode(cropBox),
                    cropBox.confidence.toDouble(),
                    croppedPath
                )
            } catch (e: Exception) {
                batchRepository.markJobFailed(job.id, "Crop failed: ${e.message}")
            }
        }
    }

    private suspend fun processCropToOcr(batchId: String) {
        val jobs = batchRepository.getCroppedJobs(batchId)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        jobs.forEach { job ->
            try {
                val imagePath = job.croppedImagePath ?: job.imagePath ?: return@forEach
                val image = InputImage.fromFilePath(applicationContext, Uri.parse("file://$imagePath"))
                val visionText = runBlockingOcr(recognizer, image)
                val parsed = ReceiptOcrParser.parse(visionText.text)
                batchRepository.updateJobOcrResult(job.id, visionText.text, parsed.confidenceScore)
            } catch (e: Exception) {
                batchRepository.markJobFailed(job.id, "OCR failed: ${e.message}")
            }
        }

        recognizer.close()
    }

    private suspend fun processParsedToReview(batchId: String) {
        val jobs = batchRepository.observeJobsByBatch(batchId).first()
        val parsedJobs = jobs.filter {
            !it.ocrRawText.isNullOrBlank() && it.status == ReceiptJobStatus.PARSED
        }
        parsedJobs.forEach { job ->
            try {
                val text = job.ocrRawText ?: return@forEach
                val parsedData = ReceiptOcrParser.parse(text)
                if (parsedData.confidenceScore > 0.3) {
                    batchRepository.updateJobStatus(job.id, ReceiptJobStatus.NEEDS_VALIDATION)
                }
            } catch (e: Exception) {
                batchRepository.markJobFailed(job.id, "Parse failed: ${e.message}")
            }
        }
    }

    private suspend fun runBlockingOcr(
        recognizer: com.google.mlkit.vision.text.TextRecognizer,
        image: InputImage
    ): com.google.mlkit.vision.text.Text {
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { cont.resume(it) { true } }
                .addOnFailureListener { cont.cancel(it) }
        }
    }

    private fun generateCroppedImage(bitmap: Bitmap, cropBox: com.farmai.core.domain.model.CropBox, jobId: String): String {
        val x = cropBox.x.toInt().coerceIn(0, bitmap.width - 1)
        val y = cropBox.y.toInt().coerceIn(0, bitmap.height - 1)
        val w = cropBox.width.toInt().coerceAtMost(bitmap.width - x).coerceAtLeast(1)
        val h = cropBox.height.toInt().coerceAtMost(bitmap.height - y).coerceAtLeast(1)

        val cropped = Bitmap.createBitmap(bitmap, x, y, w, h)
        val directory = applicationContext.getExternalFilesDir("cropped_receipts")
            ?: applicationContext.filesDir
        directory.mkdirs()
        val file = File(directory, "${jobId}_cropped.jpg")
        FileOutputStream(file).use { output ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        cropped.recycle()
        return file.absolutePath
    }

    companion object {
        fun enqueue(context: Context, batchId: String) {
            val work = OneTimeWorkRequestBuilder<BatchWorkflowWorker>()
                .setInputData(workDataOf("batchId" to batchId))
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "batch_workflow_$batchId",
                    ExistingWorkPolicy.REPLACE,
                    work
                )
        }
    }
}