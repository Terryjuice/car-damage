package com.cardamageai.feature.damageanalysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cardamageai.core.common.config.ApiKeyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DamageAnalysisScreen(
    imageUri: String,
    onBackPressed: () -> Unit,
    viewModel: DamageAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(imageUri) {
        viewModel.analyzeImage(imageUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Анализ повреждений") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки API"
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Анализируемое изображение",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is DamageAnalysisUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Анализируем повреждения...")
                        }
                    }
                }

                is DamageAnalysisUiState.Success -> {
                    AnalysisResults(analysis = uiState.analysis)
                }

                is DamageAnalysisUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Ошибка анализа",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            if (showSettings) {
                ApiKeySettingsCard(
                    viewModel = viewModel,
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}

@Composable
private fun ApiKeySettingsCard(
    viewModel: DamageAnalysisViewModel,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Настройки API",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Anthropic Claude API Key",
                style = MaterialTheme.typography.labelMedium
            )
            
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("sk-ant-...") },
                visualTransformation = if (isVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isVisible = !isVisible }) {
                        Icon(
                            imageVector = if (isVisible) 
                                Icons.Default.VisibilityOff 
                            else 
                                Icons.Default.Visibility,
                            contentDescription = "Показать/скрыть"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    viewModel.updateApiKey(apiKey)
                    onDismiss()
                },
                enabled = apiKey.startsWith("sk-ant-")
            ) {
                Text("Сохранить ключ")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Получите API ключ на console.anthropic.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalysisResults(analysis: DamageAnalysis) {
    Column {
        Text(
            text = "Результаты анализа",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                ResultItem(
                    label = "Тип повреждения",
                    value = analysis.damageType
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ResultItem(
                    label = "Уровень серьёзности",
                    value = "${analysis.severityLevel}/5"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ResultItem(
                    label = "Уверенность",
                    value = "${(analysis.confidence * 100).toInt()}%"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ResultItem(
                    label = "Предварительная стоимость ремонта",
                    value = String.format("%.0f ₽", analysis.estimatedCost)
                )

                analysis.description?.let { description ->
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultItem(
                        label = "Описание",
                        value = description
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SeverityIndicator(severityLevel = analysis.severityLevel)
    }
}

@Composable
private fun ResultItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SeverityIndicator(severityLevel: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (severityLevel) {
                1, 2 -> MaterialTheme.colorScheme.primaryContainer
                3 -> MaterialTheme.colorScheme.tertiaryContainer
                4, 5 -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Индикатор серьёзности",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { index ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (index < severityLevel) {
                                when {
                                    index < 2 -> MaterialTheme.colorScheme.primary
                                    index < 3 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                }
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        ),
                        modifier = Modifier.size(12.dp)
                    ) {}
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (severityLevel) {
                    1 -> "Минимальное повреждение"
                    2 -> "Лёгкое повреждение"
                    3 -> "Умеренное повреждение"
                    4 -> "Серьёзное повреждение"
                    5 -> "Критическое повреждение"
                    else -> "Неизвестно"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}