package com.alertgia.app.ui.camera

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alertgia.app.data.ml.ModelType
import com.alertgia.app.domain.model.AnalysisMode
import com.alertgia.app.domain.model.SafetyLevel
import com.alertgia.app.domain.model.Severity
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.DangerRed
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.SafeGreen
import com.alertgia.app.ui.theme.WarningAmber
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.util.concurrent.Executors

private val NeutralBlueGrey = Color(0xFF607D8B)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProfiles: () -> Unit = {},
    onNavigateToSmartMenu: (Long) -> Unit = {},
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!cameraPermission.status.isGranted) {
            PermissionDeniedContent(
                shouldShowRationale = cameraPermission.status.shouldShowRationale,
                onRequestPermission = { cameraPermission.launchPermissionRequest() },
                onOpenSettings = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            )
            return@Box
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading profile...", color = Color.White)
            }
            return@Box
        }

        MinimalCameraOverlay(
            uiState = uiState,
            onFrameAvailable = { imageProxy ->
                if (uiState.isScanning) viewModel.onFrameAvailable(imageProxy)
                else imageProxy.close()
            },
            onToggleScanning = { viewModel.toggleScanning() },
            onNavigateBack = onNavigateBack,
            onNavigateToProfiles = onNavigateToProfiles
        )
    }
}

// ── Minimal Camera Overlay — clean camera + single status button ──────────────

@Composable
private fun MinimalCameraOverlay(
    uiState: CameraUiState,
    onFrameAvailable: (androidx.camera.core.ImageProxy) -> Unit,
    onToggleScanning: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToProfiles: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    // ── Severity-based status logic ───────────────────────────────────────
    // RIESGO   → allergen in profile with Severity.SEVERE
    // PRECAUCIÓN → allergen in profile with Severity.MODERATE or MILD
    // SEGURO   → detected food, none match profile allergens
    // null     → not yet analysed
    val displayStatus: SafetyLevel? = remember(
        uiState.detectedLabels, uiState.profile, uiState.scanCount
    ) {
        if (uiState.scanCount == 0) return@remember null          // nothing scanned yet

        val profile = uiState.profile
        val allergenLabels = uiState.detectedLabels.filter { it.isAllergen }

        if (allergenLabels.isEmpty()) return@remember SafetyLevel.SAFE

        if (profile == null) return@remember SafetyLevel.WARNING  // no profile = caution

        val allergyMap = profile.allergies.associate { it.name.lowercase() to it.severity }

        var worst: Severity? = null
        for (label in allergenLabels) {
            val lName = label.name.lowercase()
            val matched = allergyMap.entries
                .firstOrNull { (key, _) -> lName.contains(key) || key.contains(lName) }
                ?.value ?: continue

            when (matched) {
                Severity.SEVERE -> return@remember SafetyLevel.DANGER   // worst case — stop early
                else            -> if (worst == null) worst = matched
            }
        }
        when (worst) {
            null -> SafetyLevel.SAFE
            else -> SafetyLevel.WARNING
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Full-screen camera preview ────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val future = ProcessCameraProvider.getInstance(ctx)
                    future.addListener({
                        val provider = future.get()
                        val preview = Preview.Builder().build()
                            .also { it.surfaceProvider = previewView.surfaceProvider }
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { it.setAnalyzer(analysisExecutor) { proxy -> onFrameAvailable(proxy) } }
                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Profile pill — top-left ───────────────────────────────────────
        uiState.profile?.let { profile ->
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50.dp))
                    .clickable { onNavigateToProfiles() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.AccountCircle, null,
                    tint = AlertgiaGreen, modifier = Modifier.size(15.dp))
                Text(
                    buildString {
                        append(profile.name)
                        profile.allergies.firstOrNull()?.let { append(" · Sin ${it.name}") }
                    },
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Back — top-right ──────────────────────────────────────────────
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint = Color.White, modifier = Modifier.size(18.dp))
        }

        // ── Central status button ─────────────────────────────────────────
        StatusButton(
            status = displayStatus,
            isAnalyzing = uiState.isAnalyzing,
            isScanning = uiState.isScanning,
            onTap = onToggleScanning,
            modifier = Modifier.align(Alignment.Center)
        )
    }

    DisposableEffect(Unit) { onDispose { analysisExecutor.shutdown() } }
}

// ── Status button — single large circle with colour + label ──────────────────

private val ColorRiesgo    = Color(0xFFE53935)
private val ColorPrecaucion = Color(0xFFFB8C00)
private val ColorSeguro    = Color(0xFF43A047)
private val ColorIdle      = Color(0xFF546E7A)

@Composable
private fun StatusButton(
    status: SafetyLevel?,
    isAnalyzing: Boolean,
    isScanning: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpanish = LocalAppLanguage.current == "es"

    val bgColor = when (status) {
        SafetyLevel.DANGER  -> ColorRiesgo
        SafetyLevel.WARNING -> ColorPrecaucion
        SafetyLevel.SAFE    -> ColorSeguro
        null                -> ColorIdle
    }
    val label = when (status) {
        SafetyLevel.DANGER  -> if (isSpanish) "RIESGO"     else "RISK"
        SafetyLevel.WARNING -> if (isSpanish) "PRECAUCIÓN" else "CAUTION"
        SafetyLevel.SAFE    -> if (isSpanish) "SEGURO"     else "SAFE"
        null -> if (isAnalyzing)
            if (isSpanish) "Analizando..." else "Analysing..."
        else
            if (isSpanish) "Apunta al plato" else "Point at dish"
    }
    val icon = when (status) {
        SafetyLevel.DANGER  -> Icons.Default.Warning
        SafetyLevel.WARNING -> Icons.Default.Warning
        SafetyLevel.SAFE    -> Icons.Default.Check
        null                -> Icons.Default.CameraAlt
    }

    // Pulse animation while analyzing
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue  = if (isAnalyzing && status == null) 1.08f else 1f,
        label = "scale",
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse)
    )
    // Glow ring animation when dangerous
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.25f,
        targetValue  = if (status == SafetyLevel.DANGER) 0.65f else 0.25f,
        label = "glow",
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse)
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Outer glow ring (only DANGER)
        Box(contentAlignment = Alignment.Center) {
            if (status == SafetyLevel.DANGER) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(ColorRiesgo.copy(alpha = glowAlpha), CircleShape)
                )
            }
            // Main circle button
            Box(
                modifier = Modifier
                    .size((160 * pulseScale).dp)
                    .background(bgColor, CircleShape)
                    .clickable(onClick = onTap),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        icon, contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        label,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Tap hint when idle
        if (status != null) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isScanning)
                        if (isSpanish) "Toca para pausar" else "Tap to pause"
                    else
                        if (isSpanish) "Toca para reanudar" else "Tap to resume",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ── Legacy full overlay (kept for reference — not used in current flow) ───────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LiveCameraWithOverlay(
    uiState: CameraUiState,
    onFrameAvailable: (androidx.camera.core.ImageProxy) -> Unit,
    onToggleScanning: () -> Unit,
    onCycleMode: () -> Unit,
    onToggleShowAll: () -> Unit,
    onSetRefreshInterval: (Long) -> Unit = {},
    onSetConfidence: (Int) -> Unit = {},
    onCapturePhoto: (ByteArray) -> Unit = {},
    onSetCameraMode: (CameraMode) -> Unit = {},
    onDismissSnapshot: () -> Unit = {},
    onSwitchModel: (ModelType) -> Unit = {},
    onNavigateToSmartMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { androidx.camera.core.ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                    onFrameAvailable(imageProxy)
                                }
                            }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis,
                                imageCapture
                            )
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ===== OVERLAYS =====

        // Top-left: Profile Pill
        uiState.profile?.let { profile ->
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.AccountCircle, null,
                    tint = AlertgiaGreen, modifier = Modifier.size(16.dp))
                Text(
                    buildString {
                        append(profile.name)
                        profile.allergies.firstOrNull()?.let { append(" · Sin ${it.name}") }
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Top-right: scanning indicator + mode badge
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            ScanningIndicator(
                isScanning = uiState.isScanning,
                isAnalyzing = uiState.isAnalyzing
            )
            ModeBadge(
                activeMode = uiState.activeMode,
                analysisMode = uiState.analysisMode,
                isOnline = uiState.isOnline,
                onClick = onCycleMode
            )
            if (uiState.availableModels.size > 1) {
                ModelBadge(
                    currentModel = uiState.currentModelName,
                    availableModels = uiState.availableModels,
                    onSwitch = onSwitchModel
                )
            }
        }

        // Overall safety banner (top-center)
        uiState.overallSafety?.let { safety ->
            SafetyBanner(
                safetyLevel = safety,
                confidence = uiState.confidence,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }

        // Curved arrow labels pointing to food
        if (uiState.detectedLabels.any { it.boundingBox != null }) {
            ArrowLabelsOverlay(labels = uiState.detectedLabels)
        } else {
            // Fallback: center-positioned labels (online mode)
            AnimatedVisibility(
                visible = uiState.detectedLabels.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CenterLabelsOverlay(labels = uiState.detectedLabels)
            }
        }

        // "All clear" overlay when safe and no labels
        AnimatedVisibility(
            visible = uiState.overallSafety == SafetyLevel.SAFE
                    && uiState.detectedLabels.none { it.isAllergen }
                    && uiState.scanCount > 0
                    && !uiState.showAllFoods,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            AllClearOverlay()
        }

        // Side list of recently seen items
        if (uiState.recentlySeenItems.isNotEmpty()) {
            SeenItemsSideList(
                items = uiState.recentlySeenItems,
                currentLabels = uiState.detectedLabels,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp, top = 48.dp, bottom = 100.dp)
            )
        }

        // Bottom info card
        AnimatedVisibility(
            visible = uiState.explanation.isNotBlank(),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomInfoCard(explanation = uiState.explanation)
        }

        // Error message
        uiState.error?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(DangerRed.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(error, color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Snapshot result overlay
        uiState.snapshotResult?.let { result ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .clickable { onDismissSnapshot() }
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(result, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tap to dismiss", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Refresh interval slider (only in offline/auto live scan mode)
            if (uiState.cameraMode == CameraMode.LIVE_SCAN &&
                (uiState.analysisMode != AnalysisMode.ONLINE)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${uiState.refreshIntervalMs}ms", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(48.dp))
                    Slider(
                        value = uiState.refreshIntervalMs.toFloat(),
                        onValueChange = { onSetRefreshInterval(it.toLong()) },
                        valueRange = 50f..2000f,
                        steps = 0,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Confidence threshold slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${uiState.confidenceThreshold}%", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp))
                Slider(
                    value = uiState.confidenceThreshold.toFloat(),
                    onValueChange = { onSetConfidence(it.toInt()) },
                    valueRange = 15f..85f,
                    steps = 0,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = WarningAmber,
                        activeTrackColor = WarningAmber,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                Text("conf", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
            }

            // Main controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show All toggle
                IconButton(
                    onClick = onToggleShowAll,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (uiState.showAllFoods) Color(0xFF1565C0) else Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (uiState.showAllFoods) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Show all foods",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Live Scan mode (video/recording icon)
                IconButton(
                    onClick = {
                        onSetCameraMode(CameraMode.LIVE_SCAN)
                        if (!uiState.isScanning) onToggleScanning()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (uiState.cameraMode == CameraMode.LIVE_SCAN) Color(0xFFF44336) else Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .then(
                            if (uiState.cameraMode == CameraMode.LIVE_SCAN)
                                Modifier.border(2.dp, Color.White, CircleShape)
                            else Modifier
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Live scan",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Photo Capture mode (camera icon)
                IconButton(
                    onClick = {
                        onSetCameraMode(CameraMode.PHOTO_CAPTURE)
                        imageCapture.takePicture(
                            analysisExecutor,
                            object : androidx.camera.core.ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                                    val buffer = image.planes[0].buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    image.close()
                                    onCapturePhoto(bytes)
                                }
                                override fun onError(e: androidx.camera.core.ImageCaptureException) {}
                            }
                        )
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take photo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Pause/Resume (only relevant in live scan mode)
                IconButton(
                    onClick = onToggleScanning,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (uiState.isScanning && uiState.cameraMode == CameraMode.LIVE_SCAN) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isScanning) "Pause" else "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // QR / Smart Menu scan
                IconButton(
                    onClick = onNavigateToSmartMenu,
                    modifier = Modifier
                        .size(44.dp)
                        .background(AlertgiaGreen.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan menu QR",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }
}

@Composable
private fun ArrowLabelsOverlay(labels: List<DetectedAllergenLabel>) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        labels.forEach { label ->
            key(label.id) {
                AnimatedArrowLabel(
                    label = label,
                    widthPx = widthPx,
                    heightPx = heightPx,
                    density = density
                )
            }
        }
    }
}

@Composable
private fun AnimatedArrowLabel(
    label: DetectedAllergenLabel,
    widthPx: Float,
    heightPx: Float,
    density: androidx.compose.ui.unit.Density
) {
    val box = label.boundingBox ?: return
    val color = labelColor(label)

    // Food center in pixels — ViewModel already smooths positions,
    // so we read directly for immediate reactivity
    val foodCx = box.centerX * widthPx
    val foodCy = box.centerY * heightPx

    // Label position: offset above-right of food center, clamped to screen
    val labelPadPx = with(density) { 30.dp.toPx() }
    val labelX = (foodCx + 60f).coerceIn(0f, widthPx - 200f)
    val labelY = (foodCy - 80f).coerceIn(labelPadPx, heightPx - 40f)

    // Pre-compute arrow geometry so Canvas and label chip both use the same values
    val startX = labelX + with(density) { 4.dp.toPx() }
    val startY = labelY + with(density) { 14.dp.toPx() }
    val endX = foodCx
    val endY = foodCy
    val ctrlX = (startX + endX) / 2f + (endY - startY) * 0.2f
    val ctrlY = (startY + endY) / 2f - (endX - startX) * 0.15f
    val isAllergen = label.isAllergen

    // Draw curved arrow + dot using drawBehind on a full-size transparent Box
    // This ensures the drawing updates every recomposition
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Curved arrow path
                val path = Path().apply {
                    moveTo(startX, startY)
                    quadraticBezierTo(ctrlX, ctrlY, endX, endY)
                }
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.85f),
                    style = Stroke(
                        width = if (isAllergen) 3.dp.toPx() else 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Arrowhead
                val arrowSize = 10.dp.toPx()
                val dx = endX - ctrlX
                val dy = endY - ctrlY
                val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val ux = dx / len
                val uy = dy / len

                val arrowPath = Path().apply {
                    moveTo(endX, endY)
                    lineTo(
                        endX - ux * arrowSize + uy * arrowSize * 0.5f,
                        endY - uy * arrowSize - ux * arrowSize * 0.5f
                    )
                    moveTo(endX, endY)
                    lineTo(
                        endX - ux * arrowSize - uy * arrowSize * 0.5f,
                        endY - uy * arrowSize + ux * arrowSize * 0.5f
                    )
                }
                drawPath(
                    path = arrowPath,
                    color = color.copy(alpha = 0.9f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Dot at food center
                drawCircle(
                    color = color.copy(alpha = 0.6f),
                    radius = 6.dp.toPx(),
                    center = Offset(endX, endY)
                )
            }
    )

    // Label chip
    Box(
        modifier = Modifier
            .offset { IntOffset(labelX.toInt(), labelY.toInt()) }
            .background(color.copy(alpha = 0.92f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (label.isAllergen) {
                val icon = when (label.safetyLevel) {
                    SafetyLevel.DANGER -> Icons.Default.Close
                    SafetyLevel.WARNING -> Icons.Default.Warning
                    SafetyLevel.SAFE -> Icons.Default.Check
                }
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = "${label.name} ${(label.confidence * 100).toInt()}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = if (label.isAllergen) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SeenItemsSideList(
    items: List<SeenFoodItem>,
    currentLabels: List<DetectedAllergenLabel>,
    modifier: Modifier = Modifier
) {
    val currentNames = currentLabels.map { it.name.lowercase() }.toSet()

    // Only show items NOT currently visible on screen
    val offScreenItems = items.filter { it.name.lowercase() !in currentNames }
    if (offScreenItems.isEmpty()) return

    LazyColumn(
        modifier = modifier
            .width(140.dp)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                "Seen:",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        items(offScreenItems, key = { it.name }) { item ->
            val color = if (item.isAllergen) {
                when (item.safetyLevel) {
                    SafetyLevel.DANGER -> DangerRed
                    SafetyLevel.WARNING -> WarningAmber
                    SafetyLevel.SAFE -> NeutralBlueGrey
                }
            } else NeutralBlueGrey

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isAllergen) {
                    val icon = when (item.safetyLevel) {
                        SafetyLevel.DANGER -> Icons.Default.Close
                        SafetyLevel.WARNING -> Icons.Default.Warning
                        SafetyLevel.SAFE -> Icons.Default.Check
                    }
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = if (item.isAllergen) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CenterLabelsOverlay(labels: List<DetectedAllergenLabel>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEach { label ->
            val color = labelColor(label)
            val icon = when (label.safetyLevel) {
                SafetyLevel.DANGER -> Icons.Default.Close
                SafetyLevel.WARNING -> Icons.Default.Warning
                SafetyLevel.SAFE -> Icons.Default.Check
            }

            Row(
                modifier = Modifier
                    .background(color.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label.isAllergen) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "${label.name} ${(label.confidence * 100).toInt()}%",
                    color = Color.White,
                    fontWeight = if (label.isAllergen) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (label.isAllergen) 18.sp else 14.sp
                )
            }
        }
    }
}

private fun labelColor(label: DetectedAllergenLabel): Color {
    return if (!label.isAllergen) {
        NeutralBlueGrey
    } else {
        when (label.safetyLevel) {
            SafetyLevel.DANGER -> DangerRed
            SafetyLevel.WARNING -> WarningAmber
            SafetyLevel.SAFE -> SafeGreen
        }
    }
}

@Composable
private fun ModelBadge(
    currentModel: String,
    availableModels: List<ModelType>,
    onSwitch: (ModelType) -> Unit
) {
    // Cycle through available models on tap
    Row(
        modifier = Modifier
            .background(Color(0xFF2E7D32).copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .clickable {
                val currentIdx = availableModels.indexOfFirst { it.displayName == currentModel }
                val next = availableModels[(currentIdx + 1) % availableModels.size]
                onSwitch(next)
            }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Science, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = currentModel, color = Color.White, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Default.SyncAlt, contentDescription = "Switch model", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
    }
}

@Composable
private fun ModeBadge(
    activeMode: String,
    analysisMode: AnalysisMode,
    isOnline: Boolean,
    onClick: () -> Unit
) {
    val icon = when {
        analysisMode == AnalysisMode.OFFLINE -> Icons.Default.PhoneAndroid
        analysisMode == AnalysisMode.ONLINE -> Icons.Default.Cloud
        isOnline -> Icons.Default.Cloud
        else -> Icons.Default.CloudOff
    }

    val bgColor = when {
        analysisMode == AnalysisMode.OFFLINE -> Color(0xFF6A1B9A)
        !isOnline && analysisMode == AnalysisMode.AUTO -> Color(0xFFE65100)
        else -> Color(0xFF1565C0)
    }

    Row(
        modifier = Modifier
            .background(bgColor.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = activeMode, color = Color.White, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Default.SyncAlt, contentDescription = "Switch mode", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
    }
}

@Composable
private fun ScanningIndicator(
    isScanning: Boolean,
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(if (isAnalyzing) pulseAlpha else 1f)
                .background(if (isScanning) SafeGreen else Color.Gray, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = when {
                !isScanning -> "Paused"
                isAnalyzing -> "Analyzing..."
                else -> "Scanning"
            },
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun SafetyBanner(
    safetyLevel: SafetyLevel,
    confidence: String,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when (safetyLevel) {
            SafetyLevel.SAFE -> SafeGreen
            SafetyLevel.WARNING -> WarningAmber
            SafetyLevel.DANGER -> DangerRed
        },
        label = "bannerColor"
    )

    val icon = when (safetyLevel) {
        SafetyLevel.SAFE -> Icons.Default.Check
        SafetyLevel.WARNING -> Icons.Default.Warning
        SafetyLevel.DANGER -> Icons.Default.Close
    }

    Row(
        modifier = modifier
            .background(bgColor.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(safetyLevel.displayName.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        if (confidence.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("($confidence)", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AllClearOverlay() {
    Row(
        modifier = Modifier
            .background(SafeGreen.copy(alpha = 0.88f), RoundedCornerShape(16.dp))
            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text("No allergens detected", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun BottomInfoCard(explanation: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = explanation,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun PermissionDeniedContent(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Camera permission is required to scan items for allergens.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        if (shouldShowRationale) {
            Button(onClick = onRequestPermission) { Text("Grant Permission") }
        } else {
            Button(onClick = onOpenSettings) { Text("Open Settings") }
        }
    }
}
