package com.alertgia.app.ui.score

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.alertgia.app.ui.theme.NavyDeep

data class RestaurantScore(
    val name: String,
    val type: String,
    val score: Int,
    val verified: Boolean,
    val allergensHandled: List<String>,
    val address: String,
    val partner: Boolean = false
)

private val sampleRestaurants = listOf(
    RestaurantScore(
        name = "La Tagliatella Passeig de Gràcia",
        type = "Italiano",
        score = 92,
        verified = true,
        allergensHandled = listOf("Gluten", "Lácteos", "Huevo", "Frutos secos"),
        address = "Passeig de Gràcia, 45 · Barcelona",
        partner = true
    ),
    RestaurantScore(
        name = "Honest Greens Moll de la Fusta",
        type = "Saludable",
        score = 96,
        verified = true,
        allergensHandled = listOf("Gluten", "Soja", "Lácteos", "Huevo", "Mariscos", "Cacahuetes"),
        address = "Moll de la Fusta · Barcelona",
        partner = true
    ),
    RestaurantScore(
        name = "Bacoa Burger",
        type = "Hamburguesas",
        score = 81,
        verified = true,
        allergensHandled = listOf("Gluten", "Lácteos", "Sésamo"),
        address = "Carrer de Juli Verne, 2 · Barcelona",
        partner = false
    ),
    RestaurantScore(
        name = "Flax & Kale",
        type = "Plant-based",
        score = 98,
        verified = true,
        allergensHandled = listOf("Gluten", "Soja", "Frutos secos", "Cacahuetes", "Sésamo"),
        address = "Carrer dels Tallers, 74b · Barcelona",
        partner = true
    ),
    RestaurantScore(
        name = "Parking Pizza",
        type = "Pizzería",
        score = 74,
        verified = false,
        allergensHandled = listOf("Gluten", "Lácteos"),
        address = "Londres, 98 · Barcelona",
        partner = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertgiaScoreScreen(
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) sampleRestaurants
        else sampleRestaurants.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.type.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AlertgIA Score", fontWeight = FontWeight.Bold)
                        Text(
                            "Restaurantes certificados",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDeep,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar restaurante...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            ScoreLegend()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { restaurant ->
                    RestaurantScoreCard(restaurant)
                }
            }
        }
    }
}

@Composable
private fun ScoreLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScoreBadge(range = "90-100", label = "Excelente", color = AlertgiaGreen)
        ScoreBadge(range = "70-89", label = "Bueno", color = Color(0xFFFFC107))
        ScoreBadge(range = "<70", label = "Básico", color = Color(0xFFFF7043))
    }
}

@Composable
private fun ScoreBadge(range: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text("$range $label", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RestaurantScoreCard(restaurant: RestaurantScore) {
    val scoreColor = when {
        restaurant.score >= 90 -> AlertgiaGreen
        restaurant.score >= 70 -> Color(0xFFFFC107)
        else -> Color(0xFFFF7043)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = restaurant.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (restaurant.verified) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = AlertgiaGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = restaurant.type,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = restaurant.address,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(scoreColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${restaurant.score}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = scoreColor
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = scoreColor, modifier = Modifier.size(10.dp))
                        Text("Score", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (restaurant.partner) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AlertgiaGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Partner AlertgIA certificado",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = AlertgiaGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Alérgenos con protocolo verificado:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                restaurant.allergensHandled.forEach { allergen ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = allergen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
