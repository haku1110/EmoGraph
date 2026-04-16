package com.kha98.emograph.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Entity(tableName = "emotion_records")
data class EmotionRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
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
