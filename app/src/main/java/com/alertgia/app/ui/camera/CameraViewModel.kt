package com.alertgia.app.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alertgia.app.data.ml.FoodClassifier
import com.alertgia.app.data.ml.ModelType
import com.alertgia.app.data.ml.ObjectDetector
import com.alertgia.app.data.repository.AllergenAnalysisRepository
import com.alertgia.app.data.repository.UserProfileRepository
import com.alertgia.app.domain.model.AllergenResult
import com.alertgia.app.domain.model.AnalysisMode
import com.alertgia.app.domain.model.BoundingBox
import com.alertgia.app.domain.model.SafetyLevel
import com.alertgia.app.domain.model.Severity
import com.alertgia.app.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class DetectedAllergenLabel(
    val id: Int = 0, // stable ID for tracking across frames
    val name: String,
    val safetyLevel: SafetyLevel,
    val boundingBox: BoundingBox? = null,
    val isAllergen: Boolean = true,
    val confidence: Float = 0f
)

data class SeenFoodItem(
    val name: String,
    val isAllergen: Boolean,
    val safetyLevel: SafetyLevel,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CameraMode { LIVE_SCAN, PHOTO_CAPTURE }

data class CameraUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val isScanning: Boolean = false,
    val isAnalyzing: Boolean = false,
    val detectedLabels: List<DetectedAllergenLabel> = emptyList(),
    val allIngredients: List<String> = emptyList(),
    val explanation: String = "",
    val confidence: String = "",
    val overallSafety: SafetyLevel? = null,
    val error: String? = null,
    val scanCount: Int = 0,
    val analysisMode: AnalysisMode = AnalysisMode.OFFLINE,
    val isOnline: Boolean = true,
    val isOfflineModelAvailable: Boolean = false,
    val activeMode: String = "Offline",
    val showAllFoods: Boolean = false,
    val refreshIntervalMs: Long = 200L,
    val confidenceThreshold: Int = 30, // 15–85 percent
    val currentModelName: String = "Dishes (Food-101)",
    val availableModels: List<ModelType> = emptyList(),
    val recentlySeenItems: List<SeenFoodItem> = emptyList(),
    val cameraMode: CameraMode = CameraMode.LIVE_SCAN,
    val snapshotResult: String? = null // non-null when showing photo analysis result
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: UserProfileRepository,
    private val analysisRepository: AllergenAnalysisRepository,
    private val objectDetector: ObjectDetector,
    private val foodClassifier: FoodClassifier
) : ViewModel() {

    private val profileId: Long = savedStateHandle["profileId"] ?: -1L

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val isAnalyzingFrame = AtomicBoolean(false)
    private val isTrackingFrame = AtomicBoolean(false)
    private var lastAnalysisTime = 0L
    private var lastTrackTime = 0L
    private var lastResultTime = 0L
    private var nextTrackingId = 1
    private var previousLabels: List<DetectedAllergenLabel> = emptyList()

    // Full analysis interval (detect + classify + allergen map)
    private val onlineIntervalMs = 3000L
    private var userOfflineIntervalMs = 200L
    // Fast tracking interval (object detector only, ~30ms) — runs between full analyses
    private val trackingIntervalMs = 50L
    // Clear stale results after this many ms with no new detections
    private val staleTimeoutMs = 2000L

    private val analysisIntervalMs: Long
        get() = if (isEffectivelyOnline()) onlineIntervalMs else userOfflineIntervalMs

    init {
        loadProfile()
        observeConnectivity()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = profileRepository.getProfile(profileId)
            if (profile != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = profile,
                        isScanning = true,
                        isOfflineModelAvailable = analysisRepository.isOfflineModelAvailable(),
                        availableModels = foodClassifier.getAvailableModels(),
                        currentModelName = foodClassifier.getCurrentModel()?.displayName ?: "Unknown"
                    )
                }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            analysisRepository.isOnline.collect { online ->
                _uiState.update {
                    it.copy(
                        isOnline = online,
                        activeMode = resolveActiveMode(it.analysisMode, online)
                    )
                }
            }
        }
    }

    fun cycleAnalysisMode() {
        val currentMode = _uiState.value.analysisMode
        val nextMode = when (currentMode) {
            AnalysisMode.AUTO -> AnalysisMode.ONLINE
            AnalysisMode.ONLINE -> AnalysisMode.OFFLINE
            AnalysisMode.OFFLINE -> AnalysisMode.AUTO
        }
        analysisRepository.setAnalysisMode(nextMode)
        _uiState.update {
            it.copy(
                analysisMode = nextMode,
                activeMode = resolveActiveMode(nextMode, it.isOnline)
            )
        }
        // Clear current results when switching mode
        clearResults()
    }

    fun onFrameAvailable(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        val profile = _uiState.value.profile
        if (profile == null) { imageProxy.close(); return }

        val needsFullAnalysis = now - lastAnalysisTime >= analysisIntervalMs
        val needsTracking = !needsFullAnalysis &&
            now - lastTrackTime >= trackingIntervalMs &&
            previousLabels.isNotEmpty() &&
            objectDetector.isAvailable() &&
            !isEffectivelyOnline()

        if (needsFullAnalysis && isAnalyzingFrame.compareAndSet(false, true)) {
            // FULL ANALYSIS: detect + classify + allergen mapping
            val imageBytes = imageProxyToJpeg(imageProxy)
            imageProxy.close()
            if (imageBytes == null) { isAnalyzingFrame.set(false); return }

            viewModelScope.launch {
                _uiState.update { it.copy(isAnalyzing = true) }
                val result = analysisRepository.analyzeImage(imageBytes, profile.allergies, _uiState.value.confidenceThreshold / 100f)

                result.fold(
                    onSuccess = { allergenResult ->
                        val rawLabels = buildLabels(allergenResult, profile)
                        val tracked = trackLabels(rawLabels)
                        previousLabels = tracked
                        lastResultTime = System.currentTimeMillis()
                        _uiState.update {
                            val updatedSeen = updateSeenItems(it.recentlySeenItems, tracked)
                            it.copy(
                                isAnalyzing = false,
                                detectedLabels = tracked,
                                allIngredients = allergenResult.allIngredients,
                                explanation = allergenResult.explanation,
                                confidence = allergenResult.confidence,
                                overallSafety = if (tracked.isEmpty() && allergenResult.detectedItems.isEmpty()) null else allergenResult.safetyLevel,
                                error = null,
                                scanCount = it.scanCount + 1,
                                recentlySeenItems = updatedSeen
                            )
                        }
                    },
                    onFailure = { error ->
                        val timeSinceResult = System.currentTimeMillis() - lastResultTime
                        if (timeSinceResult > staleTimeoutMs) {
                            previousLabels = emptyList()
                            _uiState.update { it.copy(isAnalyzing = false, detectedLabels = emptyList(), overallSafety = null, error = error.message) }
                        } else {
                            _uiState.update { it.copy(isAnalyzing = false, error = error.message) }
                        }
                    }
                )
                lastAnalysisTime = System.currentTimeMillis()
                isAnalyzingFrame.set(false)
            }
        } else if (needsTracking && isTrackingFrame.compareAndSet(false, true)) {
            // FAST TRACKING: object detector only to update positions (~30ms)
            val bitmap = imageProxyToBitmap(imageProxy)
            imageProxy.close()
            if (bitmap == null) { isTrackingFrame.set(false); return }

            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val detections = objectDetector.detect(bitmap, scoreThreshold = 0.25f)
                    if (detections.isNotEmpty()) {
                        // Update bounding box positions of existing labels
                        val updatedLabels = previousLabels.map { label ->
                            val box = label.boundingBox ?: return@map label
                            // Find closest detection by IoU
                            val match = detections.maxByOrNull { computeIoU(box, it.boundingBox) }
                            if (match != null && computeIoU(box, match.boundingBox) > 0.2f) {
                                val smoothed = BoundingBox(
                                    left = box.left * 0.3f + match.boundingBox.left * 0.7f,
                                    top = box.top * 0.3f + match.boundingBox.top * 0.7f,
                                    right = box.right * 0.3f + match.boundingBox.right * 0.7f,
                                    bottom = box.bottom * 0.3f + match.boundingBox.bottom * 0.7f
                                )
                                label.copy(boundingBox = smoothed)
                            } else {
                                label
                            }
                        }
                        previousLabels = updatedLabels
                        _uiState.update { it.copy(detectedLabels = updatedLabels) }
                    }
                } catch (_: Exception) {}
                lastTrackTime = System.currentTimeMillis()
                isTrackingFrame.set(false)
            }
        } else {
            imageProxy.close()
        }
    }

    /**
     * Capture a single photo, analyze it fully, and show results.
     */
    fun captureAndAnalyze(imageBytes: ByteArray) {
        val profile = _uiState.value.profile ?: return
        _uiState.update { it.copy(isAnalyzing = true, snapshotResult = null) }

        viewModelScope.launch {
            val result = analysisRepository.analyzeImage(imageBytes, profile.allergies, _uiState.value.confidenceThreshold / 100f)
            result.fold(
                onSuccess = { allergenResult ->
                    val rawLabels = buildLabels(allergenResult, profile)
                    val tracked = trackLabels(rawLabels)
                    previousLabels = tracked
                    val summary = buildString {
                        append(allergenResult.safetyLevel.displayName.uppercase())
                        if (allergenResult.detectedAllergens.isNotEmpty()) {
                            append(" — ")
                            append(allergenResult.detectedAllergens.joinToString(", "))
                        }
                        append("\n")
                        append(allergenResult.explanation)
                    }
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            detectedLabels = tracked,
                            overallSafety = allergenResult.safetyLevel,
                            snapshotResult = summary,
                            allIngredients = allergenResult.allIngredients,
                            explanation = allergenResult.explanation,
                            confidence = allergenResult.confidence,
                            recentlySeenItems = updateSeenItems(it.recentlySeenItems, tracked)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isAnalyzing = false, snapshotResult = "Error: ${error.message}") }
                }
            )
        }
    }

    fun dismissSnapshot() {
        _uiState.update { it.copy(snapshotResult = null) }
    }

    fun setCameraMode(mode: CameraMode) {
        _uiState.update { it.copy(cameraMode = mode, snapshotResult = null) }
    }

    fun toggleScanning() {
        _uiState.update { it.copy(isScanning = !it.isScanning) }
    }

    fun toggleShowAllFoods() {
        _uiState.update { it.copy(showAllFoods = !it.showAllFoods) }
    }

    fun setRefreshInterval(ms: Long) {
        val clamped = ms.coerceIn(50L, 2000L)
        userOfflineIntervalMs = clamped
        _uiState.update { it.copy(refreshIntervalMs = clamped) }
    }

    fun switchModel(modelType: ModelType) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val success = foodClassifier.switchModel(modelType)
            if (success) {
                clearResults()
                _uiState.update {
                    it.copy(
                        currentModelName = foodClassifier.getCurrentModel()?.displayName ?: "Unknown",
                        availableModels = foodClassifier.getAvailableModels()
                    )
                }
            }
        }
    }

    fun setConfidenceThreshold(percent: Int) {
        val clamped = percent.coerceIn(15, 85)
        _uiState.update { it.copy(confidenceThreshold = clamped) }
    }

    fun clearResults() {
        previousLabels = emptyList()
        _uiState.update {
            it.copy(
                detectedLabels = emptyList(),
                allIngredients = emptyList(),
                explanation = "",
                confidence = "",
                overallSafety = null,
                error = null
            )
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val yBuffer = imageProxy.planes[0].buffer
            val uBuffer = imageProxy.planes[1].buffer
            val vBuffer = imageProxy.planes[2].buffer
            val nv21 = ByteArray(yBuffer.remaining() + uBuffer.remaining() + vBuffer.remaining())
            yBuffer.get(nv21, 0, yBuffer.remaining())
            vBuffer.get(nv21, yBuffer.remaining(), vBuffer.remaining())
            uBuffer.get(nv21, yBuffer.remaining() + vBuffer.remaining(), uBuffer.remaining())

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 50, out)
            var bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size()) ?: return null

            val rotation = imageProxy.imageInfo.rotationDegrees
            if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
            // Smaller for fast tracking
            val maxDim = 400
            if (bitmap.width > maxDim || bitmap.height > maxDim) {
                val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
                bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
            }
            bitmap
        } catch (_: Exception) { null }
    }

    /**
     * Match new labels to previous ones using IoU (Intersection over Union).
     * Reuses IDs for smooth animation, assigns new IDs for new detections.
     */
    private fun trackLabels(newLabels: List<DetectedAllergenLabel>): List<DetectedAllergenLabel> {
        if (previousLabels.isEmpty()) {
            return newLabels.map { it.copy(id = nextTrackingId++) }
        }

        val matched = mutableSetOf<Int>() // indices in previousLabels already matched
        return newLabels.map { newLabel ->
            val newBox = newLabel.boundingBox ?: return@map newLabel.copy(id = nextTrackingId++)

            // Find best matching previous label by IoU
            var bestIdx = -1
            var bestIou = 0.3f // minimum IoU threshold
            previousLabels.forEachIndexed { idx, prev ->
                if (idx !in matched && prev.boundingBox != null) {
                    val iou = computeIoU(newBox, prev.boundingBox)
                    if (iou > bestIou) {
                        bestIou = iou
                        bestIdx = idx
                    }
                }
            }

            if (bestIdx >= 0) {
                matched.add(bestIdx)
                // Smooth: blend previous box with new box (70% new, 30% previous)
                val prev = previousLabels[bestIdx].boundingBox!!
                val smoothed = BoundingBox(
                    left = prev.left * 0.3f + newBox.left * 0.7f,
                    top = prev.top * 0.3f + newBox.top * 0.7f,
                    right = prev.right * 0.3f + newBox.right * 0.7f,
                    bottom = prev.bottom * 0.3f + newBox.bottom * 0.7f
                )
                newLabel.copy(id = previousLabels[bestIdx].id, boundingBox = smoothed)
            } else {
                newLabel.copy(id = nextTrackingId++)
            }
        }
    }

    /**
     * Maintain a list of recently seen food items. Add new ones, keep for 30s, deduplicate.
     */
    private fun updateSeenItems(
        existing: List<SeenFoodItem>,
        currentLabels: List<DetectedAllergenLabel>
    ): List<SeenFoodItem> {
        val now = System.currentTimeMillis()
        val maxAge = 30_000L // keep items for 30 seconds

        // Start with existing items that aren't stale
        val kept = existing.filter { now - it.timestamp < maxAge }.toMutableList()

        // Add currently visible items (update timestamp if already in list)
        currentLabels.forEach { label ->
            val existingIdx = kept.indexOfFirst { it.name.equals(label.name, ignoreCase = true) }
            if (existingIdx >= 0) {
                kept[existingIdx] = kept[existingIdx].copy(timestamp = now)
            } else {
                kept.add(
                    SeenFoodItem(
                        name = label.name,
                        isAllergen = label.isAllergen,
                        safetyLevel = label.safetyLevel,
                        timestamp = now
                    )
                )
            }
        }

        // Cap at 20 items, most recent first
        return kept.sortedByDescending { it.timestamp }.take(20)
    }

    private fun computeIoU(a: BoundingBox, b: BoundingBox): Float {
        val interLeft = maxOf(a.left, b.left)
        val interTop = maxOf(a.top, b.top)
        val interRight = minOf(a.right, b.right)
        val interBottom = minOf(a.bottom, b.bottom)

        if (interRight <= interLeft || interBottom <= interTop) return 0f

        val interArea = (interRight - interLeft) * (interBottom - interTop)
        val aArea = (a.right - a.left) * (a.bottom - a.top)
        val bArea = (b.right - b.left) * (b.bottom - b.top)
        val unionArea = aArea + bArea - interArea

        return if (unionArea > 0) interArea / unionArea else 0f
    }

    private fun isEffectivelyOnline(): Boolean {
        val mode = _uiState.value.analysisMode
        return when (mode) {
            AnalysisMode.ONLINE -> true
            AnalysisMode.OFFLINE -> false
            AnalysisMode.AUTO -> _uiState.value.isOnline
        }
    }

    private fun resolveActiveMode(mode: AnalysisMode, online: Boolean): String {
        return when (mode) {
            AnalysisMode.ONLINE -> "Online"
            AnalysisMode.OFFLINE -> "Offline"
            AnalysisMode.AUTO -> if (online) "Auto (Online)" else "Auto (Offline)"
        }
    }

    private fun buildLabels(result: AllergenResult, profile: UserProfile): List<DetectedAllergenLabel> {
        val showAll = _uiState.value.showAllFoods

        // Use positioned items if available (offline detection mode)
        if (result.detectedItems.isNotEmpty()) {
            return result.detectedItems.mapNotNull { item ->
                val matchedAllergens = item.allergens.filter { allergen ->
                    profile.allergies.any { it.name.equals(allergen, ignoreCase = true) }
                }
                val isAllergen = matchedAllergens.isNotEmpty()

                if (!showAll && !isAllergen) return@mapNotNull null

                val safetyLevel = when {
                    !isAllergen -> SafetyLevel.SAFE
                    matchedAllergens.any { a ->
                        profile.allergies.any {
                            it.name.equals(a, ignoreCase = true) && it.severity == Severity.SEVERE
                        }
                    } -> SafetyLevel.DANGER
                    else -> SafetyLevel.WARNING
                }

                val displayName = if (isAllergen) {
                    "${item.foodName} (${matchedAllergens.joinToString()})"
                } else {
                    item.foodName
                }

                DetectedAllergenLabel(
                    name = displayName,
                    safetyLevel = safetyLevel,
                    boundingBox = item.boundingBox,
                    isAllergen = isAllergen,
                    confidence = item.confidence
                )
            }
        }

        // Fallback: flat allergen list (online mode or no detections)
        if (result.detectedAllergens.isEmpty() && !showAll) return emptyList()

        val labels = mutableListOf<DetectedAllergenLabel>()

        // Add allergen labels
        result.detectedAllergens.forEach { allergenName ->
            val severity = profile.allergies
                .find { it.name.equals(allergenName, ignoreCase = true) }
                ?.severity

            val safetyLevel = when {
                severity?.name == "SEVERE" -> SafetyLevel.DANGER
                severity?.name == "MODERATE" -> SafetyLevel.WARNING
                severity?.name == "MILD" -> SafetyLevel.WARNING
                else -> SafetyLevel.DANGER
            }
            labels.add(DetectedAllergenLabel(name = allergenName, safetyLevel = safetyLevel, isAllergen = true, confidence = 0.8f))
        }

        // In show-all mode, add non-allergen ingredients too
        if (showAll) {
            val allergenNames = result.detectedAllergens.map { it.lowercase() }.toSet()
            result.allIngredients
                .filter { it.lowercase() !in allergenNames }
                .forEach { ingredient ->
                    labels.add(DetectedAllergenLabel(name = ingredient, safetyLevel = SafetyLevel.SAFE, isAllergen = false, confidence = 0.5f))
                }
        }

        return labels
    }

    private fun imageProxyToJpeg(imageProxy: ImageProxy): ByteArray? {
        return try {
            val yBuffer = imageProxy.planes[0].buffer
            val uBuffer = imageProxy.planes[1].buffer
            val vBuffer = imageProxy.planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(
                nv21,
                ImageFormat.NV21,
                imageProxy.width,
                imageProxy.height,
                null
            )

            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                Rect(0, 0, imageProxy.width, imageProxy.height),
                70,
                out
            )
            val jpegBytes = out.toByteArray()

            var bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                ?: return null

            val rotation = imageProxy.imageInfo.rotationDegrees
            if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            // 300px covers COCO detector (300px) and SigLIP2 (256px)
            val maxDim = 300
            if (bitmap.width > maxDim || bitmap.height > maxDim) {
                val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            }

            val finalOut = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, finalOut)
            finalOut.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
