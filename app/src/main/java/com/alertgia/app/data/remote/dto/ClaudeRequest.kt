package com.alertgia.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaudeRequest(
    val model: String = "claude-sonnet-4-20250514",
    @SerialName("max_tokens")
    val maxTokens: Int = 1024,
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String = "user",
    val content: List<ContentBlock>
)

@Serializable
data class ContentBlock(
    val type: String,
    val source: ImageSource? = null,
    val text: String? = null
)

@Serializable
data class ImageSource(
    val type: String = "base64",
    @SerialName("media_type")
    val mediaType: String = "image/jpeg",
    val data: String
)
