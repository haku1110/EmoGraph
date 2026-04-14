package com.example.emograph.data.model

import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EmotionRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "",
    val score: Int = 5,
    val weather: String = "",
    val temperature: Double = 0.0,
    val eventText: String = "",
    val memoText: String = ""
) {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
        return sdf.format(Date(timestamp))
    }

    fun toMarkdown(): String = buildString {
        appendLine("## ${formattedDate()} - $category（スコア: $score/10）")
        if (weather.isNotBlank()) {
            appendLine("- 天気: $weather / 気温: ${"%.1f".format(temperature)}°C")
        }
        if (eventText.isNotBlank()) appendLine("- 出来事: $eventText")
        if (memoText.isNotBlank()) appendLine("- 備考: $memoText")
        appendLine()
    }
}
