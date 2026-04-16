package com.kha98.emograph.ui.screens.timeline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kha98.emograph.data.model.EmotionRecord
import com.kha98.emograph.data.repository.EmotionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SortOrder { NEWEST, OLDEST }
enum class ExportType { COPY, SHARE }

class TimelineViewModel : ViewModel() {

    private val _records = MutableStateFlow<List<EmotionRecord>>(emptyList())
    val records: StateFlow<List<EmotionRecord>> = _records.asStateFlow()

    var sortOrder by mutableStateOf(SortOrder.NEWEST)
    var filterCategory by mutableStateOf("")
    var filterScoreMin by mutableIntStateOf(1)
    var filterScoreMax by mutableIntStateOf(10)
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    // 選択・エクスポート状態
    var isSelectionMode by mutableStateOf(false)
    var selectedIds by mutableStateOf(emptySet<String>())
    var pendingExportType by mutableStateOf<ExportType?>(null)

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
                selectedIds = selectedIds - id
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

    // エクスポートボタン押下 → 選択モード開始
    fun startExport(type: ExportType) {
        pendingExportType = type
        isSelectionMode = true
        selectedIds = emptySet()
    }

    // 選択確定 → Markdown 文字列を返してモードを終了
    fun confirmExport(): String {
        val targets = getFilteredRecords().filter { it.id in selectedIds }
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        val dateStr = sdf.format(Date())
        val markdown = buildString {
            appendLine("# EmoGraph 感情記録 - $dateStr エクスポート")
            appendLine()
            appendLine("記録件数: ${targets.size}件")
            appendLine()
            targets.forEach { append(it.toMarkdown()) }
        }
        cancelExport()
        return markdown
    }

    fun cancelExport() {
        pendingExportType = null
        isSelectionMode = false
        selectedIds = emptySet()
    }

    fun toggleRecord(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun selectAll(records: List<EmotionRecord>) {
        selectedIds = records.map { it.id }.toSet()
    }
}
