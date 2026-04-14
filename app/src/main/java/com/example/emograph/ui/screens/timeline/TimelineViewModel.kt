package com.example.emograph.ui.screens.timeline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emograph.data.model.EmotionRecord
import com.example.emograph.data.repository.EmotionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SortOrder { NEWEST, OLDEST }

class TimelineViewModel : ViewModel() {

    private val _records = MutableStateFlow<List<EmotionRecord>>(emptyList())
    val records: StateFlow<List<EmotionRecord>> = _records.asStateFlow()

    var sortOrder by mutableStateOf(SortOrder.NEWEST)
    var filterCategory by mutableStateOf("")
    var filterScoreMin by mutableIntStateOf(1)
    var filterScoreMax by mutableIntStateOf(10)
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            try {
                EmotionRepository.getRecords().collect { list ->
                    _records.value = list
                    isLoading = false
                }
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

    fun getFilteredRecords(): List<EmotionRecord> {
        var result = _records.value
        if (filterCategory.isNotBlank()) {
            result = result.filter {
                it.category.contains(filterCategory, ignoreCase = true)
            }
        }
        result = result.filter { it.score in filterScoreMin..filterScoreMax }
        return if (sortOrder == SortOrder.NEWEST) {
            result.sortedByDescending { it.timestamp }
        } else {
            result.sortedBy { it.timestamp }
        }
    }

    fun getAllCategories(): List<String> =
        _records.value.map { it.category }.distinct().sorted()

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            try {
                EmotionRepository.deleteRecord(id)
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    fun resetFilters() {
        filterCategory = ""
        filterScoreMin = 1
        filterScoreMax = 10
    }

    fun exportMarkdown(): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        val dateStr = sdf.format(Date())
        return buildString {
            appendLine("# EmoGraph 感情記録 - $dateStr エクスポート")
            appendLine()
            val filtered = getFilteredRecords()
            appendLine("記録件数: ${filtered.size}件")
            appendLine()
            filtered.forEach { append(it.toMarkdown()) }
        }
    }
}
