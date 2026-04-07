package com.alertgia.app.domain.model

data class BoundingBox(
    val left: Float,   // normalized 0..1
    val top: Float,    // normalized 0..1
    val right: Float,  // normalized 0..1
    val bottom: Float  // normalized 0..1
) {
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

data class DetectedFoodItem(
    val foodName: String,
    val confidence: Float,
    val boundingBox: BoundingBox,
    val allergens: Set<String>
)

data class AllergenResult(
    val safetyLevel: SafetyLevel,
    val detectedAllergens: List<String>,
    val allIngredients: List<String>,
    val confidence: String,
    val explanation: String,
    val recommendations: String,
    val detectedItems: List<DetectedFoodItem> = emptyList()
)
