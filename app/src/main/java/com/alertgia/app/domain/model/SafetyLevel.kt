package com.alertgia.app.domain.model

import androidx.compose.ui.graphics.Color

enum class SafetyLevel(val displayName: String, val color: Color) {
    SAFE("Safe", Color(0xFF4CAF50)),
    WARNING("Warning", Color(0xFFFFC107)),
    DANGER("Danger", Color(0xFFF44336));

    companion object {
        fun fromString(value: String): SafetyLevel {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: WARNING
        }
    }
}
