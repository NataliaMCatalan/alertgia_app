package com.alertgia.app.ui.score

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.AlertgiaGreenGlow
import com.alertgia.app.ui.theme.BorderLight
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.StatusDanger
import com.alertgia.app.ui.theme.StatusWarning
import com.alertgia.app.ui.theme.SurfaceBg
import com.alertgia.app.ui.theme.SurfaceCard
import com.alertgia.app.ui.theme.SurfaceSubtle
import com.alertgia.app.ui.theme.TextPrimary
import com.alertgia.app.ui.theme.TextSecondary

data class RestaurantScore(
    val name: String,
    val type: String,
    val score: Int,
    val verified: Boolean,
    val allergensHandled: List<String>,
    val address: String,
    val partner: Boolean = false
)

data class RestaurantFeedback(
    val menuUpdated: Boolean? = null,
    val staffTrained: Boolean? = null,
    val crossContactRisk: Boolean? = null
)

private val sampleRestaurants = listOf(
    RestaurantScore("La Tagliatella Passeig de Gràcia", "Italiano", 92, true,
        listOf("Gluten", "Lácteos", "Huevo", "Frutos secos"), "Passeig de Gràcia, 45", partner = true),
    RestaurantScore("Honest Greens Moll de la Fusta", "Saludable", 96, true,
        listOf("Gluten", "Soja", "Lácteos", "Huevo", "Mariscos", "Cacahuetes"), "Moll de la Fusta", partner = true),
    RestaurantScore("Bacoa Burger", "Hamburguesas", 81, true,
        listOf("Gluten", "Lácteos", "Sésamo"), "Carrer de Juli Verne, 2"),
    RestaurantScore("Flax & Kale", "Plant-based", 98, true,
        listOf("Gluten", "Soja", "Frutos secos", "Cacahuetes", "Sésamo"), "Carrer dels Tallers, 74b", partner = true),
    RestaurantScore("Parking Pizza", "Pizzería", 62, false,
        listOf("Gluten", "Lácteos"), "Londres, 98")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertgiaScoreScreen(onNavigateBack: () -> Unit) {
    val isSpanish = LocalAppLanguage.current == "es"
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) sampleRestaurants
        else sampleRestaurants.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.type.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AlertgIA Score",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Shield, null,
                                tint = AlertgiaGreen, modifier = Modifier.size(11.dp))
                            Text(
                                if (isSpanish) "Restaurantes certificados" else "Certified restaurants",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlertgiaGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor  = SurfaceCard,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Search bar ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (isSpanish) "Buscar restaurante..." else "Search restaurant...",
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))          // capsule search bar
                        .border(1.dp, BorderLight, RoundedCornerShape(50.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = SurfaceSubtle,
                        unfocusedContainerColor = SurfaceSubtle,
                        focusedBorderColor      = AlertgiaGreen,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedTextColor        = TextPrimary,
                        unfocusedTextColor      = TextPrimary,
                        cursorColor             = AlertgiaGreen
                    ),
                    singleLine = true
                )
            }

            // ── Legend ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendPill(AlertgiaGreen, if (isSpanish) "Seguro 90+" else "Safe 90+", Icons.Default.Check)
                LegendPill(StatusWarning, if (isSpanish) "Precaución" else "Caution", Icons.Default.Warning)
                LegendPill(StatusDanger,  if (isSpanish) "Riesgo" else "Risk", Icons.Default.Warning)
            }

            // ── List ──────────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { restaurant ->
                    RestaurantCard(restaurant, isSpanish)
                }
            }
        }
    }
}

@Composable
private fun LegendPill(color: Color, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(11.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium, color = color)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RestaurantCard(restaurant: RestaurantScore, isSpanish: Boolean) {
    var feedback by remember { mutableStateOf(RestaurantFeedback()) }
    var feedbackSubmitted by remember { mutableStateOf(false) }
    val scoreColor = when {
        restaurant.score >= 90 -> AlertgiaGreen
        restaurant.score >= 70 -> StatusWarning
        else -> StatusDanger
    }

    // Status label — siempre coherente con scoreColor
    val statusLabel = when {
        restaurant.score >= 90 -> if (isSpanish) "SEGURO" else "SAFE"
        restaurant.score >= 70 -> if (isSpanish) "PRECAUCIÓN" else "CAUTION"
        else                   -> if (isSpanish) "RIESGO" else "RISK"
    }
    val statusIcon = when {
        restaurant.score >= 90 -> Icons.Default.Check
        else                   -> Icons.Default.Warning
    }

    // Tier badge (supplementary detail)
    val tierLabel = when {
        restaurant.score >= 90 -> if (isSpanish) "Excelente" else "Excellent"
        restaurant.score >= 70 -> if (isSpanish) "Bueno" else "Good"
        else                   -> if (isSpanish) "Básico" else "Basic"
    }

    // White card — left color stripe — 28 dp radius
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceCard)
            .height(IntrinsicSize.Min)
    ) {
        // ── Left color stripe (5 dp) — no full border gradient ────────
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(scoreColor)
        )

        // ── Card content ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Name + meta
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            restaurant.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        if (restaurant.verified) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = AlertgiaGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                        Text(restaurant.type, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("·", color = TextSecondary, fontSize = 10.sp)
                        Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                        Text(restaurant.address, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // ── SAFE / SEGURO badge (replaces numeric circle) ─────
                Box(
                    modifier = Modifier
                        .background(scoreColor.copy(alpha = 0.12f), RoundedCornerShape(50.dp))
                        .border(1.5.dp, scoreColor.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(statusIcon, contentDescription = null, tint = scoreColor, modifier = Modifier.size(14.dp))
                        Text(
                            text = statusLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = scoreColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tier + partner badges row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Tier chip
                Box(
                    modifier = Modifier
                        .background(SurfaceSubtle, RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(tierLabel, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }

                if (restaurant.partner) {
                    Box(
                        modifier = Modifier
                            .background(AlertgiaGreenGlow, RoundedCornerShape(50.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (isSpanish) "Partner certificado" else "Certified partner",
                            fontSize = 11.sp,
                            color = AlertgiaGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                if (isSpanish) "Alérgenos con protocolo" else "Allergens with protocol",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Allergen chips — capsule, grey bg, dark text
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                restaurant.allergensHandled.forEach { allergen ->
                    Box(
                        modifier = Modifier
                            .background(SurfaceSubtle, RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(allergen, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // ── Quick Feedback — shown only for ALERT/RIESGO restaurants ──
            AnimatedVisibility(
                visible = restaurant.score < 70,
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderLight)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (feedbackSubmitted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = AlertgiaGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                if (isSpanish) "Feedback enviado. ¡Gracias!" else "Feedback sent. Thank you!",
                                fontSize = 13.sp,
                                color = AlertgiaGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            if (isSpanish) "Feedback rápido" else "Quick Feedback",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusDanger,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Chip row 1: Menu updated?
                        FeedbackChipRow(
                            question = if (isSpanish) "¿Menú actualizado?" else "Menu updated?",
                            selected = feedback.menuUpdated,
                            isSpanish = isSpanish,
                            onYes = { feedback = feedback.copy(menuUpdated = true) },
                            onNo  = { feedback = feedback.copy(menuUpdated = false) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Chip row 2: Staff trained?
                        FeedbackChipRow(
                            question = if (isSpanish) "¿Personal formado?" else "Staff trained?",
                            selected = feedback.staffTrained,
                            isSpanish = isSpanish,
                            onYes = { feedback = feedback.copy(staffTrained = true) },
                            onNo  = { feedback = feedback.copy(staffTrained = false) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Chip row 3: Cross-contact risk?
                        FeedbackChipRow(
                            question = if (isSpanish) "¿Riesgo contaminación?" else "Cross-contact risk?",
                            selected = feedback.crossContactRisk,
                            isSpanish = isSpanish,
                            onYes = { feedback = feedback.copy(crossContactRisk = true) },
                            onNo  = { feedback = feedback.copy(crossContactRisk = false) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit button — capsule
                        val anyAnswered = feedback.menuUpdated != null ||
                                feedback.staffTrained != null ||
                                feedback.crossContactRisk != null
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    if (anyAnswered) AlertgiaGreen else SurfaceSubtle,
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable(enabled = anyAnswered) { feedbackSubmitted = true }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = if (anyAnswered) Color.White else TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    if (isSpanish) "Enviar" else "Submit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (anyAnswered) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackChipRow(
    question: String,
    selected: Boolean?,
    isSpanish: Boolean,
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(question, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeedbackToggleChip(
                label = if (isSpanish) "Sí" else "Yes",
                isSelected = selected == true,
                selectedColor = AlertgiaGreen,
                onClick = onYes
            )
            FeedbackToggleChip(
                label = if (isSpanish) "No" else "No",
                isSelected = selected == false,
                selectedColor = StatusDanger,
                onClick = onNo
            )
        }
    }
}

@Composable
private fun FeedbackToggleChip(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (isSelected) selectedColor.copy(alpha = 0.12f) else SurfaceSubtle,
                RoundedCornerShape(28.dp)
            )
            .border(
                1.5.dp,
                if (isSelected) selectedColor else BorderLight,
                RoundedCornerShape(28.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) selectedColor else TextSecondary
        )
    }
}
