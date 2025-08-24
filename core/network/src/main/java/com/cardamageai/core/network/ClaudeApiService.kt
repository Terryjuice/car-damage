package com.cardamageai.core.network

import retrofit2.http.*

interface ClaudeApiService {
    
    @POST("v1/messages")
    suspend fun analyzeImage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: ClaudeMessageRequest
    ): ClaudeMessageResponse
}

data class ClaudeMessageRequest(
    val model: String = "claude-3-sonnet-20241022",
    val max_tokens: Int = 1000,
    val messages: List<ClaudeMessage>
)

data class ClaudeMessage(
    val role: String,
    val content: List<ClaudeContent>
)

sealed class ClaudeContent {
    data class Text(
        val type: String = "text",
        val text: String
    ) : ClaudeContent()
    
    data class Image(
        val type: String = "image",
        val source: ClaudeImageSource
    ) : ClaudeContent()
}

data class ClaudeImageSource(
    val type: String = "base64",
    val media_type: String = "image/jpeg",
    val data: String
)

data class ClaudeMessageResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ClaudeResponseContent>,
    val model: String,
    val stop_reason: String?,
    val usage: ClaudeUsage?
)

data class ClaudeResponseContent(
    val type: String,
    val text: String
)

data class ClaudeUsage(
    val input_tokens: Int,
    val output_tokens: Int
)