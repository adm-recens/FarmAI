package com.farmai.feature.receipt.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.farmai.core.domain.model.Batch
import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptJob
import com.farmai.core.domain.model.ReceiptLineItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ExportShare {
    private const val HISTORY_FILE_NAME = "export_history.jsonl"
    private val json = Json { ignoreUnknownKeys = true }

    fun shareReceiptCsv(
        context: Context,
        receipt: Receipt,
        lineItems: List<ReceiptLineItem>,
        deductions: List<Deduction>
    ): File {
        val csv = buildReceiptCsv(receipt, lineItems, deductions)
        val file = writeCsv(context, "receipt_${receipt.voucherNumber.ifBlank { receipt.id.takeLast(8) }}", csv)
        shareFile(
            context = context,
            file = file,
            mimeType = "text/csv",
            chooserTitle = "Share FarmAI receipt",
            subject = "FarmAI receipt ${receipt.voucherNumber}",
            extraText = csv
        )
        recordExport(context, "Receipt CSV", "receipt", file.name, lineItems.size + deductions.size)
        return file
    }

    fun shareReceiptPdf(
        context: Context,
        receipt: Receipt,
        lineItems: List<ReceiptLineItem>,
        deductions: List<Deduction>
    ): File {
        val lines = buildReceiptPdfLines(receipt, lineItems, deductions)
        val file = writePdf(context, "receipt_${receipt.voucherNumber.ifBlank { receipt.id.takeLast(8) }}", lines)
        shareFile(
            context = context,
            file = file,
            mimeType = "application/pdf",
            chooserTitle = "Share FarmAI receipt PDF",
            subject = "FarmAI receipt ${receipt.voucherNumber}",
            extraText = "FarmAI receipt export"
        )
        recordExport(context, "Receipt PDF", "receipt", file.name, lineItems.size + deductions.size)
        return file
    }

    fun shareBatchCsv(context: Context, batch: Batch, jobs: List<ReceiptJob>): File {
        val csv = buildBatchCsv(batch, jobs)
        val file = writeCsv(context, "batch_${batch.name}", csv)
        shareFile(
            context = context,
            file = file,
            mimeType = "text/csv",
            chooserTitle = "Share FarmAI batch",
            subject = "FarmAI batch ${batch.name}",
            extraText = csv
        )
        recordExport(context, "Batch CSV", "batch", file.name, jobs.size)
        return file
    }

    fun shareBatchPdf(context: Context, batch: Batch, jobs: List<ReceiptJob>): File {
        val lines = buildBatchPdfLines(batch, jobs)
        val file = writePdf(context, "batch_${batch.name}", lines)
        shareFile(
            context = context,
            file = file,
            mimeType = "application/pdf",
            chooserTitle = "Share FarmAI batch PDF",
            subject = "FarmAI batch ${batch.name}",
            extraText = "FarmAI batch export"
        )
        recordExport(context, "Batch PDF", "batch", file.name, jobs.size)
        return file
    }

    fun shareReportCsv(context: Context, reportType: String, csv: String): File {
        val file = writeCsv(context, "reports_${reportType.lowercase(Locale.US)}", csv)
        shareFile(
            context = context,
            file = file,
            mimeType = "text/csv",
            chooserTitle = "Share FarmAI report",
            subject = "FarmAI $reportType report",
            extraText = csv
        )
        recordExport(context, "Report CSV", "report", file.name, csv.lines().size.coerceAtLeast(0))
        return file
    }

    fun shareReportPdf(context: Context, reportType: String, lines: List<String>): File {
        val file = writePdf(context, "reports_${reportType.lowercase(Locale.US)}", lines)
        shareFile(
            context = context,
            file = file,
            mimeType = "application/pdf",
            chooserTitle = "Share FarmAI report PDF",
            subject = "FarmAI $reportType report",
            extraText = "FarmAI report export"
        )
        recordExport(context, "Report PDF", "report", file.name, lines.size)
        return file
    }

    fun recordExport(context: Context, title: String, type: String, fileName: String, rowCount: Int) {
        runCatching {
            val historyFile = historyFile(context)
            historyFile.parentFile?.mkdirs()
            val item = ExportHistoryItem(
                timestamp = LocalDateTime.now().toString(),
                title = title,
                type = type,
                fileName = fileName,
                rowCount = rowCount
            )
            historyFile.appendText("${json.encodeToString(item)}\n", Charsets.UTF_8)
        }
    }

    fun readExportHistory(context: Context): List<ExportHistoryItem> {
        val historyFile = historyFile(context)
        if (!historyFile.exists()) return emptyList()
        return runCatching {
            historyFile.readLines(Charsets.UTF_8)
                .mapNotNull { line -> runCatching { json.decodeFromString<ExportHistoryItem>(line) }.getOrNull() }
                .takeLast(10)
                .asReversed()
        }.getOrDefault(emptyList())
    }

    internal fun writeCsv(context: Context, baseName: String, csv: String): File {
        val directory = context.getExternalFilesDir("exports") ?: context.filesDir
        directory.mkdirs()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val safeName = baseName.replace(Regex("[^A-Za-z0-9_.-]"), "_")
        val file = File(directory, "${safeName}_$timestamp.csv")
        file.writeText(csv, Charsets.UTF_8)
        return file
    }

    private fun writePdf(context: Context, baseName: String, lines: List<String>): File {
        val directory = context.getExternalFilesDir("exports") ?: context.filesDir
        directory.mkdirs()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val safeName = baseName.replace(Regex("[^A-Za-z0-9_.-]"), "_")
        val file = File(directory, "${safeName}_$timestamp.pdf")
        val pdf = PdfDocument()
        try {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 48f
            val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 12f
            }
            val titlePaint = Paint(normalPaint).apply {
                typeface = Typeface.DEFAULT_BOLD
                textSize = 16f
            }
            val sectionPaint = Paint(normalPaint).apply {
                typeface = Typeface.DEFAULT_BOLD
                textSize = 13f
            }
            var pageIndex = 1
            var y = margin
            var page = startPdfPage(pdf, pageWidth, pageHeight, pageIndex)
            var canvas = page.canvas
            fun newPageIfNeeded(requiredHeight: Float) {
                if (y + requiredHeight > pageHeight - margin) {
                    pdf.finishPage(page)
                    pageIndex += 1
                    y = margin
                    page = startPdfPage(pdf, pageWidth, pageHeight, pageIndex)
                    canvas = page.canvas
                }
            }
            lines.forEachIndexed { index, rawLine ->
                val paint = if (index == 0) titlePaint else if (rawLine.isBlank() || rawLine.endsWith(':')) sectionPaint else normalPaint
                val chunks = wrapLine(rawLine, paint, pageWidth - margin * 2f)
                newPageIfNeeded((chunks.size * 18f).coerceAtLeast(18f))
                chunks.forEach { line ->
                    newPageIfNeeded(18f)
                    canvas.drawText(line, margin, y, paint)
                    y += if (line.isBlank()) 8f else 18f
                }
                y += 4f
            }
            pdf.finishPage(page)
            FileOutputStream(file).use { output -> pdf.writeTo(output) }
        } finally {
            pdf.close()
        }
        return file
    }

    private fun startPdfPage(pdf: PdfDocument, width: Int, height: Int, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageNumber).create()
        return pdf.startPage(pageInfo)
    }

    private fun shareFile(
        context: Context,
        file: File,
        mimeType: String,
        chooserTitle: String,
        subject: String,
        extraText: String
    ) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, extraText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
        Toast.makeText(context, "Export saved to ${file.name}", Toast.LENGTH_LONG).show()
    }

    private fun buildReceiptPdfLines(
        receipt: Receipt,
        lineItems: List<ReceiptLineItem>,
        deductions: List<Deduction>
    ): List<String> {
        val gross = lineItems.sumOf { it.amount }
        val totalDeductions = deductions.sumOf { it.amount }
        val net = gross - totalDeductions
        return buildList {
            add("FarmAI Receipt Export")
            add("Generated: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
            add("Voucher: ${receipt.voucherNumber}")
            add("Date: ${receipt.voucherDate.toReceiptDate()}")
            add("Farmer ID: ${receipt.farmerId}")
            add("Broker ID: ${receipt.brokerId}")
            add("Status: ${receipt.status.name}")
            add("")
            add("Line Items:")
            add("Quantity | Price Per Unit | Amount | Grade")
            lineItems.forEach { item ->
                add("${item.quantity.csv()} | ${item.pricePerUnit.csv()} | ${item.amount.csv()} | ${item.grade.orEmpty()}")
            }
            add("")
            add("Deductions:")
            add("Type | Amount | Description | Percentage Value")
            deductions.forEach { deduction ->
                add("${deduction.type.name} | ${deduction.amount.csv()} | ${deduction.description.orEmpty()} | ${deduction.percentageValue.csv()}")
            }
            add("")
            add("Totals:")
            add("Gross: ${gross.csv()}")
            add("Deductions: ${totalDeductions.csv()}")
            add("Net: ${net.csv()}")
        }
    }

    private fun buildBatchPdfLines(batch: Batch, jobs: List<ReceiptJob>): List<String> {
        return buildList {
            add("FarmAI Batch Export")
            add("Generated: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
            add("Batch: ${batch.name}")
            add("Status: ${batch.status.name}")
            add("Total Images: ${batch.totalImages}")
            add("Processed: ${batch.processedCount}")
            add("Validated: ${batch.validatedCount}")
            add("Failed: ${batch.failedCount}")
            add("")
            add("Jobs:")
            add("Job ID | Status | Receipt ID | Confidence | Error")
            jobs.forEach { job ->
                add("${job.id.takeLast(8)} | ${job.status.name} | ${job.receiptId.orEmpty()} | ${job.confidenceScore.csv()} | ${job.error.orEmpty()}")
            }
        }
    }

    private fun buildReceiptCsv(
        receipt: Receipt,
        lineItems: List<ReceiptLineItem>,
        deductions: List<Deduction>
    ): String {
        val gross = lineItems.sumOf { it.amount }
        val totalDeductions = deductions.sumOf { it.amount }
        val net = gross - totalDeductions
        val builder = StringBuilder()
        builder.appendLine("FarmAI Receipt Export")
        builder.appendLine("Voucher,${csvCell(receipt.voucherNumber)}")
        builder.appendLine("Date,${csvCell(receipt.voucherDate.toReceiptDate())}")
        builder.appendLine("Farmer ID,${csvCell(receipt.farmerId)}")
        builder.appendLine("Broker ID,${csvCell(receipt.brokerId)}")
        builder.appendLine("Status,${receipt.status.name}")
        builder.appendLine()
        builder.appendLine("Line Items")
        builder.appendLine("Quantity,Price Per Unit,Amount,Grade")
        lineItems.forEach { item ->
            builder.appendLine(
                "${item.quantity.csv()},${item.pricePerUnit.csv()},${item.amount.csv()},${csvCell(item.grade.orEmpty())}"
            )
        }
        builder.appendLine()
        builder.appendLine("Deductions")
        builder.appendLine("Type,Amount,Description,Percentage Value")
        deductions.forEach { deduction ->
            builder.appendLine(
                "${deduction.type.name},${deduction.amount.csv()},${csvCell(deduction.description.orEmpty())},${deduction.percentageValue.csv()}"
            )
        }
        builder.appendLine()
        builder.appendLine("Totals")
        builder.appendLine("Gross,${gross.csv()}")
        builder.appendLine("Deductions,${totalDeductions.csv()}")
        builder.appendLine("Net,${net.csv()}")
        return builder.toString()
    }

    private fun buildBatchCsv(batch: Batch, jobs: List<ReceiptJob>): String {
        val builder = StringBuilder()
        builder.appendLine("FarmAI Batch Export")
        builder.appendLine("Batch,${csvCell(batch.name)}")
        builder.appendLine("Status,${batch.status.name}")
        builder.appendLine("Total Images,${batch.totalImages}")
        builder.appendLine("Processed,${batch.processedCount}")
        builder.appendLine("Validated,${batch.validatedCount}")
        builder.appendLine("Failed,${batch.failedCount}")
        builder.appendLine()
        builder.appendLine("Jobs")
        builder.appendLine("Job ID,Batch ID,Receipt ID,Status,Image Path,Crop Box,OCR Text,Parser JSON,Confidence,Error")
        jobs.forEach { job ->
            builder.appendLine(
                listOf(
                    job.id,
                    job.batchId,
                    job.receiptId.orEmpty(),
                    job.status.name,
                    job.imagePath.orEmpty(),
                    job.cropBoxJson.orEmpty(),
                    job.ocrRawText.orEmpty(),
                    job.parserJson.orEmpty(),
                    job.confidenceScore.csv(),
                    job.error.orEmpty()
                ).joinToString(",") { csvCell(it) }
            )
        }
        return builder.toString()
    }

    private fun historyFile(context: Context): File {
        val directory = context.getExternalFilesDir("exports") ?: context.filesDir
        directory.mkdirs()
        return File(directory, HISTORY_FILE_NAME)
    }

    private fun wrapLine(line: String, paint: Paint, maxWidth: Float): List<String> {
        if (line.isEmpty()) return listOf("")
        val result = mutableListOf<String>()
        var remaining = line
        while (remaining.isNotEmpty()) {
            val count = paint.breakText(remaining, false, maxWidth, null).coerceAtLeast(1)
            result += remaining.take(count)
            remaining = remaining.drop(count)
        }
        return result
    }

    private fun csvCell(value: String): String {
        return if (value.containsAny(',', '"', '\n', '\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun String.containsAny(vararg chars: Char): Boolean {
        return chars.any { contains(it) }
    }

    private fun Double.csv(): String = String.format(Locale.US, "%.2f", this)

    private fun Double?.csv(): String = this?.csv() ?: ""

    private fun Long.toReceiptDate(): String {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(this), java.time.ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @Serializable
    data class ExportHistoryItem(
        val timestamp: String,
        val title: String,
        val type: String,
        val fileName: String,
        val rowCount: Int
    )
}
