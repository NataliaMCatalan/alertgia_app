package com.alertgia.app.data.repository

import android.util.Base64
import com.alertgia.app.BuildConfig
import com.alertgia.app.data.remote.AnthropicApiService
import com.alertgia.app.data.remote.dto.ClaudeRequest
import com.alertgia.app.data.remote.dto.ContentBlock
import com.alertgia.app.data.remote.dto.ImageSource
import com.alertgia.app.data.remote.dto.Message
import com.alertgia.app.domain.model.AllergenResult
import com.alertgia.app.domain.model.Allergy
import com.alertgia.app.domain.model.BoundingBox
import com.alertgia.app.domain.model.DetectedFoodItem
import com.alertgia.app.domain.model.SafetyLevel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineAnalysisStrategy @Inject constructor(
    private val apiService: AnthropicApiService
) : ImageAnalysisStrategy {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyze(
        imageBytes: ByteArray,
        allergies: List<Allergy>,
        confidenceThreshold: Float
    ): Result<AllergenResult> {
        return try {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val allergyList = allergies.joinToString(", ") {
                "${it.name} (${it.severity.displayName})"
            }

            val prompt = buildString {
                append("You are an allergen detection assistant. The user has the following allergies: ")
                append(allergyList)
                append(".\n\n")
                append("Analyze this image of a food item or ingredient label. ")
                append("Identify ALL ingredients visible or likely present.\n\n")
                append("Respond ONLY with this exact JSON format, no other text:\n")
                append("{\n")
                append("  \"safety_level\": \"SAFE\" or \"WARNING\" or \"DANGER\",\n")
                append("  \"detected_allergens\": [\"list of allergens found that match user's allergies\"],\n")
                append("  \"all_ingredients_identified\": [\"list of all ingredients you can identify\"],\n")
                append("  \"confidence\": \"HIGH\" or \"MEDIUM\" or \"LOW\",\n")
                append("  \"explanation\": \"Brief explanation of your assessment\",\n")
                append("  \"recommendations\": \"Any recommendations for the user\",\n")
                append("  \"detected_food_items\": [\n")
                append("    {\n")
                append("      \"name\": \"food name\",\n")
                append("      \"bounding_box\": {\"left\": 0.0, \"top\": 0.0, \"right\": 1.0, \"bottom\": 1.0},\n")
                append("      \"allergens_present\": [\"allergen names from user's list\"]\n")
                append("    }\n")
                append("  ]\n")
                append("}\n\n")
                append("For bounding_box, estimate approximate normalized coordinates (0-1) where (0,0) is top-left of the image.")
            }

            val request = ClaudeRequest(
                messages = listOf(
                    Message(
                        content = listOf(
                            ContentBlock(
                                type = "image",
                                source = ImageSource(data = base64Image)
                            ),
                            ContentBlock(
                                type = "text",
                                text = prompt
                            )
                        )
                    )
                )
            )

            val response = apiService.analyzeImage(
                apiKey = BuildConfig.ANTHROPIC_API_KEY,
                request = request
            )

            val responseText = response.content.firstOrNull()?.text
                ?: return Result.failure(Exception("Empty response from API"))

            parseAnalysisResponse(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAnalysisResponse(responseText: String): Result<AllergenResult> {
        return try {
            val jsonText = responseText
                .substringAfter("{")
                .substringBeforeLast("}")
                .let { "{$it}" }

            val jsonObject = json.decodeFromString<JsonObject>(jsonText)

            val safetyLevel = SafetyLevel.fromString(
                jsonObject["safety_level"]?.jsonPrimitive?.content ?: "WARNING"
            )
            val detectedAllergens = jsonObject["detected_allergens"]
                ?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            val allIngredients = jsonObject["all_ingredients_identified"]
                ?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            val confidence = jsonObject["confidence"]?.jsonPrimitive?.content ?: "MEDIUM"
            val explanation = jsonObject["explanation"]?.jsonPrimitive?.content ?: ""
            val recommendations = jsonObject["recommendations"]?.jsonPrimitive?.content ?: ""

            // Parse positioned food items (best-effort)
            val detectedItems = try {
                jsonObject["detected_food_items"]?.jsonArray?.map { itemEl ->
                    val item = itemEl.jsonObject
                    val bbox = item["bounding_box"]?.jsonObject
                    DetectedFoodItem(
                        foodName = item["name"]?.jsonPrimitive?.content ?: "",
                        confidence = 0.8f,
                        boundingBox = BoundingBox(
                            left = bbox?.get("left")?.jsonPrimitive?.float ?: 0f,
                            top = bbox?.get("top")?.jsonPrimitive?.float ?: 0f,
                            right = bbox?.get("right")?.jsonPrimitive?.float ?: 1f,
                            bottom = bbox?.get("bottom")?.jsonPrimitive?.float ?: 1f
                        ),
                        allergens = item["allergens_present"]?.jsonArray
                            ?.map { it.jsonPrimitive.content }?.toSet() ?: emptySet()
                    )
                } ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            Result.success(
                AllergenResult(
                    safetyLevel = safetyLevel,
                    detectedAllergens = detectedAllergens,
                    allIngredients = allIngredients,
                    confidence = confidence,
                    explanation = explanation,
                    recommendations = recommendations,
                    detectedItems = detectedItems
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse analysis response: ${e.message}"))
        }
    }
}
