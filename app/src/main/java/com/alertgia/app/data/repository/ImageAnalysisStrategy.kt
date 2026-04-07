package com.alertgia.app.data.repository

import com.alertgia.app.domain.model.AllergenResult
import com.alertgia.app.domain.model.Allergy

interface ImageAnalysisStrategy {
    suspend fun analyze(
        imageBytes: ByteArray,
        allergies: List<Allergy>,
        confidenceThreshold: Float = 0.30f
    ): Result<AllergenResult>
}
