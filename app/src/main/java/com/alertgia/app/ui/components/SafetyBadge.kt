package com.alertgia.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertgia.app.domain.model.SafetyLevel
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.NavyBorder
import com.alertgia.app.ui.theme.NavyLight
import com.alertgia.app.ui.theme.NavyMid
import com.alertgia.app.ui.theme.TextSecondary

// ── Data model ────────────────────────────────────────────────────────────────

data class SafetyFeedback(
    val noAllergenMenu: Boolean      = false,
    val staffUntrained: Boolean      = false,
    val crossContaminationRisk: Boolean = false
) {
    val hasAnySelection get() = noAllergenMenu || staffUntrained || crossContaminationRisk
}

// ── Colors (local to component) ───────────────────────────────────────────────

private val SafeColor    = Color(0xFF7DB366)
private val CautionColor = Color(0xFFF59E0B)
private val AlertColor   = Color(0xFFE57373)

// ── Main component ────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafetyBadge(
    safetyLevel: SafetyLevel,
    modifier: Modifier = Modifier,
    onFeedbackSubmit: (SafetyFeedback) -> Unit = {}
) {
    val isSpanish = LocalAppLanguage.current == "es"

    val badgeColor = when (safetyLevel) {
        SafetyLevel.SAFE    -> SafeColor
        SafetyLevel.WARNING -> CautionColor
        SafetyLevel.DANGER  -> AlertColor
    }
    val badgeIcon = when (safetyLevel) {
        SafetyLevel.SAFE    -> Icons.Default.Check
        SafetyLevel.WARNING -> Icons.Default.Warning
        SafetyLevel.DANGER  -> Icons.Default.Warning
    }
    val badgeLabel = when (safetyLevel) {
        SafetyLevel.SAFE    -> "SAFE / SEGURO"
        SafetyLevel.WARNING -> "CAUTION / PRECAUCIÓN"
        SafetyLevel.DANGER  -> "ALERT / ALERTA"
    }

    var feedback by remember(safetyLevel) { mutableStateOf(SafetyFeedback()) }
    var submitted by remember(safetyLevel) { mutableStateOf(false) }

    Column(modifier = modifier) {

        // ── Badge pill ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .background(badgeColor.copy(alpha = 0.10f), RoundedCornerShape(50.dp))
                .border(1.dp, badgeColor.copy(alpha = 0.40f), RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = badgeIcon,
                contentDescription = badgeLabel,
                tint = badgeColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = badgeLabel,
                color = badgeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }

        // ── Feedback section (DANGER only) ────────────────────────────────
        AnimatedVisibility(
            visible = safetyLevel == SafetyLevel.DANGER && !submitted,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .background(NavyMid, RoundedCornerShape(16.dp))
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isSpanish) "Cuéntanos qué falló" else "Tell us what went wrong",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlertColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isSpanish) "Selecciona todos los que apliquen"
                           else "Select all that apply",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeedbackChip(
                        label = if (isSpanish) "Sin menú de alérgenos" else "No allergen menu",
                        selected = feedback.noAllergenMenu,
                        alertColor = AlertColor,
                        onClick = { feedback = feedback.copy(noAllergenMenu = !feedback.noAllergenMenu) }
                    )
                    FeedbackChip(
                        label = if (isSpanish) "Personal no formado" else "Staff untrained",
                        selected = feedback.staffUntrained,
                        alertColor = AlertColor,
                        onClick = { feedback = feedback.copy(staffUntrained = !feedback.staffUntrained) }
                    )
                    FeedbackChip(
                        label = if (isSpanish) "Riesgo de trazas" else "Cross-contamination risk",
                        selected = feedback.crossContaminationRisk,
                        alertColor = AlertColor,
                        onClick = { feedback = feedback.copy(crossContaminationRisk = !feedback.crossContaminationRisk) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onFeedbackSubmit(feedback)
                        submitted = true
                    },
                    enabled = feedback.hasAnySelection,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertColor,
                        disabledContainerColor = NavyLight
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSpanish) "Enviar" else "Submit",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ── Confirmation after submit ─────────────────────────────────────
        AnimatedVisibility(
            visible = safetyLevel == SafetyLevel.DANGER && submitted,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(SafeColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(1.dp, SafeColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SafeColor, modifier = Modifier.size(18.dp))
                Text(
                    text = if (isSpanish) "Gracias, tu feedback ayuda a mejorar la seguridad alimentaria."
                           else "Thanks, your feedback helps improve food safety.",
                    fontSize = 12.sp,
                    color = SafeColor,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ── Selection chip ────────────────────────────────────────────────────────────

@Composable
private fun FeedbackChip(
    label: String,
    selected: Boolean,
    alertColor: Color,
    onClick: () -> Unit
) {
    val bg     = if (selected) alertColor.copy(alpha = 0.12f) else NavyLight
    val border = if (selected) alertColor.copy(alpha = 0.50f) else NavyBorder
    val text   = if (selected) alertColor else TextSecondary

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50.dp))
            .border(1.dp, border, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = text, modifier = Modifier.size(12.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Safe", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
private fun PreviewSafe() {
    CompositionLocalProvider(LocalAppLanguage provides "es") {
        SafetyBadge(safetyLevel = SafetyLevel.SAFE)
    }
}

@Preview(name = "Warning", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
private fun PreviewWarning() {
    CompositionLocalProvider(LocalAppLanguage provides "es") {
        SafetyBadge(safetyLevel = SafetyLevel.WARNING)
    }
}

@Preview(name = "Alert + Feedback", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
private fun PreviewAlert() {
    CompositionLocalProvider(LocalAppLanguage provides "es") {
        SafetyBadge(safetyLevel = SafetyLevel.DANGER)
    }
}
