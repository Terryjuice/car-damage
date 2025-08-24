package com.cardamageai.core.network

import android.graphics.Bitmap
import android.util.Base64
import com.cardamageai.core.common.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaudeRepository @Inject constructor(
    private val claudeApiService: ClaudeApiService
) {
    
    suspend fun analyzeCarDamage(
        bitmap: Bitmap,
        apiKey: String
    ): Result<CarDamageAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val base64Image = bitmapToBase64(bitmap)
            
            val request = ClaudeMessageRequest(
                messages = listOf(
                    ClaudeMessage(
                        role = "user",
                        content = listOf(
                            ClaudeContent.Text(
                                text = """
                                Проанализируй это изображение автомобиля и определи повреждения.
                                
                                Ответь в формате JSON:
                                {
                                  "damageType": "тип повреждения (царапина/вмятина/трещина/ржавчина/разбитое стекло/повреждение краски)",
                                  "severityLevel": числовое значение от 1 до 5,
                                  "confidence": уверенность от 0.0 до 1.0,
                                  "estimatedCost": предполагаемая стоимость ремонта в рублях,
                                  "description": "подробное описание повреждения",
                                  "repairRecommendations": "рекомендации по ремонту"
                                }
                                
                                Если не видишь повреждений автомобиля, укажи damageType как "не обнаружено".
                                """.trimIndent()
                            ),
                            ClaudeContent.Image(
                                source = ClaudeImageSource(
                                    data = base64Image
                                )
                            )
                        )
                    )
                )
            )
            
            val response = claudeApiService.analyzeImage(
                apiKey = apiKey,
                request = request
            )
            
            val analysisText = response.content.firstOrNull()?.text
                ?: throw Exception("Пустой ответ от Claude API")
            
            val analysis = parseClaudeResponse(analysisText)
            Result.Success(analysis)
            
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    private fun parseClaudeResponse(responseText: String): CarDamageAnalysisResult {
        try {
            // Простой парсинг JSON-ответа
            // В реальном приложении лучше использовать Gson или другую библиотеку
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}")
            
            if (jsonStart == -1 || jsonEnd == -1) {
                throw Exception("JSON не найден в ответе")
            }
            
            val jsonString = responseText.substring(jsonStart, jsonEnd + 1)
            
            // Извлекаем значения из JSON (упрощенный подход)
            val damageType = extractJsonValue(jsonString, "damageType") ?: "не обнаружено"
            val severityLevel = extractJsonValue(jsonString, "severityLevel")?.toIntOrNull() ?: 1
            val confidence = extractJsonValue(jsonString, "confidence")?.toFloatOrNull() ?: 0.5f
            val estimatedCost = extractJsonValue(jsonString, "estimatedCost")?.toDoubleOrNull() ?: 0.0
            val description = extractJsonValue(jsonString, "description") ?: "Анализ не выполнен"
            val repairRecommendations = extractJsonValue(jsonString, "repairRecommendations") ?: ""
            
            return CarDamageAnalysisResult(
                damageType = damageType,
                severityLevel = severityLevel,
                confidence = confidence,
                estimatedCost = estimatedCost,
                description = description,
                repairRecommendations = repairRecommendations
            )
            
        } catch (e: Exception) {
            // Fallback анализ на основе текста
            return CarDamageAnalysisResult(
                damageType = "общее повреждение",
                severityLevel = 2,
                confidence = 0.6f,
                estimatedCost = 15000.0,
                description = responseText.take(200),
                repairRecommendations = "Обратитесь к специалисту для точной оценки"
            )
        }
    }
    
    private fun extractJsonValue(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\"|\"$key\"\\s*:\\s*([^,}\\s]+)".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.getOrNull(1) ?: match?.groupValues?.getOrNull(2)
    }
}

data class CarDamageAnalysisResult(
    val damageType: String,
    val severityLevel: Int,
    val confidence: Float,
    val estimatedCost: Double,
    val description: String,
    val repairRecommendations: String
)