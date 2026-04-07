package com.alertgia.app.data.ml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.alertgia.app.data.repository.ImageAnalysisStrategy
import com.alertgia.app.domain.model.AllergenResult
import com.alertgia.app.domain.model.Allergy
import com.alertgia.app.domain.model.BoundingBox
import com.alertgia.app.domain.model.DetectedFoodItem
import com.alertgia.app.domain.model.SafetyLevel
import com.alertgia.app.domain.model.Severity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalModelAnalysisStrategy @Inject constructor(
    private val foodClassifier: FoodClassifier,
    private val objectDetector: ObjectDetector
) : ImageAnalysisStrategy {

    fun isAvailable(): Boolean = foodClassifier.isAvailable()

    override suspend fun analyze(
        imageBytes: ByteArray,
        allergies: List<Allergy>,
        confidenceThreshold: Float
    ): Result<AllergenResult> = withContext(Dispatchers.Default) {
        try {
            if (!foodClassifier.isAvailable()) {
                return@withContext Result.failure(
                    Exception("Offline model not available. Please connect to the internet.")
                )
            }

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return@withContext Result.failure(Exception("Failed to decode image"))

            val detectedItems = if (objectDetector.isAvailable()) {
                detectAndClassify(bitmap, allergies, confidenceThreshold)
            } else {
                classifyFullFrame(bitmap, allergies, confidenceThreshold)
            }

            val userAllergenNames = allergies.map { it.name.lowercase() }.toSet()
            val matchedAllergens = detectedItems
                .flatMap { it.allergens }
                .filter { it.lowercase() in userAllergenNames }
                .distinct()

            val allFoods = detectedItems.map { it.foodName }

            val safetyLevel = when {
                matchedAllergens.isEmpty() -> SafetyLevel.SAFE
                matchedAllergens.any { allergen ->
                    allergies.any {
                        it.name.equals(allergen, ignoreCase = true) && it.severity == Severity.SEVERE
                    }
                } -> SafetyLevel.DANGER
                matchedAllergens.isNotEmpty() -> SafetyLevel.WARNING
                else -> SafetyLevel.SAFE
            }

            val topConfidence = detectedItems.maxOfOrNull { it.confidence } ?: 0f
            val confidenceStr = when {
                topConfidence > 0.7f -> "HIGH"
                topConfidence > 0.4f -> "MEDIUM"
                else -> "LOW"
            }

            val explanation = if (detectedItems.isNotEmpty()) {
                "Offline: Detected ${allFoods.joinToString(", ")}."
            } else {
                "Offline: Could not identify food items."
            }

            Result.success(
                AllergenResult(
                    safetyLevel = safetyLevel,
                    detectedAllergens = matchedAllergens,
                    allIngredients = allFoods,
                    confidence = confidenceStr,
                    explanation = explanation,
                    recommendations = when {
                        confidenceStr == "LOW" -> "Low confidence. Verify ingredients manually."
                        matchedAllergens.isNotEmpty() -> "Potential allergens found. Check actual ingredients."
                        else -> ""
                    },
                    detectedItems = detectedItems
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun detectAndClassify(bitmap: Bitmap, allergies: List<Allergy>, confidenceThreshold: Float): List<DetectedFoodItem> {
        val detections = objectDetector.detect(bitmap, scoreThreshold = 0.3f)

        if (detections.isEmpty()) {
            return classifyFullFrame(bitmap, allergies, confidenceThreshold)
        }

        val items = detections.mapNotNull { det ->
            val crop = cropBitmap(bitmap, det.boundingBox) ?: return@mapNotNull null
            val predictions = foodClassifier.classify(crop, topK = 3)
            val topPrediction = predictions.firstOrNull() ?: return@mapNotNull null

            // If the food classifier is not confident enough, skip
            if (topPrediction.second < confidenceThreshold) return@mapNotNull null

            val foodName = topPrediction.first.replace("_", " ")
            val allergens = FoodToAllergenMapper.getPotentialAllergens(topPrediction.first)

            DetectedFoodItem(
                foodName = foodName,
                confidence = topPrediction.second,
                boundingBox = det.boundingBox,
                allergens = allergens
            )
        }

        // If object detector found things but classifier rejected them all, try full frame
        return items.ifEmpty { classifyFullFrame(bitmap, allergies, confidenceThreshold) }
    }

    private fun classifyFullFrame(bitmap: Bitmap, allergies: List<Allergy>, confidenceThreshold: Float): List<DetectedFoodItem> {
        val predictions = foodClassifier.classify(bitmap, topK = 5)
        val topPrediction = predictions.firstOrNull() ?: return emptyList()

        if (topPrediction.second < confidenceThreshold) return emptyList()

        val foodName = topPrediction.first.replace("_", " ")
        val allergens = FoodToAllergenMapper.getPotentialAllergens(topPrediction.first)

        return listOf(
            DetectedFoodItem(
                foodName = foodName,
                confidence = topPrediction.second,
                boundingBox = BoundingBox(0.15f, 0.15f, 0.85f, 0.85f),
                allergens = allergens
            )
        )
    }

    private fun cropBitmap(bitmap: Bitmap, box: BoundingBox): Bitmap? {
        val x = (box.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val y = (box.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val w = ((box.right - box.left) * bitmap.width).toInt().coerceIn(1, bitmap.width - x)
        val h = ((box.bottom - box.top) * bitmap.height).toInt().coerceIn(1, bitmap.height - y)
        return try {
            Bitmap.createBitmap(bitmap, x, y, w, h)
        } catch (e: Exception) {
            null
        }
    }
}
