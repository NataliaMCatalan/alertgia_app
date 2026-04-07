package com.alertgia.app.data.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TextRecognitionHelper @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    cont.resume(result.text)
                }
                .addOnFailureListener {
                    cont.resume("")
                }
        }
    }

    fun analyzeMenuText(
        text: String,
        allergens: List<String>,
        restrictedIngredients: Set<String>
    ): MenuAnalysisResult {
        val lowerText = text.lowercase()
        val foundAllergens = allergens.filter { allergen ->
            lowerText.contains(allergen.lowercase())
        }
        val foundRestricted = restrictedIngredients.filter { ingredient ->
            lowerText.contains(ingredient.lowercase())
        }
        return MenuAnalysisResult(
            fullText = text,
            detectedAllergens = foundAllergens,
            detectedRestricted = foundRestricted.toList(),
            isSafe = foundAllergens.isEmpty() && foundRestricted.isEmpty()
        )
    }
}

data class MenuAnalysisResult(
    val fullText: String,
    val detectedAllergens: List<String>,
    val detectedRestricted: List<String>,
    val isSafe: Boolean
)
