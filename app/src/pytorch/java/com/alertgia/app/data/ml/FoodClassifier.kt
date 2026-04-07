package com.alertgia.app.data.ml

import android.content.Context
import android.graphics.Bitmap

/**
 * PyTorch-only flavor: delegates everything to PytorchClassifier.
 * No TFLite dependency needed.
 */
enum class ModelType(
    val displayName: String,
    val modelFile: String,
    val labelsFile: String,
    val isPytorch: Boolean = false
) {
    SIGCLIP_FOOD(
        "SigLIP2 (Best)",
        "food_sigclip_classifier.pt",
        "food_ingredients_labels.txt",
        isPytorch = true
    );
}

class FoodClassifier(private val context: Context) {

    private var pytorchClassifier: PytorchClassifier? = null
    private var isInitialized = false
    private var currentModel: ModelType? = null

    fun initialize() {
        loadModel(ModelType.SIGCLIP_FOOD)
    }

    fun getAvailableModels(): List<ModelType> {
        return ModelType.entries.filter { modelType ->
            try {
                context.assets.open(modelType.modelFile).close()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun getCurrentModel(): ModelType? = currentModel

    fun switchModel(modelType: ModelType): Boolean {
        if (modelType == currentModel && isInitialized) return true
        close()
        return loadModel(modelType)
    }

    private fun loadModel(modelType: ModelType): Boolean {
        return try {
            val pt = PytorchClassifier(context)
            if (!pt.initialize()) return false
            pytorchClassifier = pt
            currentModel = modelType
            isInitialized = true
            true
        } catch (_: Exception) {
            isInitialized = false
            false
        }
    }

    fun isAvailable(): Boolean = isInitialized

    fun getClassCount(): Int = pytorchClassifier?.getClassCount() ?: 0

    fun classify(bitmap: Bitmap, topK: Int = 5): List<Pair<String, Float>> {
        return pytorchClassifier?.classify(bitmap, topK) ?: emptyList()
    }

    fun close() {
        pytorchClassifier?.close()
        pytorchClassifier = null
        isInitialized = false
        currentModel = null
    }
}
