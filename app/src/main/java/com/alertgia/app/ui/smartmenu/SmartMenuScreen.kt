package com.alertgia.app.ui.smartmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.BorderLight
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.StatusDanger
import com.alertgia.app.ui.theme.SurfaceBg
import com.alertgia.app.ui.theme.SurfaceCard
import com.alertgia.app.ui.theme.SurfaceSubtle
import com.alertgia.app.ui.theme.TextPrimary
import com.alertgia.app.ui.theme.TextSecondary

// ── Data model ───────────────────────────────────────────────────────────────

enum class MenuCategory { STARTERS, MAINS, DESSERTS }

data class SmartMenuItem(
    val id: Int,
    val nameEs: String,
    val nameEn: String,
    val descriptionEs: String,
    val descriptionEn: String,
    val category: MenuCategory,
    val allergens: List<String>
)

@Suppress("SpellCheckingInspection")
private val sampleMenuItems = listOf(
    // Starters
    SmartMenuItem(1, "Croquetas de jamón", "Ham croquettes",
        "Bechamel cremosa con jamón ibérico", "Creamy bechamel with Iberian ham",
        MenuCategory.STARTERS, listOf("Gluten", "Lácteos", "Huevo")),
    SmartMenuItem(2, "Ensalada verde", "Green salad",
        "Mezclum, tomate cherry, pepino, vinagreta", "Mesclun, cherry tomatoes, cucumber, vinaigrette",
        MenuCategory.STARTERS, listOf("Mostaza")),
    SmartMenuItem(3, "Hummus con crudités", "Hummus with crudités",
        "Hummus casero con palitos de zanahoria y apio", "Homemade hummus with carrot and celery sticks",
        MenuCategory.STARTERS, listOf("Sésamo", "Gluten")),
    SmartMenuItem(4, "Gazpacho", "Gazpacho",
        "Sopa fría de tomate, pimiento y pepino", "Cold tomato, pepper and cucumber soup",
        MenuCategory.STARTERS, emptyList()),

    // Mains
    SmartMenuItem(5, "Salmón a la plancha", "Grilled salmon",
        "Salmón atlántico con verduras de temporada", "Atlantic salmon with seasonal vegetables",
        MenuCategory.MAINS, listOf("Pescado")),
    SmartMenuItem(6, "Pollo asado", "Roast chicken",
        "Pollo de corral con patatas y romero", "Free-range chicken with potatoes and rosemary",
        MenuCategory.MAINS, emptyList()),
    SmartMenuItem(7, "Pasta boloñesa", "Bolognese pasta",
        "Tagliatelle con ragú de ternera y parmesano", "Tagliatelle with veal ragù and parmesan",
        MenuCategory.MAINS, listOf("Gluten", "Lácteos")),
    SmartMenuItem(8, "Risotto de setas", "Mushroom risotto",
        "Arroz arborio con boletus y trufa", "Arborio rice with boletus and truffle",
        MenuCategory.MAINS, listOf("Lácteos")),
    SmartMenuItem(9, "Bowl vegano", "Vegan bowl",
        "Quinoa, garbanzos, aguacate y salsa tahini", "Quinoa, chickpeas, avocado and tahini sauce",
        MenuCategory.MAINS, listOf("Sésamo")),

    // Desserts
    SmartMenuItem(10, "Tarta de queso", "Cheesecake",
        "Tarta de queso al horno estilo vasco", "Basque-style baked cheesecake",
        MenuCategory.DESSERTS, listOf("Lácteos", "Huevo", "Gluten")),
    SmartMenuItem(11, "Sorbete de limón", "Lemon sorbet",
        "Sorbete artesanal sin lactosa", "Artisan lactose-free sorbet",
        MenuCategory.DESSERTS, emptyList()),
    SmartMenuItem(12, "Coulant de chocolate", "Chocolate lava cake",
        "Bizcocho templado con interior fundente", "Warm cake with molten chocolate center",
        MenuCategory.DESSERTS, listOf("Gluten", "Lácteos", "Huevo", "Frutos secos"))
)

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SmartMenuScreen(
    onNavigateBack: () -> Unit,
    viewModel: SmartMenuViewModel = hiltViewModel()
) {
    val isSpanish = LocalAppLanguage.current == "es"
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    // Allergen names from the active profile (lower-cased for matching)
    val profileAllergenNames = remember(profile) {
        profile?.allergies?.map { it.name.lowercase() } ?: emptyList()
    }

    fun isUnsafe(item: SmartMenuItem): Boolean =
        item.allergens.any { allergen ->
            profileAllergenNames.any { it.contains(allergen.lowercase()) || allergen.lowercase().contains(it) }
        }

    fun whyUnsafe(item: SmartMenuItem): List<String> =
        item.allergens.filter { allergen ->
            profileAllergenNames.any { it.contains(allergen.lowercase()) || allergen.lowercase().contains(it) }
        }

    var showSafeOnly by remember { mutableStateOf(false) }

    // Group items by category, applying the safe-only filter
    val categoryHeaders = if (isSpanish)
        mapOf(MenuCategory.STARTERS to "Entrantes", MenuCategory.MAINS to "Principales", MenuCategory.DESSERTS to "Postres")
    else
        mapOf(MenuCategory.STARTERS to "Starters", MenuCategory.MAINS to "Mains", MenuCategory.DESSERTS to "Desserts")

    val groupedItems = MenuCategory.entries.associateWith { cat ->
        sampleMenuItems.filter { it.category == cat && (!showSafeOnly || !isUnsafe(it)) }
    }

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isSpanish) "Menú Inteligente" else "Smart Menu",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.QrCode, null,
                                tint = AlertgiaGreen, modifier = Modifier.size(12.dp))
                            Text(
                                if (isSpanish) "Escaneado por IA" else "AI-scanned",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlertgiaGreen
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    // Safe-only filter chip
                    FilterChip(
                        selected = showSafeOnly,
                        onClick = { showSafeOnly = !showSafeOnly },
                        label = {
                            Text(
                                if (isSpanish) "Solo seguros" else "Safe only",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = if (showSafeOnly) {
                            { Icon(Icons.Default.Shield, null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.padding(end = 8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AlertgiaGreen.copy(alpha = 0.15f),
                            selectedLabelColor = AlertgiaGreen,
                            selectedLeadingIconColor = AlertgiaGreen
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = showSafeOnly,
                            selectedBorderColor = AlertgiaGreen,
                            borderColor = BorderLight
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCard,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
        ) {

            // ── Profile context banner ────────────────────────────────────
            profile?.let { p ->
                item(key = "profile_banner") {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceCard)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(AlertgiaGreen.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Shield, null,
                                        tint = AlertgiaGreen, modifier = Modifier.size(14.dp))
                                    Text(
                                        p.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AlertgiaGreen
                                    )
                                }
                            }
                            Text(
                                if (isSpanish) "${p.allergies.size} alérgeno(s) activo(s)"
                                else "${p.allergies.size} active allergen(s)",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderLight))
                    }
                }
            }

            // ── One section per category — sticky header + items ─────────
            MenuCategory.entries.forEach { category ->
                val items = groupedItems[category] ?: emptyList()

                // Skip the entire section when safe-only is on and there's nothing left
                if (showSafeOnly && items.isEmpty()) return@forEach

                stickyHeader(key = "header_${category.name}") {
                    CategoryStickyHeader(
                        label = categoryHeaders[category] ?: category.name,
                        itemCount = items.size,
                        safeCount = items.count { !isUnsafe(it) }
                    )
                }

                if (items.isEmpty()) {
                    item(key = "empty_${category.name}") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isSpanish) "No hay platos en esta categoría"
                                else "No dishes in this category",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            MenuItemCard(
                                item = item,
                                isUnsafe = isUnsafe(item),
                                triggeredAllergens = whyUnsafe(item),
                                isSpanish = isSpanish
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Sticky category header ────────────────────────────────────────────────────

@Composable
private fun CategoryStickyHeader(label: String, itemCount: Int, safeCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .background(AlertgiaGreen, RoundedCornerShape(2.dp))
            )
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 1.sp
            )
        }
        // Safe count badge
        Box(
            modifier = Modifier
                .background(AlertgiaGreen.copy(alpha = 0.12f), RoundedCornerShape(50.dp))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                "$safeCount / $itemCount",
                fontSize = 11.sp,
                color = AlertgiaGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(BorderLight))
}

// ── Menu Item Card ────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuItemCard(
    item: SmartMenuItem,
    isUnsafe: Boolean,
    triggeredAllergens: List<String>,
    isSpanish: Boolean
) {
    var showWhy by remember { mutableStateOf(false) }
    val safeColor = AlertgiaGreen
    val unsafeColor = StatusDanger

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceCard)
            .height(IntrinsicSize.Min)
    ) {
        // Left color stripe
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(if (isUnsafe) unsafeColor else safeColor)
        )

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isSpanish) item.nameEs else item.nameEn,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        if (isSpanish) item.descriptionEs else item.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Safety badge
                Box(
                    modifier = Modifier
                        .background(
                            (if (isUnsafe) unsafeColor else safeColor).copy(alpha = 0.12f),
                            RoundedCornerShape(50.dp)
                        )
                        .border(
                            1.5.dp,
                            (if (isUnsafe) unsafeColor else safeColor).copy(alpha = 0.35f),
                            RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnsafe) Icons.Default.Close else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isUnsafe) unsafeColor else safeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Allergen chips
            if (item.allergens.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.allergens.forEach { allergen ->
                        val isTriggered = allergen in triggeredAllergens
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isTriggered) unsafeColor.copy(alpha = 0.10f) else SurfaceSubtle,
                                    RoundedCornerShape(50.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isTriggered) unsafeColor.copy(alpha = 0.4f) else Color.Transparent,
                                    RoundedCornerShape(50.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                allergen,
                                fontSize = 11.sp,
                                color = if (isTriggered) unsafeColor else TextPrimary,
                                fontWeight = if (isTriggered) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // "Why?" expandable section for unsafe items
            if (isUnsafe) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(unsafeColor.copy(alpha = 0.08f))
                        .clickable { showWhy = !showWhy }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Close, null,
                        tint = unsafeColor, modifier = Modifier.size(13.dp))
                    Text(
                        if (isSpanish) "¿Por qué no es seguro?" else "Why is this unsafe?",
                        fontSize = 12.sp,
                        color = unsafeColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                AnimatedVisibility(
                    visible = showWhy,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            if (isSpanish)
                                "Contiene alérgenos de tu perfil: ${triggeredAllergens.joinToString(", ")}"
                            else
                                "Contains allergens from your profile: ${triggeredAllergens.joinToString(", ")}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
