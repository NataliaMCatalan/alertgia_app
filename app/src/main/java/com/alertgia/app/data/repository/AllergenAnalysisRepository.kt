package com.alertgia.app.data.repository

import com.alertgia.app.data.ml.LocalModelAnalysisStrategy
import com.alertgia.app.domain.model.AllergenResult
import com.alertgia.app.domain.model.Allergy
import com.alertgia.app.domain.model.AnalysisMode
import com.alertgia.app.util.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllergenAnalysisRepository @Inject constructor(
    private val onlineStrategy: OnlineAnalysisStrategy,
    private val localStrategy: LocalModelAnalysisStrategy,
    private val connectivityObserver: ConnectivityObserver
) {
    val isOnline: StateFlow<Boolean> = connectivityObserver.isOnline

    private val _analysisMode = MutableStateFlow(AnalysisMode.OFFLINE)
    val analysisMode: StateFlow<AnalysisMode> = _analysisMode.asStateFlow()

    fun setAnalysisMode(mode: AnalysisMode) {
        _analysisMode.value = mode
    }

    fun isOfflineModelAvailable(): Boolean = localStrategy.isAvailable()

    suspend fun analyzeImage(
        imageBytes: ByteArray,
        allergies: List<Allergy>,
        confidenceThreshold: Float = 0.30f
    ): Result<AllergenResult> {
        val useOnline = when (_analysisMode.value) {
            AnalysisMode.ONLINE -> true
            AnalysisMode.OFFLINE -> false
            AnalysisMode.AUTO -> connectivityObserver.isOnline.value
        }

        if (useOnline) {
            val result = onlineStrategy.analyze(imageBytes, allergies)
            if (result.isSuccess) return result

            // Online failed — try offline fallback if available
            if (localStrategy.isAvailable()) {
                return localStrategy.analyze(imageBytes, allergies, confidenceThreshold)
            }
            return result
        }

        return localStrategy.analyze(imageBytes, allergies, confidenceThreshold)
    }
}
