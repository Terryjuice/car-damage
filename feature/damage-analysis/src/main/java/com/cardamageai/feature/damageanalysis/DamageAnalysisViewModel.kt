package com.cardamageai.feature.damageanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardamageai.core.common.di.IODispatcher
import com.cardamageai.core.common.config.ApiKeyManager
import com.cardamageai.core.database.dao.DamageAnalysisDao
import com.cardamageai.core.database.entities.DamageAnalysisEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DamageAnalysisViewModel @Inject constructor(
    private val damageAnalysisDao: DamageAnalysisDao,
    private val damageAnalyzer: DamageAnalyzer,
    private val apiKeyManager: ApiKeyManager,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<DamageAnalysisUiState>(DamageAnalysisUiState.Loading)
    val uiState: StateFlow<DamageAnalysisUiState> = _uiState.asStateFlow()

    fun analyzeImage(imageUri: String) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = DamageAnalysisUiState.Loading
            
            try {
                val apiKey = apiKeyManager.getAnthropicApiKey()
                val analysis = damageAnalyzer.analyzeImage(imageUri, apiKey)
                
                // Сохраняем результат анализа в базу данных
                val entity = DamageAnalysisEntity(
                    imageUri = imageUri,
                    damageType = analysis.damageType,
                    severityLevel = analysis.severityLevel,
                    confidence = analysis.confidence,
                    estimatedCost = analysis.estimatedCost,
                    timestamp = System.currentTimeMillis(),
                    description = analysis.description
                )
                
                damageAnalysisDao.insertAnalysis(entity)
                
                _uiState.value = DamageAnalysisUiState.Success(analysis)
            } catch (e: Exception) {
                _uiState.value = DamageAnalysisUiState.Error(
                    message = e.message ?: "Произошла неизвестная ошибка"
                )
            }
        }
    }
    
    fun updateApiKey(apiKey: String) {
        apiKeyManager.setAnthropicApiKey(apiKey)
    }
}

sealed class DamageAnalysisUiState {
    object Loading : DamageAnalysisUiState()
    data class Success(val analysis: DamageAnalysis) : DamageAnalysisUiState()
    data class Error(val message: String) : DamageAnalysisUiState()
}

data class DamageAnalysis(
    val damageType: String,
    val severityLevel: Int, // 1-5
    val confidence: Float, // 0.0-1.0
    val estimatedCost: Double,
    val description: String?
)