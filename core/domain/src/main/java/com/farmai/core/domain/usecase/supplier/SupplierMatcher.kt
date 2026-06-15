package com.farmai.core.domain.usecase.supplier

import com.farmai.core.domain.model.Supplier
import com.farmai.core.domain.model.SupplierMatch
import com.farmai.core.domain.model.SupplierMergeSuggestion
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object SupplierMatcher {
    private const val MIN_MERGE_CONFIDENCE = 0.90f

    fun normalize(value: String): String {
        return value
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    fun similarity(left: String, right: String): Float {
        val normalizedLeft = normalize(left)
        val normalizedRight = normalize(right)
        if (normalizedLeft == normalizedRight) return 1.0f
        if (normalizedLeft.isBlank() || normalizedRight.isBlank()) return 0.0f
        if (normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) {
            val shorter = min(normalizedLeft.length, normalizedRight.length).toFloat()
            val longer = max(normalizedLeft.length, normalizedRight.length).toFloat()
            return (shorter / longer).coerceIn(0.0f, 1.0f)
        }
        val distance = levenshteinDistance(normalizedLeft, normalizedRight)
        val maxDistance = max(normalizedLeft.length, normalizedRight.length)
        return (1.0f - (distance.toFloat() / maxDistance)).coerceIn(0.0f, 1.0f)
    }

    fun suggestMatches(sourceText: String, suppliers: List<Supplier>, limit: Int = 5): List<SupplierMatch> {
        if (sourceText.isBlank()) return emptyList()

        return suppliers
            .mapNotNull { supplier ->
                val aliasScore = supplier.aliases
                    .filter { it.isNotBlank() }
                    .map { it to similarity(sourceText, it) }
                    .maxByOrNull { it.second }
                val nameScore = similarity(sourceText, supplier.name)
                val bestAlias = aliasScore?.second ?: -1.0f
                val isAlias = bestAlias >= nameScore && bestAlias > 0.0f
                val confidence = max(nameScore, bestAlias)
                val matchedText = if (isAlias) aliasScore?.first.orEmpty() else supplier.name
                if (confidence < supplier.confidenceThreshold.coerceIn(0.0f, 1.0f)) {
                    null
                } else {
                    SupplierMatch(
                        supplier = supplier,
                        matchedText = matchedText,
                        confidence = confidence,
                        isAlias = isAlias,
                        sourceText = sourceText
                    )
                }
            }
            .sortedByDescending { it.confidence }
            .take(limit)
    }

    fun suggestMergeCandidates(suppliers: List<Supplier>, limit: Int = 10): List<SupplierMergeSuggestion> {
        val suggestions = mutableListOf<SupplierMergeSuggestion>()
        val seenPairs = mutableSetOf<String>()

        for (leftIndex in suppliers.indices) {
            for (rightIndex in leftIndex + 1 until suppliers.size) {
                val left = suppliers[leftIndex]
                val right = suppliers[rightIndex]
                val confidence = similarity(left.name, right.name)
                if (confidence < MIN_MERGE_CONFIDENCE) continue

                val pairKey = listOf(left.id, right.id).sorted().joinToString("|")
                if (!seenPairs.add(pairKey)) continue

                suggestions += SupplierMergeSuggestion(
                    leftSupplier = left,
                    rightSupplier = right,
                    confidence = confidence,
                    reason = "Supplier names are ${formatPercent(confidence)} similar"
                )
            }
        }

        return suggestions
            .sortedByDescending { it.confidence }
            .take(limit)
    }

    private fun levenshteinDistance(left: String, right: String): Int {
        val previous = IntArray(right.length + 1) { it }
        val current = IntArray(right.length + 1)

        for (i in 1..left.length) {
            current[0] = i
            for (j in 1..right.length) {
                val substitutionCost = if (left[i - 1] == right[j - 1]) 0 else 1
                current[j] = min(
                    current[j - 1] + 1,
                    min(previous[j] + 1, previous[j - 1] + substitutionCost)
                )
            }
            for (index in previous.indices) {
                previous[index] = current[index]
            }
        }

        return previous[right.length]
    }

    private fun formatPercent(value: Float): String {
        return "${(value * 100.0f).toInt()}%"
    }
}
