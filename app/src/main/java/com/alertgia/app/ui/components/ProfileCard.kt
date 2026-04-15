package com.alertgia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertgia.app.domain.model.UserProfile
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.NavyBorder
import com.alertgia.app.ui.theme.NavyGlass
import com.alertgia.app.ui.theme.NavyLight
import com.alertgia.app.ui.theme.NavyMid
import com.alertgia.app.ui.theme.TextPrimary
import com.alertgia.app.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileCard(
    profile: UserProfile,
    onScan: () -> Unit,
    onEdit: () -> Unit,
    onMenuScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(profile.avatarColor).copy(alpha = 0.4f),
                        NavyBorder
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onScan),
        color = NavyMid,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp
    ) {
        Column {
            // Gradient accent bar at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(profile.avatarColor),
                                Color(profile.avatarColor).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(profile.avatarColor),
                                    Color(profile.avatarColor).copy(alpha = 0.6f)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = profile.name.take(1).uppercase(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Name + chips
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (profile.allergies.isEmpty()) {
                        Text(
                            text = "Sin alergias configuradas",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            profile.allergies.forEach { allergy ->
                                AllergyChip(name = allergy.name, severity = allergy.severity)
                            }
                        }
                    }
                }
            }

            // Action bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NavyBorder)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyLight.copy(alpha = 0.5f)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CardAction(
                    icon = Icons.Default.CameraAlt,
                    label = "Escanear",
                    tint = AlertgiaGreen,
                    onClick = onScan,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.width(1.dp).height(44.dp).background(NavyBorder))
                CardAction(
                    icon = Icons.Default.MenuBook,
                    label = "Menú",
                    tint = Color(0xFF60A5FA),
                    onClick = onMenuScan,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.width(1.dp).height(44.dp).background(NavyBorder))
                CardAction(
                    icon = Icons.Default.Edit,
                    label = "Editar",
                    tint = TextSecondary,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CardAction(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}
