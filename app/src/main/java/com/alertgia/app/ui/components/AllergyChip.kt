package com.alertgia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.alertgia.app.ui.theme.SurfaceSubtle
import com.alertgia.app.ui.theme.TextPrimary

@Composable
fun AllergyChip(
    name: String,
    severity: Severity,
    modifier: Modifier = Modifier
) {
    // Severity shown only by the coloured dot — chip itself stays neutral
    val dotColor = when (severity) {
        Severity.MILD     -> StatusSafe
        Severity.MODERATE -> StatusWarning
        Severity.SEVERE   -> StatusDanger
    }

    Row(
        modifier = modifier
            .background(SurfaceSubtle, RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1
        )
    }
}
