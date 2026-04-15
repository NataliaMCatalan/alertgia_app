package com.alertgia.app.ui.scan

import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alertgia.app.data.ocr.BarcodeScannerHelper
import com.alertgia.app.data.ocr.MenuAnalysisResult
import com.alertgia.app.data.ocr.TextRecognitionHelper
import com.alertgia.app.ui.theme.DangerRed
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.SafeGreen
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MenuScannerScreen(
    onNavigateBack: () -> Unit,
    allergens: List<String>,
    restrictedIngredients: Set<String>
) {
    val isSpanish = LocalAppLanguage.current == "es"
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val textHelper = remember { TextRecognitionHelper() }
    val barcodeHelper = remember { BarcodeScannerHelper() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var isCapturing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<MenuAnalysisResult?>(null) }
    var qrResult by remember { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(true) }

    val imageCapture = remember { ImageCapture.Builder().build() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSpanish) "Escanear Menú" else "Scan Menu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showCamera) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                val future = ProcessCameraProvider.getInstance(ctx)
                                future.addListener({
                                    val provider = future.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    try {
                                        provider.unbindAll()
                                        provider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageCapture
                                        )
                                    } catch (_: Exception) {}
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isCapturing) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    // Capture buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // OCR capture
                        IconButton(
                            onClick = {
                                isCapturing = true
                                imageCapture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val buffer: ByteBuffer = image.planes[0].buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)
                                            image.close()
                                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            if (bitmap != null) {
                                                scope.launch {
                                                    val text = textHelper.recognizeText(bitmap)
                                                    val barcodes = barcodeHelper.scanBarcodes(bitmap)

                                                    if (barcodes.isNotEmpty()) {
                                                        qrResult = barcodes.first().value
                                                    }

                                                    analysisResult = textHelper.analyzeMenuText(
                                                        text, allergens, restrictedIngredients
                                                    )
                                                    showCamera = false
                                                    isCapturing = false
                                                }
                                            } else {
                                                isCapturing = false
                                            }
                                        }
                                        override fun onError(e: ImageCaptureException) {
                                            isCapturing = false
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Scan text", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    // Hint
                    Text(
                        if (isSpanish) "Apunta al menú o código QR" else "Point at menu or QR code",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Results view
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // QR result
                    qrResult?.let { url ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isSpanish) "QR Detectado" else "QR Detected", fontWeight = FontWeight.Bold)
                                    Text(url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    analysisResult?.let { result ->
                        // Safety card
                        val isSafe = result.isSafe
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSafe) SafeGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isSafe) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isSafe) SafeGreen else DangerRed,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    if (isSafe) {
                                        if (isSpanish) "No se detectaron alérgenos en el menú" else "No allergens detected in menu"
                                    } else {
                                        if (isSpanish) "Se encontraron posibles alérgenos" else "Potential allergens found"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSafe) SafeGreen else DangerRed
                                )
                            }
                        }

                        // Detected allergens
                        if (result.detectedAllergens.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(if (isSpanish) "Alérgenos Detectados" else "Detected Allergens", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                result.detectedAllergens.forEach {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(it) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DangerRed.copy(alpha = 0.2f), labelColor = DangerRed)
                                    )
                                }
                            }
                        }

                        // Detected dietary restrictions
                        if (result.detectedRestricted.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(if (isSpanish) "Restricciones Dietéticas" else "Dietary Restrictions", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                result.detectedRestricted.forEach {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(it) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.2f), labelColor = Color(0xFFFF9800))
                                    )
                                }
                            }
                        }

                        // Full recognized text
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(if (isSpanish) "Texto Reconocido" else "Recognized Text", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                result.fullText.ifBlank { if (isSpanish) "No se detectó texto" else "No text detected" },
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        showCamera = true
                        analysisResult = null
                        qrResult = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(if (isSpanish) "Escanear Otro" else "Scan Another")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }
}
