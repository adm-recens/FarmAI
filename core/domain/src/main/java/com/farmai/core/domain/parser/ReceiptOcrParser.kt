package com.farmai.core.domain.parser

import com.farmai.core.domain.model.DeductionType
import com.farmai.core.domain.model.ParsedDeduction
import com.farmai.core.domain.model.ParsedLineItem
import com.farmai.core.domain.model.ParsedReceiptData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

object ReceiptOcrParser {
    private val numberRegex = Regex("([0-9]+(?:\\.[0-9]+)?)")
    private val percentageRegex = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%")
    private val phoneRegex = Regex("(\\+?91[-\\s]?)?(\\d{3,4}[-\\s]?\\d{6,7}|\\d{10,11})")
    private val dateSlashDashRegex = Regex("\\b(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{2,4})\\b")
    private val dateCompactRegex = Regex("\\b(\\d{8})\\b")
    private val dateWordsRegex = Regex("\\b(\\d{1,2})\\s+([A-Za-z]{3,9})\\s+(\\d{4})\\b")

    fun parse(rawText: String): ParsedReceiptData {
        if (rawText.isBlank()) {
            return ParsedReceiptData(confidenceScore = 0.0)
        }

        val lines = rawText
            .lines()
            .map { it.replace('\t', ' ').trim() }
            .filter { it.isNotBlank() }
            .map { normalizeSpaces(it) }

        val brokerParts = extractBroker(lines)
        val voucherNumber = extractVoucherNumber(lines)
        val voucherDate = extractVoucherDate(lines)
        val supplierName = extractSupplierName(lines)
        val supplierCode = extractSupplierCode(lines)
        val lineItems = parseLineItems(lines)
        val deductionParts = extractDeductions(lines)

        val fieldConfidence = mutableMapOf<String, Double>()
        if (brokerParts.name != null) fieldConfidence["brokerName"] = 0.75
        if (brokerParts.address != null) fieldConfidence["brokerAddress"] = 0.65
        if (brokerParts.phone != null) fieldConfidence["brokerPhone"] = 0.85
        if (voucherNumber != null) fieldConfidence["voucherNumber"] = 0.80
        if (voucherDate != null) fieldConfidence["voucherDate"] = 0.85
        if (supplierName != null) fieldConfidence["supplierName"] = 0.70
        if (supplierCode != null) fieldConfidence["supplierCode"] = 0.75
        if (lineItems.isNotEmpty()) fieldConfidence["lineItems"] = (0.65 + minOf(lineItems.size * 0.05, 0.20))
        if (deductionParts.hasDeductions()) fieldConfidence["deductions"] = 0.70

        val confidenceScore = if (fieldConfidence.isNotEmpty()) {
            round(fieldConfidence.values.average() * 100.0) / 100.0
        } else {
            0.0
        }

        return ParsedReceiptData(
            brokerName = brokerParts.name,
            brokerAddress = brokerParts.address,
            brokerPhone = brokerParts.phone,
            voucherNumber = voucherNumber,
            voucherDate = voucherDate,
            supplierName = supplierName,
            supplierCode = supplierCode,
            lineItems = lineItems,
            commissionPercent = deductionParts.commissionPercent,
            commissionAmount = deductionParts.commissionAmount,
            damagesAmount = deductionParts.damagesAmount,
            unloadingAmount = deductionParts.unloadingAmount,
            advanceAmount = deductionParts.advanceAmount,
            otherDeductions = deductionParts.otherDeductions,
            confidenceScore = confidenceScore,
            fieldConfidence = fieldConfidence
        )
    }

    private fun extractBroker(lines: List<String>): BrokerParts {
        var name: String? = null
        var address: String? = null
        var phone: String? = null

        lines.take(8).forEach { line ->
            val upperLine = line.uppercase(Locale.ROOT)
            if (phone == null && phoneRegex.containsMatchIn(line)) {
                phone = phoneRegex.find(line)?.value
            }
            if (address == null && looksLikeAddress(upperLine)) {
                address = line
            }
            if (name == null && looksLikeBrokerName(upperLine)) {
                name = line
            }
        }

        if (name == null) {
            name = lines.firstOrNull { line ->
                val upperLine = line.uppercase(Locale.ROOT)
                !looksLikeLabel(upperLine) && !looksLikeTableHeader(upperLine) && !looksLikeTableStop(upperLine) && !looksLikeAddress(upperLine) && !phoneRegex.containsMatchIn(line)
            }
        }

        return BrokerParts(name, address, phone)
    }

    private fun extractVoucherNumber(lines: List<String>): String? {
        lines.forEach { line ->
            val upperLine = line.uppercase(Locale.ROOT)
            if (upperLine.contains("VOUCHER") || upperLine.contains("VCH") || upperLine.contains("INVOICE") || upperLine.contains("RECEIPT NO")) {
                val afterLabel = extractAfterLabel(
                    line,
                    listOf("VOUCHER NO", "VOUCHER NUMBER", "VOUCHER", "VCH NO", "VCH NUMBER", "VCH", "INVOICE NO", "INVOICE NUMBER", "INVOICE", "RECEIPT NO", "RECEIPT NUMBER")
                )
                val voucher = firstAlphanumericToken(afterLabel ?: line)
                if (voucher != null) return voucher
            }
        }

        return lines.take(10).firstNotNullOfOrNull { line ->
            val tokens = line.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (tokens.size == 1 && tokens.first().all { it.isDigit() }) tokens.first()
            else if (tokens.firstOrNull()?.uppercase(Locale.ROOT) == "NO" && tokens.getOrNull(1) != null) tokens[1]
            else null
        }
    }

    private fun extractVoucherDate(lines: List<String>): Long? {
        lines.forEach { line ->
            val upperLine = line.uppercase(Locale.ROOT)
            if (upperLine.contains("DATE") || upperLine.contains("VOUCHER DATE") || upperLine.contains("VCH DATE")) {
                val afterLabel = extractAfterLabel(line, listOf("VOUCHER DATE", "VCH DATE", "DATE"))
                val parsed = parseReceiptDate(afterLabel ?: line)
                if (parsed != null) return parsed
            }
        }

        return lines.firstNotNullOfOrNull { line -> parseReceiptDate(line) }
    }

    private fun extractSupplierName(lines: List<String>): String? {
        lines.forEachIndexed { index, line ->
            val upperLine = line.uppercase(Locale.ROOT)
            if (upperLine.contains("SUPPLIER NAME") || upperLine.contains("FARMER NAME") || upperLine.contains("PRODUCER NAME") || upperLine.contains("GROWER NAME")) {
                val afterLabel = extractAfterLabel(
                    line,
                    listOf("SUPPLIER NAME", "FARMER NAME", "PRODUCER NAME", "GROWER NAME", "SUPPLIER", "FARMER", "PRODUCER", "GROWER", "NAME")
                )
                if (!afterLabel.isNullOrBlank()) return afterLabel
                return lines.getOrNull(index + 1)?.takeIf { it.isNotBlank() }
            }
        }

        return null
    }

    private fun extractSupplierCode(lines: List<String>): String? {
        lines.forEach { line ->
            val upperLine = line.uppercase(Locale.ROOT)
            if (upperLine.contains("SUPP") || upperLine.contains("FARMER") || upperLine.contains("PRODUCER") || upperLine.contains("GROWER") || upperLine.contains("CODE")) {
                val afterLabel = extractAfterLabel(
                    line,
                    listOf("SUPPLIER CODE", "SUPPLIER NAME", "SUPPLIER", "SUPP CODE", "SUPP NAME", "FARMER CODE", "FARMER NAME", "FARMER", "PRODUCER", "GROWER", "CODE")
                )
                val code = extractCode(afterLabel ?: line)
                if (code != null) return code
            }
        }

        return null
    }

    private fun parseLineItems(lines: List<String>): List<ParsedLineItem> {
        val items = mutableListOf<ParsedLineItem>()
        var inTable = false

        lines.forEachIndexed { index, line ->
            val upperLine = line.uppercase(Locale.ROOT)
            if (!inTable && looksLikeTableHeader(upperLine)) {
                inTable = true
                return@forEachIndexed
            }

            if (inTable) {
                if (looksLikeTableStop(upperLine)) {
                    inTable = false
                    return@forEachIndexed
                }

                if (index > 0 && looksLikeTableSeparator(line)) {
                    return@forEachIndexed
                }

                parseLineItem(line)?.let { items += it }
            }
        }

        return items
    }

    private fun parseLineItem(line: String): ParsedLineItem? {
        val tokens = line.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size < 2) return null

        val numericTokens = tokens.mapIndexedNotNull { index, token ->
            val normalized = token.removeSuffix(".").removeSuffix(",").replace(",", "")
            val value = normalized.toDoubleOrNull()
            if (value != null && value > 0.0) NumericToken(index, value) else null
        }

        for (i in 0 until numericTokens.size - 1) {
            val quantity = numericTokens[i].value
            val price = numericTokens[i + 1].value
            if (quantity <= 0.0 || price <= 0.0) continue

            val amountToken = numericTokens.getOrNull(i + 2)
            val expectedAmount = quantity * price
            val amount = amountToken?.value ?: expectedAmount
            if (amountToken == null || numbersClose(amount, expectedAmount)) {
                val grade = tokens.subList(0, numericTokens[i].index).joinToString(" ").ifBlank { null }
                return ParsedLineItem(
                    quantity = quantity,
                    pricePerUnit = price,
                    amount = amount,
                    grade = grade,
                    confidence = if (amountToken == null) 0.65 else 0.85
                )
            }
        }

        return null
    }

    private fun extractDeductions(lines: List<String>): DeductionParts {
        var commissionPercent: Double? = null
        var commissionAmount: Double? = null
        var damagesAmount: Double? = null
        var unloadingAmount: Double? = null
        var advanceAmount: Double? = null
        val otherDeductions = mutableListOf<ParsedDeduction>()

        lines.forEach { line ->
            val upperLine = line.uppercase(Locale.ROOT)

            if (isCommissionLine(upperLine)) {
                commissionPercent = extractPercentage(line) ?: commissionPercent
                val amount = extractLastAmount(line, ignorePercent = true)
                if (amount != null) commissionAmount = amount
            }

            if (isDamagesLine(upperLine) && damagesAmount == null) {
                damagesAmount = extractLastAmount(line, ignorePercent = false)
            }

            if (isUnloadingLine(upperLine) && unloadingAmount == null) {
                unloadingAmount = extractLastAmount(line, ignorePercent = false)
            }

            if (isAdvanceLine(upperLine) && advanceAmount == null) {
                advanceAmount = extractLastAmount(line, ignorePercent = false)
            }

            if (isOtherDeductionLine(upperLine) && damagesAmount == null && unloadingAmount == null && advanceAmount == null) {
                val amount = extractLastAmount(line, ignorePercent = false)
                if (amount != null) {
                    otherDeductions += ParsedDeduction(
                        type = DeductionType.OTHER,
                        amount = amount,
                        description = deductionDescription(line),
                        confidence = 0.55
                    )
                }
            }
        }

        return DeductionParts(commissionPercent, commissionAmount, damagesAmount, unloadingAmount, advanceAmount, otherDeductions)
    }

    private fun isCommissionLine(upperLine: String): Boolean {
        if (upperLine.contains("COMMISSION AGENT") || upperLine.contains("COMM AGENT") || upperLine.contains("BROKER")) return false
        return upperLine.contains("COMM") && (upperLine.contains("%") || upperLine.contains("COMMISSION") || upperLine.contains("DEDUCT"))
    }

    private fun isDamagesLine(upperLine: String): Boolean {
        return (upperLine.contains("DAMAGE") || upperLine.contains("LESS FOR")) && !upperLine.contains("TOTAL")
    }

    private fun isUnloadingLine(upperLine: String): Boolean {
        return upperLine.contains("UNLOAD") || upperLine.contains("LOADING")
    }

    private fun isAdvanceLine(upperLine: String): Boolean {
        if (upperLine.contains("CASH MEMO") || upperLine.contains("CASH SALE")) return false
        return upperLine.contains("ADV") || upperLine.contains("L/F") || upperLine.contains("CASH")
    }

    private fun isOtherDeductionLine(upperLine: String): Boolean {
        if (upperLine.contains("TOTAL") || upperLine.contains("GRAND") || upperLine.contains("NET PAY") || upperLine.contains("COMMISSION") || upperLine.contains("DAMAGE") || upperLine.contains("UNLOAD") || upperLine.contains("ADVANCE")) return false
        return upperLine.contains("DEDUCT") || upperLine.contains("LESS") || upperLine.contains("ROUND") || upperLine.contains("OTHER") || upperLine.contains("TRANSPORT")
    }

    private fun extractAfterLabel(line: String, labels: List<String>): String? {
        val upperLine = line.uppercase(Locale.ROOT)
        labels.forEach { label ->
            val labelIndex = upperLine.indexOf(label)
            if (labelIndex >= 0) {
                var start = labelIndex + label.length
                while (start < line.length && !line[start].isLetterOrDigit()) {
                    start++
                }
                if (start < line.length) {
                    val value = line.substring(start).trim()
                    if (value.isNotBlank()) return value
                }

                val colonIndex = line.indexOf(':', start)
                if (colonIndex >= 0 && colonIndex < line.length - 1) {
                    val colonValue = line.substring(colonIndex + 1).trim()
                    if (colonValue.isNotBlank()) return colonValue
                }
            }
        }

        return extractAfterColon(line)
    }

    private fun extractAfterColon(line: String): String? {
        val idx = line.indexOf(':')
        return if (idx >= 0 && idx < line.length - 1) line.substring(idx + 1).trim().ifBlank { null } else null
    }

    private fun parseReceiptDate(raw: String?): Long? {
        if (raw == null || raw.isBlank()) return null

        val cleaned = raw
            .replace(Regex("[^0-9A-Za-z/.\\-\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleaned.isBlank()) return null

        val dateWordsMatch = dateWordsRegex.find(cleaned)
        if (dateWordsMatch != null) {
            val day = dateWordsMatch.groupValues[1].toIntOrNull()
            val month = monthNumber(dateWordsMatch.groupValues[2])
            val year = dateWordsMatch.groupValues[3].toIntOrNull()
            if (day != null && month != null && year != null && isValidDate(day, month, year)) {
                return calendarTime(year, month, day)
            }
        }

        val slashDashMatch = dateSlashDashRegex.find(cleaned)
        if (slashDashMatch != null) {
            val day = slashDashMatch.groupValues[1].toIntOrNull()
            val month = slashDashMatch.groupValues[2].toIntOrNull()
            val year = normalizeYear(slashDashMatch.groupValues[3].toIntOrNull())
            if (day != null && month != null && year != null && isValidDate(day, month, year)) {
                return calendarTime(year, month, day)
            }
        }

        val compactMatch = dateCompactRegex.find(cleaned)
        if (compactMatch != null) {
            val value = compactMatch.groupValues[1]
            val day = value.substring(0, 2).toIntOrNull()
            val month = value.substring(2, 4).toIntOrNull()
            val year = normalizeYear(value.substring(4, 8).toIntOrNull())
            if (day != null && month != null && year != null && isValidDate(day, month, year)) {
                return calendarTime(year, month, day)
            }
        }

        val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "ddMMyyyy", "yyyy-MM-dd")
        formats.forEach { format ->
            runCatching {
                SimpleDateFormat(format, Locale.ROOT).parse(cleaned)?.time
            }.getOrNull()?.let { return it }
        }

        return null
    }

    private fun extractCode(text: String): String? {
        val normalized = text.uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9/-]"), " ")
        return normalized
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .firstOrNull { candidate ->
                candidate.length in 2..8 && candidate.any { it.isLetter() } && !candidate.contains("FARMER") && !candidate.contains("SUPPLIER") && !candidate.contains("CODE")
            }
    }

    private fun firstAlphanumericToken(text: String): String? {
        val normalized = text.uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9/-]"), " ")
        return normalized
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .firstOrNull { token ->
                token.any { it.isLetterOrDigit() } && token != "DATE" && token != "NO" && token != "NUMBER"
            }
    }

    private fun extractPercentage(line: String): Double? {
        return percentageRegex.find(line)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractLastAmount(line: String, ignorePercent: Boolean): Double? {
        val matches = numberRegex.findAll(line).toList()
        if (matches.isEmpty()) return null

        val filtered = if (ignorePercent) {
            matches.filter { match ->
                val prefix = line.substring(0, match.range.first).trimEnd()
                !prefix.endsWith("%")
            }
        } else {
            matches
        }

        return (filtered.ifEmpty { matches }).lastOrNull()?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun deductionDescription(line: String): String {
        return line
            .replace(numberRegex, "")
            .replace(Regex("[^A-Za-z0-9 /-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "Other deduction" }
    }

    private fun looksLikeBrokerName(upperLine: String): Boolean {
        return upperLine.contains("BROS") || upperLine.contains("BRO") || upperLine.contains("BROKER") || upperLine.contains("AGENT") || upperLine.contains("COMMISSION")
    }

    private fun looksLikeAddress(upperLine: String): Boolean {
        return upperLine.contains("ROAD") || upperLine.contains("STREET") || upperLine.contains("ST ") || upperLine.contains("LANE") || upperLine.contains("AREA") || upperLine.contains("HYDERABAD") || upperLine.contains("500")
    }

    private fun looksLikeLabel(upperLine: String): Boolean {
        return upperLine.contains("VOUCHER") || upperLine.contains("DATE") || upperLine.contains("SUPP") || upperLine.contains("FARMER") || upperLine.contains("QTY") || upperLine.contains("PRICE") || upperLine.contains("AMOUNT")
    }

    private fun looksLikeTableHeader(upperLine: String): Boolean {
        return (upperLine.contains("QTY") || upperLine.contains("QUANTITY") || upperLine.contains("RATE")) && (upperLine.contains("PRICE") || upperLine.contains("RATE") || upperLine.contains("AMOUNT"))
    }

    private fun looksLikeTableStop(upperLine: String): Boolean {
        return upperLine.contains("TOTAL") || upperLine.contains("GRAND") || upperLine.contains("NET") || upperLine.contains("COMM") || upperLine.contains("DAMAGE") || upperLine.contains("UNLOAD") || upperLine.contains("ADV") || upperLine.contains("L/F") || upperLine.contains("DEDUCT")
    }

    private fun looksLikeTableSeparator(line: String): Boolean {
        return line.all { it == '-' || it == '_' || it == '=' || it == '.' || it.isWhitespace() }
    }

    private fun normalizeSpaces(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }

    private fun numbersClose(left: Double, right: Double): Boolean {
        return abs(left - right) <= maxOf(1.0, right * 0.05)
    }

    private fun monthNumber(month: String): Int? {
        val names = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        return names.indexOfFirst { month.uppercase(Locale.ROOT).startsWith(it) } + 1
    }

    private fun normalizeYear(year: Int?): Int? {
        if (year == null) return null
        return if (year in 0..30) 2000 + year else year
    }

    private fun isValidDate(day: Int, month: Int, year: Int): Boolean {
        if (month !in 1..12 || day < 1 || year < 1900) return false
        val calendar = GregorianCalendar(year, month - 1, 1)
        return day <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun calendarTime(year: Int, month: Int, day: Int): Long {
        return GregorianCalendar(year, month - 1, day, 0, 0, 0).timeInMillis
    }

    private data class NumericToken(val index: Int, val value: Double)
    private data class BrokerParts(val name: String?, val address: String?, val phone: String?)
    private data class DeductionParts(
        val commissionPercent: Double?,
        val commissionAmount: Double?,
        val damagesAmount: Double?,
        val unloadingAmount: Double?,
        val advanceAmount: Double?,
        val otherDeductions: List<ParsedDeduction>
    ) {
        fun hasDeductions(): Boolean {
            return commissionPercent != null || commissionAmount != null || damagesAmount != null || unloadingAmount != null || advanceAmount != null || otherDeductions.isNotEmpty()
        }
    }
}
