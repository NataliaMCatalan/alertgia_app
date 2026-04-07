package com.alertgia.app.data.ml

import android.content.Context
import android.graphics.Bitmap
import com.alertgia.app.domain.model.BoundingBox
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ObjectDetector(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var isInitialized = false

    private val inputSize = 300 // SSD MobileNet uses 300x300
    private val maxDetections = 10

    // COCO class indices that are food-related (0-indexed after background)
    private val foodClassIndices = setOf(
        46, 47, 48, 49, 50, 51, 52, 53, 54, 55 // banana..cake
    )

    // Container/context objects that likely hold food
    private val foodContextIndices = setOf(
        39, 40, 41, 42, 43, 44, 45, 60 // bottle, wine glass, cup, fork, knife, spoon, bowl, dining table
    )

    // Non-food classes to always skip (people, vehicles, furniture, etc.)
    private val skipClassIndices = setOf(
        0, // person
        1, 2, 3, 4, 5, 6, 7, 8, // vehicles
        9, 10, 11, 12, 13, // traffic/street objects
        14, 15, 16, 17, 18, 19, 20, 21, 22, 23, // animals
        24, 25, 26, 27, 28, // accessories
        29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // sports
        56, 57, 58, 59, // chair, couch, plant, bed
        61, 62, 63, 64, 65, 66, 67, 68, // electronics
        73, 74, 75, 76, 77, 78, 79 // household items
    )

    data class Detection(
        val boundingBox: BoundingBox,
        val classId: Int,
        val className: String,
        val score: Float
    )

    fun initialize() {
        if (isInitialized) return
        try {
            val model = FileUtil.loadMappedFile(context, "coco_ssd_mobilenet.tflite")
            val options = Interpreter.Options().apply { setNumThreads(4) }
            interpreter = Interpreter(model, options)
            labels = FileUtil.loadLabels(context, "coco_labels.txt")
            isInitialized = true
        } catch (e: Exception) {
            isInitialized = false
        }
    }

    fun isAvailable(): Boolean = isInitialized && interpreter != null

    fun detect(bitmap: Bitmap, scoreThreshold: Float = 0.35f): List<Detection> {
        val interp = interpreter ?: return emptyList()

        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = convertBitmapToByteBuffer(resized)

        // SSD MobileNet v1 quantized output tensors
        val boxes = Array(1) { Array(maxDetections) { FloatArray(4) } }     // [1][10][4]
        val classes = Array(1) { FloatArray(maxDetections) }                 // [1][10]
        val scores = Array(1) { FloatArray(maxDetections) }                  // [1][10]
        val count = FloatArray(1)                                            // [1]

        val outputs = mapOf(
            0 to boxes,
            1 to classes,
            2 to scores,
            3 to count
        )

        interp.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

        val numDetections = count[0].toInt().coerceAtMost(maxDetections)
        val results = mutableListOf<Detection>()

        for (i in 0 until numDetections) {
            val score = scores[0][i]
            if (score < scoreThreshold) continue

            val classId = classes[0][i].toInt()
            // Skip classes that are definitely not food; accept everything else
            // so the food classifier can decide
            if (classId in skipClassIndices) continue

            val className = if (classId in labels.indices) labels[classId] else "food"

            // SSD output is [top, left, bottom, right]
            val top = boxes[0][i][0].coerceIn(0f, 1f)
            val left = boxes[0][i][1].coerceIn(0f, 1f)
            val bottom = boxes[0][i][2].coerceIn(0f, 1f)
            val right = boxes[0][i][3].coerceIn(0f, 1f)

            results.add(
                Detection(
                    boundingBox = BoundingBox(left, top, right, bottom),
                    classId = classId,
                    className = className,
                    score = score
                )
            )
        }

        return results
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            // Quantized model expects uint8 values
            byteBuffer.put(((pixel shr 16) and 0xFF).toByte())
            byteBuffer.put(((pixel shr 8) and 0xFF).toByte())
            byteBuffer.put((pixel and 0xFF).toByte())
        }

        return byteBuffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        isInitialized = false
    }
}
