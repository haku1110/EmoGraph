package com.kha98.emograph.ui.screens.input

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kha98.emograph.ui.components.scoreToColor
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.roundToInt

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    onNavigateBack: () -> Unit,
    viewModel: InputViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val categories by viewModel.categories.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var lastSliderInt by remember { mutableStateOf(viewModel.score.roundToInt()) }

    fun fetchLocationAndWeather() {
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                location?.let { viewModel.fetchWeather(it.latitude, it.longitude) }
            }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocationAndWeather()
    }

    // 新規記録のときだけ起動時に天気を自動取得
    LaunchedEffect(Unit) {
        if (!viewModel.isEditing) {
            when {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> fetchLocationAndWeather()
                else -> locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    val filteredSuggestions = remember(categories, viewModel.category) {
        if (viewModel.category.isBlank()) emptyList()
        else categories.filter {
            it.contains(viewModel.category, ignoreCase = true) && it != viewModel.category
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "記録を編集" else "感情を記録", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Category input with suggestions
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded && filteredSuggestions.isNotEmpty(),
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = viewModel.category,
                    onValueChange = {
                        viewModel.category = it
                        dropdownExpanded = true
                    },
                    label = { Text("感情カテゴリー *") },
                    placeholder = { Text("例: 幸福感、不安、達成感") },
                    singleLine = true,
                    isError = viewModel.saveError?.contains("カテゴリー") == true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                )
                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    viewModel.category = suggestion
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Score slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("スコア", style = MaterialTheme.typography.titleSmall)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(scoreToColor(viewModel.score.roundToInt())),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.score.roundToInt().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Slider(
                    value = viewModel.score,
                    onValueChange = { newVal ->
                        viewModel.score = newVal
                        val newInt = newVal.roundToInt()
                        if (newInt != lastSliderInt) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastSliderInt = newInt
                        }
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = scoreToColor(viewModel.score.roundToInt()),
                        activeTrackColor = scoreToColor(viewModel.score.roundToInt())
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("10", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Weather display
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        when {
                            viewModel.isWeatherLoading -> {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            }
                            viewModel.weatherError != null -> {
                                Text(
                                    viewModel.weatherError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            viewModel.weather.isNotBlank() -> {
                                Column {
                                    Text(
                                        text = "${viewModel.weatherEmoji} ${viewModel.weather}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${"%.1f".format(viewModel.temperature)}°C",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    "天気を取得中...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    IconButton(onClick = { fetchLocationAndWeather() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "天気を更新",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Event text
            OutlinedTextField(
                value = viewModel.eventText,
                onValueChange = { viewModel.eventText = it },
                label = { Text("出来事") },
                placeholder = { Text("今日何があったか書いてみましょう") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            // Memo text
            OutlinedTextField(
                value = viewModel.memoText,
                onValueChange = { viewModel.memoText = it },
                label = { Text("備考") },
                placeholder = { Text("その他メモなど") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Error message
            viewModel.saveError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save button
            Button(
                onClick = { viewModel.saveRecord(onNavigateBack) },
                enabled = !viewModel.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(if (viewModel.isEditing) "更新する" else "記録する", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
