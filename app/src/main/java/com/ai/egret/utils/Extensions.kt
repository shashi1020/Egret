package com.ai.egret.utils


import android.graphics.Color
import com.ai.egret.models.AdvisoryDto
import com.ai.egret.models.HistoryItemDto
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

/* --- 1. STATUS & COLORS --- */
fun AdvisoryDto.getSeverityColor(): Long {
    // Returning generic ARGB Longs (Replaced with specific Compose Colors in UI)
    return when (severity.uppercase()) {
        "CRITICAL" -> 0xFFFFCDD2 // Red-ish
        "WARNING" -> 0xFFFFE082  // Orange/Yellow
        else -> 0xFFC8E6C9       // Green (INFO)
    }
}

fun AdvisoryDto.getStatusTitle(): String {
    return when (severity.uppercase()) {
        "CRITICAL" -> "Action Required"
        "WARNING" -> "Attention Needed"
        else -> "Crop is Stable"
    }
}

/* --- 2. NATURAL LANGUAGE HISTORY (The "No Graph" Logic) --- */
fun List<HistoryItemDto>.toNaturalLanguageSummary(): String {
    if (this.size < 2) return "Not enough data to analyze trends yet."

    // 1. Get Current and Previous
    val current = this[0]
    val previous = this[1]

    val curScore = current.health_score ?: 0.0
    val prevScore = previous.health_score ?: 0.0
    val diff = curScore - prevScore

    // 2. Parse Date
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateStr = try {
        val date = inputFormat.parse(previous.timestamp ?: "")
        outputFormat.format(date!!)
    } catch (e: Exception) {
        "the last pass"
    }

    // 3. Generate Sentence
    // Tolerance of 2.0 points for "stability"
    return when {
        abs(diff) < 2.0 -> "Your crop health is stable. It is effectively the same as the scan on $dateStr."
        diff > 0 -> "Good news! Your crop health has improved by ${"%.1f".format(diff)} points since $dateStr."
        else -> "Your crop health has declined slightly (${"%.1f".format(diff)}) compared to $dateStr."
    }
}

/* --- 3. FORMATTERS --- */
fun Double?.toPercentage(): String {
    return "${((this ?: 0.0) * 100).toInt()}%"
}

fun String.formatDatePretty(): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val d = input.parse(this)
        output.format(d!!)
    } catch (e: Exception) {
        this
    }
}