package com.alertgia.app.ui.scanmode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.res.painterResource
import com.alertgia.app.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.BorderLight
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.SurfaceBg
import com.alertgia.app.ui.theme.SurfaceCard
import com.alertgia.app.ui.theme.TextPrimary
import com.alertgia.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanModeScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToSmartMenu: () -> Unit,
    viewModel: ScanModeViewModel = hiltViewModel()
) {
    val isSpanish = LocalAppLanguage.current == "es"

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu",
                            tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCard
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // ── App logo ──────────────────────────────────────────────────
            Icon(
                painter = painterResource(R.drawable.ic_alertgia_logo),
                contentDescription = "AlertgIA",
                tint = Color.Unspecified,
                modifier = Modifier.size(104.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (isSpanish) "¿Cómo quieres analizar?" else "How do you want to scan?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (isSpanish)
                    "Elige entre escanear la carta del restaurante o analizar un plato en tiempo real."
                else
                    "Choose between scanning the restaurant menu or analysing a dish in real time.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Two option cards ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScanOptionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.QrCodeScanner,
                    title = if (isSpanish) "Escanear carta" else "Scan menu",
                    subtitle = if (isSpanish)
                        "Lee el QR del restaurante y filtra platos seguros"
                    else
                        "Read the restaurant QR and filter safe dishes",
                    accentColor = AlertgiaGreen,
                    onClick = onNavigateToSmartMenu
                )

                ScanOptionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Visibility,
                    title = if (isSpanish) "Analizar plato" else "Analyse dish",
                    subtitle = if (isSpanish)
                        "Apunta la cámara a cualquier plato para detectar alérgenos"
                    else
                        "Point the camera at any dish to detect allergens",
                    accentColor = Color(0xFF5B8FD4),
                    onClick = onNavigateToCamera
                )
            }
        }
    }
}

// ── Option card ───────────────────────────────────────────────────────────────

@Composable
private fun ScanOptionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderLight, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(accentColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(30.dp)
            )
        }

        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            subtitle,
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )

        // CTA pill
        Box(
            modifier = Modifier
                .background(accentColor, RoundedCornerShape(50.dp))
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "→",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
