package com.cardamageai.feature.damageanalysis

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.cardamageai.core.network.ClaudeRepository
import com.cardamageai.core.common.result.Result
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class DamageAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val claudeRepository: ClaudeRepository
) {
    
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )
    
    private val imageLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    suspend fun analyzeImage(imageUri: String, apiKey: String? = null): DamageAnalysis {
        val bitmap = BitmapFactory.decodeFile(imageUri)
        
        // Пробуем использовать Claude API, если есть API ключ
        if (!apiKey.isNullOrEmpty()) {
            return analyzeWithClaude(bitmap, apiKey)
        }
        
        // Fallback на ML Kit если нет API ключа
        return analyzeWithMLKit(bitmap)
    }
    
    private suspend fun analyzeWithClaude(bitmap: android.graphics.Bitmap, apiKey: String): DamageAnalysis {
        return try {
            when (val result = claudeRepository.analyzeCarDamage(bitmap, apiKey)) {
                is Result.Success -> {
                    val claudeResult = result.data
                    DamageAnalysis(
                        damageType = claudeResult.damageType,
                        severityLevel = claudeResult.severityLevel,
                        confidence = claudeResult.confidence,
                        estimatedCost = claudeResult.estimatedCost,
                        description = claudeResult.description
                    )
                }
                is Result.Error -> {
                    // Fallback на ML Kit при ошибке Claude API
                    analyzeWithMLKit(bitmap)
                }
            }
        } catch (e: Exception) {
            // Fallback на ML Kit при любых проблемах
            analyzeWithMLKit(bitmap)
        }
    }
    
    private suspend fun analyzeWithMLKit(bitmap: android.graphics.Bitmap): DamageAnalysis {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // Анализируем изображение с помощью ML Kit
        val labels = getImageLabels(image)
        val objects = getDetectedObjects(image)
        
        // Анализируем полученные данные для определения повреждений
        return analyzeDamageFromResults(labels, objects)
    }

    private suspend fun getImageLabels(image: InputImage): List<String> {
        return suspendCancellableCoroutine { continuation ->
            imageLabeler.process(image)
                .addOnSuccessListener { labels ->
                    val labelTexts = labels.map { it.text }
                    continuation.resume(labelTexts)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    private suspend fun getDetectedObjects(image: InputImage): List<String> {
        return suspendCancellableCoroutine { continuation ->
            objectDetector.process(image)
                .addOnSuccessListener { objects ->
                    val objectLabels = objects.mapNotNull { detectedObject ->
                        detectedObject.labels.firstOrNull()?.text
                    }
                    continuation.resume(objectLabels)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    private fun analyzeDamageFromResults(
        labels: List<String>,
        objects: List<String>
    ): DamageAnalysis {
        // Простая логика анализа на основе найденных меток
        val allTags = (labels + objects).map { it.lowercase() }
        
        val damageKeywords = mapOf(
            "царапина" to listOf("scratch", "scrape", "mark"),
            "вмятина" to listOf("dent", "bent", "damaged"),
            "трещина" to listOf("crack", "broken", "fracture"),
            "ржавчина" to listOf("rust", "corrosion", "oxidation"),
            "разбитое стекло" to listOf("broken glass", "shattered", "glass"),
            "повреждение краски" to listOf("paint damage", "faded", "peeling")
        )
        
        var detectedDamageType = "Общее повреждение"
        var maxSeverity = 1
        var confidence = 0.3f // Базовая уверенность
        
        for ((damageType, keywords) in damageKeywords) {
            val found = keywords.any { keyword ->
                allTags.any { tag -> tag.contains(keyword) }
            }
            
            if (found) {
                detectedDamageType = damageType
                confidence = 0.8f
                
                // Определяем серьёзность в зависимости от типа повреждения
                maxSeverity = when (damageType) {
                    "царапина" -> 2
                    "вмятина" -> 3
                    "трещина" -> 4
                    "ржавчина" -> 3
                    "разбитое стекло" -> 5
                    "повреждение краски" -> 2
                    else -> 2
                }
                break
            }
        }
        
        // Если найдены ключевые слова автомобиля, повышаем уверенность
        val carKeywords = listOf("car", "vehicle", "auto", "automobile", "bumper", "door", "hood")
        if (carKeywords.any { keyword ->
                allTags.any { tag -> tag.contains(keyword) }
            }) {
            confidence = (confidence + 0.2f).coerceAtMost(1.0f)
        }
        
        val estimatedCost = calculateEstimatedCost(detectedDamageType, maxSeverity)
        
        return DamageAnalysis(
            damageType = detectedDamageType,
            severityLevel = maxSeverity,
            confidence = confidence,
            estimatedCost = estimatedCost,
            description = generateDescription(detectedDamageType, maxSeverity)
        )
    }
    
    private fun calculateEstimatedCost(damageType: String, severity: Int): Double {
        val baseCost = when (damageType.lowercase()) {
            "царапина" -> 5000.0
            "вмятина" -> 15000.0
            "трещина" -> 25000.0
            "ржавчина" -> 20000.0
            "разбитое стекло" -> 30000.0
            "повреждение краски" -> 10000.0
            else -> 10000.0
        }
        
        return baseCost * (severity * 0.5 + 0.5)
    }
    
    private fun generateDescription(damageType: String, severity: Int): String {
        val severityText = when (severity) {
            1 -> "минимальное"
            2 -> "лёгкое"
            3 -> "умеренное"
            4 -> "серьёзное"
            5 -> "критическое"
            else -> "неопределённое"
        }
        
        return "Обнаружено $severityText повреждение типа: $damageType. " +
                "Рекомендуется обратиться к специалисту для точной оценки и ремонта."
    }
}