package com.alertgia.app.data.remote

import com.alertgia.app.data.remote.dto.ClaudeRequest
import com.alertgia.app.data.remote.dto.ClaudeResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AnthropicApiService {

    @POST("v1/messages")
    suspend fun analyzeImage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: ClaudeRequest
    ): ClaudeResponse
}
