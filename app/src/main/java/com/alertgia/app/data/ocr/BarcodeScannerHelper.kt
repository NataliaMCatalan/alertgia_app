package com.alertgia.app.data.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BarcodeScannerHelper @Inject constructor() {

    private val scanner = BarcodeScanning.getClient()

    suspend fun scanBarcodes(bitmap: Bitmap): List<BarcodeResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { cont ->
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val results = barcodes.mapNotNull { barcode ->
                        val value = barcode.rawValue ?: return@mapNotNull null
                        BarcodeResult(
                            value = value,
                            type = when (barcode.valueType) {
                                Barcode.TYPE_URL -> BarcodeType.URL
                                Barcode.TYPE_TEXT -> BarcodeType.TEXT
                                else -> BarcodeType.OTHER
                            }
                        )
                    }
                    cont.resume(results)
                }
                .addOnFailureListener {
                    cont.resume(emptyList())
                }
        }
    }
}

data class BarcodeResult(
    val value: String,
    val type: BarcodeType
)

enum class BarcodeType { URL, TEXT, OTHER }
