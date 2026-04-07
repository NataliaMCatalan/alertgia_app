package com.alertgia.app.data.ml

import android.content.Context
import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream

class PytorchClassifier(private val context: Context) {

    private var module: Module? = null
    private var labels: List<String> = emptyList()
    private var isInitialized = false

    private val inputSize = 256 // SigLIP2-256 input size
    private val mean = floatArrayOf(0.5f, 0.5f, 0.5f)
    private val std = floatArrayOf(0.5f, 0.5f, 0.5f)

    fun initialize(): Boolean {
        if (isInitialized) return true
        return try {
            val modelPath = assetToFile("food_sigclip_classifier.pt")
            // Use multiple threads for inference
            org.pytorch.PyTorchAndroid.setNumThreads(4)
            module = Module.load(modelPath)
            labels = context.assets.open("food_ingredients_labels.txt")
                .bufferedReader()
                .readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            isInitialized = true
            true
        } catch (e: Exception) {
            // Model not bundled or incompatible
            isInitialized = false
            false
        }
    }

    fun isAvailable(): Boolean = isInitialized && module != null

    fun getClassCount(): Int = labels.size

    fun classify(bitmap: Bitmap, topK: Int = 5): List<Pair<String, Float>> {
        val mod = module ?: return emptyList()

        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resized, mean, std
        )

        val output = mod.forward(IValue.from(inputTensor)).toTensor()
        val scores = output.dataAsFloatArray

        // Apply softmax
        val maxScore = scores.max() ?: 0f
        val expScores = scores.map { Math.exp((it - maxScore).toDouble()).toFloat() }
        val sumExp = expScores.sum()
        val probs = expScores.map { it / sumExp }

        return probs
            .mapIndexed { index, confidence ->
                if (index < labels.size) labels[index] to confidence
                else "unknown" to confidence
            }
            .sortedByDescending { it.second }
            .take(topK)
    }

    fun close() {
        module?.destroy()
        module = null
        isInitialized = false
    }

    private fun assetToFile(assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath

        context.assets.open(assetName).use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}
