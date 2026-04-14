package com.example.emograph.ui.screens.timeline

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emograph.ui.components.EmotionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNavigateToInput: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: TimelineViewModel = viewModel()
) {
    val records by viewModel.records.collectAsState()
    val filteredRecords = remember(
        records,
        viewModel.sortOrder,
        viewModel.filterCategory,
        viewModel.filterScoreMin,
        viewModel.filterScoreMax
    ) { viewModel.getFilteredRecords() }

    val context = LocalContext.current
    var showFilterSheet by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Delete confirmation dialog
    deleteTargetId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            title = { Text("記録を削除") },
            text = { Text("この記録を削除しますか？この操作は元に戻せません。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord(id)
                    deleteTargetId = null
                }) { Text("削除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetId = null }) { Text("キャンセル") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("EmoGraph", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // クリップボードにコピー（NotebookLM へのペースト用）
                    IconButton(onClick = {
                        val markdown = viewModel.exportMarkdown()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("EmoGraph", markdown))
                        Toast.makeText(context, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "クリップボードにコピー")
                    }
                    // Google Drive などへ共有
                    IconButton(onClick = {
                        val markdown = viewModel.exportMarkdown()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, markdown)
                            putExtra(Intent.EXTRA_SUBJECT, "EmoGraph 感情記録エクスポート")
                        }
                        context.startActivity(Intent.createChooser(intent, "エクスポート"))
                    }) {
                        Icon(Icons.Default.IosShare, contentDescription = "エクスポート")
                    }
                    IconButton(onClick = {
                        viewModel.sortOrder = if (viewModel.sortOrder == SortOrder.NEWEST) {
                            SortOrder.OLDEST
                        } else {
                            SortOrder.NEWEST
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "ソート切替")
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "フィルタ")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.Logout, contentDescription = "サインアウト")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToInput) {
                Icon(Icons.Default.Add, contentDescription = "感情を記録")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                filteredRecords.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (records.isEmpty()) "まだ記録がありません" else "条件に一致する記録がありません",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (records.isEmpty()) "右下のボタンから感情を記録しましょう" else "フィルタを変更してください",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                else -> {
                    Column {
                        // Sort indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredRecords.size}件",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (viewModel.sortOrder == SortOrder.NEWEST) "新しい順" else "古い順",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredRecords, key = { it.id }) { record ->
                                EmotionCard(
                                    record = record,
                                    onDelete = { deleteTargetId = record.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            FilterSheetContent(
                viewModel = viewModel,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@Composable
private fun FilterSheetContent(
    viewModel: TimelineViewModel,
    onDismiss: () -> Unit
) {
    val categories = remember(viewModel.records) { viewModel.getAllCategories() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "フィルタ・ソート",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Category filter chips
        if (categories.isNotEmpty()) {
            Text(
                text = "感情カテゴリー",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                item {
                    FilterChip(
                        selected = viewModel.filterCategory.isEmpty(),
                        onClick = { viewModel.filterCategory = "" },
                        label = { Text("すべて") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = viewModel.filterCategory == cat,
                        onClick = {
                            viewModel.filterCategory =
                                if (viewModel.filterCategory == cat) "" else cat
                        },
                        label = { Text(cat) }
                    )
                }
            }
        }

        // Score range filter
        Text(
            text = "スコア範囲: ${viewModel.filterScoreMin} 〜 ${viewModel.filterScoreMax}",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        RangeSlider(
            value = viewModel.filterScoreMin.toFloat()..viewModel.filterScoreMax.toFloat(),
            onValueChange = { range ->
                viewModel.filterScoreMin = range.start.toInt()
                viewModel.filterScoreMax = range.endInclusive.toInt()
            },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                viewModel.resetFilters()
                onDismiss()
            }) {
                Text("リセット")
            }
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    }
}
