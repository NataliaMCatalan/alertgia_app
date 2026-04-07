package com.alertgia.app.data.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class ModelType(
    val displayName: String,
    val modelFile: String,
    val labelsFile: String,
    val isPytorch: Boolean = false
) {
    FOOD_101(
        "Dishes (Fast)",
        "food_classifier.tflite",
        "food_labels.txt"
    ),
    FOOD_INGREDIENTS(
        "Dishes + Ingredients",
        "food_ingredients_classifier.tflite",
        "food_ingredients_labels.txt"
    ),
    SIGCLIP_FOOD(
        "SigLIP2 (Best)",
        "food_sigclip_classifier.pt",
        "food_ingredients_labels.txt",
        isPytorch = true
    );
}

class FoodClassifier(private val context: Context) {

    // TFLite backend
    private var interpreter: Interpreter? = null
    private var tfliteLabels: List<String> = emptyList()

    // PyTorch backend
    private var pytorchClassifier: PytorchClassifier? = null

    private var isInitialized = false
    private var currentModel: ModelType? = null

    // TFLite settings (auto-detected)
    private var inputSize = 260
    private val pixelSize = 3
    private val numBytesPerChannel = 4
    private var useEfficientNetNorm = false

    fun initialize() {
        loadModel(ModelType.FOOD_101)
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
            if (modelType.isPytorch) {
                loadPytorchModel(modelType)
            } else {
                loadTfliteModel(modelType)
            }
        } catch (e: Exception) {
            if (modelType != ModelType.FOOD_101) {
                return loadModel(ModelType.FOOD_101)
            }
            isInitialized = false
            false
        }
    }

    private fun loadTfliteModel(modelType: ModelType): Boolean {
        pytorchClassifier?.close()
        pytorchClassifier = null

        val model = FileUtil.loadMappedFile(context, modelType.modelFile)
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(model, options)

        val inputShape = interpreter!!.getInputTensor(0).shape()
        if (inputShape.size >= 3) {
            inputSize = inputShape[1]
        }
        useEfficientNetNorm = inputSize >= 260

        tfliteLabels = FileUtil.loadLabels(context, modelType.labelsFile)
        currentModel = modelType
        isInitialized = true
        return true
    }

    private fun loadPytorchModel(modelType: ModelType): Boolean {
        interpreter?.close()
        interpreter = null

        val ptClassifier = PytorchClassifier(context)
        if (!ptClassifier.initialize()) {
            return false
        }
        pytorchClassifier = ptClassifier
        currentModel = modelType
        isInitialized = true
        return true
    }

    fun isAvailable(): Boolean = isInitialized

    fun getClassCount(): Int {
        return pytorchClassifier?.getClassCount()
            ?: tfliteLabels.size
    }

    fun classify(bitmap: Bitmap, topK: Int = 5): List<Pair<String, Float>> {
        // Delegate to PyTorch if active
        pytorchClassifier?.let { pt ->
            if (pt.isAvailable()) {
                return pt.classify(bitmap, topK)
            }
        }

        // TFLite path
        val interp = interpreter ?: return emptyList()
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = convertBitmapToByteBuffer(resized)
        val output = Array(1) { FloatArray(tfliteLabels.size) }
        interp.run(inputBuffer, output)

        return output[0]
            .mapIndexed { index, confidence -> tfliteLabels[index] to confidence }
            .sortedByDescending { it.second }
            .take(topK)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(
            1 * inputSize * inputSize * pixelSize * numBytesPerChannel
        )
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF).toFloat()
            val g = ((pixel shr 8) and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()
            if (useEfficientNetNorm) {
                byteBuffer.putFloat(r / 127.5f - 1.0f)
                byteBuffer.putFloat(g / 127.5f - 1.0f)
                byteBuffer.putFloat(b / 127.5f - 1.0f)
            } else {
                byteBuffer.putFloat(r / 255.0f)
                byteBuffer.putFloat(g / 255.0f)
                byteBuffer.putFloat(b / 255.0f)
            }
        }

        return byteBuffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        pytorchClassifier?.close()
        pytorchClassifier = null
        isInitialized = false
        currentModel = null
    }
}
