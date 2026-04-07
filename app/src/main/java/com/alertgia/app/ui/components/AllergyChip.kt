package com.alertgia.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alertgia.app.domain.model.Severity
import com.alertgia.app.ui.theme.DangerRed
import com.alertgia.app.ui.theme.SafeGreen
import com.alertgia.app.ui.theme.WarningAmber

@Composable
fun AllergyChip(
    name: String,
    severity: Severity,
    modifier: Modifier = Modifier
) {
    val chipColor = when (severity) {
        Severity.MILD -> SafeGreen
        Severity.MODERATE -> WarningAmber
        Severity.SEVERE -> DangerRed
    }

    AssistChip(
        onClick = {},
        label = { Text(name, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier,
        leadingIcon = {
            if (severity == Severity.SEVERE) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = chipColor
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = chipColor.copy(alpha = 0.15f),
            labelColor = chipColor
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = chipColor.copy(alpha = 0.5f)
        )
    )
}
