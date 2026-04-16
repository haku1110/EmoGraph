package com.kha98.emograph.ui.screens.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kha98.emograph.data.model.EmotionRecord
import com.kha98.emograph.data.repository.EmotionRepository
import com.kha98.emograph.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InputViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val recordId: String = savedStateHandle["recordId"] ?: ""
    val isEditing: Boolean = recordId.isNotBlank()
    private var originalTimestamp: Long = System.currentTimeMillis()

    var category by mutableStateOf("")
    var score by mutableFloatStateOf(5f)
    var weather by mutableStateOf("")
    var weatherEmoji by mutableStateOf("")
    var temperature by mutableDoubleStateOf(0.0)
    var eventText by mutableStateOf("")
    var memoText by mutableStateOf("")
    var isWeatherLoading by mutableStateOf(false)
    var weatherError by mutableStateOf<String?>(null)
    var isSaving by mutableStateOf(false)
    var saveError by mutableStateOf<String?>(null)
    var isLoadingRecord by mutableStateOf(isEditing)

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            EmotionRepository.getCategories().collect {
                _categories.value = it
            }
        }
        if (isEditing) {
            viewModelScope.launch { loadRecord(recordId) }
        }
    }

    private suspend fun loadRecord(id: String) {
        EmotionRepository.getRecordById(id)?.let { record ->
            originalTimestamp = record.timestamp
            category = record.category
            score = record.score.toFloat()
            // weather フィールドはそのまま保持（"☀️ 快晴" など）
            weather = record.weather
            weatherEmoji = ""
            temperature = record.temperature
            eventText = record.eventText
            memoText = record.memoText
        }
        isLoadingRecord = false
    }

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            isWeatherLoading = true
            weatherError = null
            try {
                val data = WeatherRepository.getWeather(latitude, longitude)
                weather = data.description
                weatherEmoji = data.emoji
                temperature = data.temperature
            } catch (e: Exception) {
                weatherError = "天気の取得に失敗しました"
            } finally {
                isWeatherLoading = false
            }
        }
    }

    fun saveRecord(onSuccess: () -> Unit) {
        if (category.isBlank()) {
            saveError = "カテゴリーを入力してください"
            return
        }
        viewModelScope.launch {
            isSaving = true
            saveError = null
            try {
                val record = EmotionRecord(
                    id = if (isEditing) recordId else EmotionRecord().id,
                    timestamp = if (isEditing) originalTimestamp else System.currentTimeMillis(),
                    category = category.trim(),
                    score = score.toInt(),
                    weather = if (weatherEmoji.isNotBlank()) "$weatherEmoji $weather" else weather,
                    temperature = temperature,
                    eventText = eventText.trim(),
                    memoText = memoText.trim()
                )
                EmotionRepository.addRecord(record)
                onSuccess()
            } catch (e: Exception) {
                saveError = "保存に失敗しました: ${e.message}"
                isSaving = false
            }
        }
    }
}
