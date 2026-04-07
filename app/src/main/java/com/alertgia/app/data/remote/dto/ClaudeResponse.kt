package com.alertgia.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClaudeResponse(
    val id: String = "",
    val type: String = "",
    val content: List<ResponseContent> = emptyList()
)

@Serializable
data class ResponseContent(
    val type: String = "",
    val text: String = ""
)
