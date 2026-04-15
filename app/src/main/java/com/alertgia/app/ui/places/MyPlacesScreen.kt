package com.alertgia.app.ui.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.DangerRed
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.NavyDeep
import com.alertgia.app.ui.theme.TextPrimary

enum class PlaceType { PHARMACY, HOSPITAL, RESTAURANT }

data class FavoritePlace(
    val name: String,
    val type: PlaceType,
    val notes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlacesScreen() {
    val isSpanish = LocalAppLanguage.current == "es"
    val places = remember { emptyList<FavoritePlace>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSpanish) "Mis Sitios" else "My Places") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDeep,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Google account sync banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                if (isSpanish) "Sincronizar con Google" else "Sync with Google",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (isSpanish) "Accede a tus lugares desde cualquier dispositivo"
                                else "Access your places from any device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { /* TODO: Google Sign-In */ }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSpanish) "Conectar" else "Connect")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (places.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (isSpanish) "Aún no tienes lugares guardados"
                        else "No saved places yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (isSpanish)
                            "Guarda restaurantes, farmacias y hospitales donde ya sabes que puedes comer seguro o que tienen tu medicación."
                        else
                            "Save restaurants, pharmacies and hospitals where you know you can eat safely or they carry your medication.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { /* TODO: add place flow */ },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertgiaGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSpanish) "Añadir lugar" else "Add place")
                    }
                }
            } else {
                places.forEach { place ->
                    PlaceCard(place = place, isSpanish = isSpanish)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PlaceCard(place: FavoritePlace, isSpanish: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (place.type) {
                    PlaceType.PHARMACY -> Icons.Default.LocalPharmacy
                    PlaceType.HOSPITAL -> Icons.Default.LocalHospital
                    PlaceType.RESTAURANT -> Icons.Default.Restaurant
                },
                contentDescription = null,
                tint = when (place.type) {
                    PlaceType.PHARMACY -> AlertgiaGreen
                    PlaceType.HOSPITAL -> DangerRed
                    PlaceType.RESTAURANT -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (place.notes.isNotBlank()) {
                    Text(
                        place.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
