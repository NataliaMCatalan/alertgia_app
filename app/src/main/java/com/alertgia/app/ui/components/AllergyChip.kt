package com.alertgia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertgia.app.domain.model.Severity
import com.alertgia.app.ui.theme.StatusDanger
import com.alertgia.app.ui.theme.StatusSafe
import com.alertgia.app.ui.theme.StatusWarning
import com.alertgia.app.ui.theme.TextPrimary

@Composable
fun AllergyChip(
    name: String,
    severity: Severity,
    modifier: Modifier = Modifier
) {
    val chipColor = when (severity) {
        Severity.MILD     -> StatusSafe
        Severity.MODERATE -> StatusWarning
        Severity.SEVERE   -> StatusDanger
    }
    val chipIcon = when (severity) {
        Severity.MILD     -> Icons.Default.Check
        Severity.MODERATE -> Icons.Default.Info
        Severity.SEVERE   -> Icons.Default.Warning
    }

    Row(
        modifier = modifier
            .background(chipColor.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = chipIcon,
            contentDescription = null,
            tint = chipColor,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1
        )
    }
}
