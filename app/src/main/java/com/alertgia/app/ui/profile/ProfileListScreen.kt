package com.alertgia.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alertgia.app.R
import com.alertgia.app.ui.components.ProfileCard
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.AlertgiaGreenGlow
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.NavyDeep
import com.alertgia.app.ui.theme.NavyLight
import com.alertgia.app.ui.theme.NavyMid
import com.alertgia.app.ui.theme.TextPrimary
import com.alertgia.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
    onNavigateToEditor: (Long) -> Unit,
    onNavigateToCamera: (Long) -> Unit,
    onNavigateToMenuScanner: (Long) -> Unit = {},
    viewModel: ProfileListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSpanish = LocalAppLanguage.current == "es"

    Scaffold(
        containerColor = NavyDeep,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(-1L) },
                containerColor = AlertgiaGreen,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = if (isSpanish) "Añadir perfil" else "Add profile")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Content ───────────────────────────────────────────────────
            when (val state = uiState) {
                is ProfileListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AlertgiaGreen)
                    }
                }

                is ProfileListUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(AlertgiaGreenGlow, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp),
                                    tint = AlertgiaGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = if (isSpanish) "Sin perfiles aún" else "No profiles yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isSpanish)
                                    "Crea un perfil con tus alergias\ny empieza a escanear de forma segura"
                                else
                                    "Create a profile with your allergies\nand start scanning safely",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is ProfileListUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.profiles, key = { it.id }) { profile ->
                            ProfileCard(
                                profile = profile,
                                onScan = { onNavigateToCamera(profile.id) },
                                onEdit = { onNavigateToEditor(profile.id) },
                                onMenuScan = { onNavigateToMenuScanner(profile.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
                    }
                }
            }
        }
    }
}
