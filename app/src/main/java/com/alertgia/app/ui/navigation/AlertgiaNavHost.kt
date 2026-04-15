package com.alertgia.app.ui.navigation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.alertgia.app.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alertgia.app.data.preferences.AppPreferences
import com.alertgia.app.ui.camera.CameraScreen
import com.alertgia.app.ui.scanmode.ScanModeScreen
import com.alertgia.app.ui.nearby.NearbyScreen
import com.alertgia.app.ui.onboarding.OnboardingScreen
import com.alertgia.app.ui.places.MyPlacesScreen
import com.alertgia.app.ui.profile.ProfileListScreen
import com.alertgia.app.ui.profileeditor.ProfileEditorScreen
import com.alertgia.app.ui.scan.MenuScannerScreen
import com.alertgia.app.ui.score.AlertgiaScoreScreen
import com.alertgia.app.ui.settings.SettingsScreen
import com.alertgia.app.ui.smartmenu.SmartMenuScreen
import com.alertgia.app.ui.theme.AlertgiaGreen
import com.alertgia.app.ui.theme.BrandGreenLight
import com.alertgia.app.ui.theme.LocalAppLanguage
import com.alertgia.app.ui.theme.NavyMid
import com.alertgia.app.ui.theme.SurfaceCard
import com.alertgia.app.ui.theme.TextPrimary
import com.alertgia.app.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ScanMode is home — bottom bar shows on all these destinations
private val bottomNavRoutes = setOf(
    Screen.ScanMode.route,
    Screen.Nearby.route,
    Screen.MyPlaces.route,
    Screen.AlertgiaScore.route
)

// Default colours for all nav items
private val navItemColors @Composable get() = NavigationBarItemDefaults.colors(
    selectedIconColor   = AlertgiaGreen,
    selectedTextColor   = AlertgiaGreen,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
    indicatorColor      = BrandGreenLight
)

// Map-pin always shows brand green (faded when unselected) — brand rule
private val pinItemColors @Composable get() = NavigationBarItemDefaults.colors(
    selectedIconColor   = AlertgiaGreen,
    selectedTextColor   = AlertgiaGreen,
    unselectedIconColor = AlertgiaGreen.copy(alpha = 0.45f),
    unselectedTextColor = TextSecondary,
    indicatorColor      = BrandGreenLight
)

@Composable
fun AlertgiaNavHost() {
    val navController  = rememberNavController()
    val context        = LocalContext.current
    val prefs          = remember { AppPreferences(context) }
    val language           by prefs.language.collectAsState(initial = "es")
    val onboardingComplete by prefs.onboardingComplete.collectAsState(initial = false)
    val drawerState    = rememberDrawerState(DrawerValue.Closed)
    val scope          = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes
    val isSpanish = language == "es"

    fun navigate(route: String) {
        navController.navigate(route) {
            popUpTo(Screen.ScanMode.route) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides language) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = SurfaceCard) {
                    Spacer(Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(AlertgiaGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_alertgia_logo),
                                contentDescription = "AlertgIA",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Column {
                            Text(
                                "AlertgIA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                if (isSpanish) "Come seguro, vayas donde vayas"
                                else "Eat safely, wherever you go",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                    Spacer(Modifier.height(12.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                        label = { Text(if (isSpanish) "Perfiles" else "Profiles") },
                        selected = currentRoute == Screen.ProfileList.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.ProfileList.route)
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandGreenLight,
                            selectedIconColor = AlertgiaGreen,
                            selectedTextColor = AlertgiaGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextPrimary
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(if (isSpanish) "Ajustes" else "Settings") },
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigate(Screen.Settings.route)
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandGreenLight,
                            selectedIconColor = AlertgiaGreen,
                            selectedTextColor = AlertgiaGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextPrimary
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar(containerColor = NavyMid, contentColor = TextPrimary) {

                            // ── Escanear (home) ───────────────────────────────
                            NavigationBarItem(
                                selected = currentRoute == Screen.ScanMode.route ||
                                           currentRoute == Screen.Camera.route,
                                onClick  = { navigate(Screen.ScanMode.route) },
                                icon     = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                                label    = { Text(if (isSpanish) "Escanear" else "Scan") },
                                colors   = navItemColors
                            )

                            // ── Urgencia 🏥 ───────────────────────────────────
                            NavigationBarItem(
                                selected = currentRoute == Screen.Nearby.route,
                                onClick  = { navigate(Screen.Nearby.route) },
                                icon     = { Icon(Icons.Filled.LocalHospital, contentDescription = null) },
                                label    = { Text(if (isSpanish) "Urgencia" else "Emergency") },
                                colors   = navItemColors
                            )

                            // ── Mis Sitios 📍 — pin siempre en verde de marca ─
                            NavigationBarItem(
                                selected = currentRoute == Screen.MyPlaces.route,
                                onClick  = { navigate(Screen.MyPlaces.route) },
                                icon     = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                                label    = { Text(if (isSpanish) "Mis Sitios" else "My Places") },
                                colors   = pinItemColors
                            )

                            // ── Score 🥗 ──────────────────────────────────────
                            NavigationBarItem(
                                selected = currentRoute == Screen.AlertgiaScore.route,
                                onClick  = { navigate(Screen.AlertgiaScore.route) },
                                icon     = { Icon(Icons.Filled.Restaurant, contentDescription = null) },
                                label    = { Text("Score") },
                                colors   = navItemColors
                            )

                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController    = navController,
                    startDestination = if (onboardingComplete) Screen.ScanMode.route else Screen.Onboarding.route,
                    modifier         = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(
                            onAccept = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    prefs.setOnboardingComplete(true)
                                    prefs.setRgpdAccepted(true)
                                }
                                navController.navigate(Screen.ScanMode.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            },
                            onDecline = { (context as? Activity)?.finish() }
                        )
                    }

                    // ── Scan Mode — entry point for Escanear tab ──────────
                    composable(Screen.ScanMode.route) {
                        ScanModeScreen(
                            onOpenDrawer       = { scope.launch { drawerState.open() } },
                            onNavigateToCamera = {
                                navController.navigate(Screen.Camera.createRoute(-1L))
                            },
                            onNavigateToSmartMenu = {
                                navController.navigate(Screen.SmartMenu.createRoute(-1L))
                            }
                        )
                    }

                    composable(Screen.ProfileList.route) {
                        ProfileListScreen(
                            onNavigateToEditor      = { navController.navigate(Screen.ProfileEditor.createRoute(it)) },
                            onNavigateToCamera      = { navController.navigate(Screen.Camera.createRoute(it)) },
                            onNavigateToMenuScanner = { navController.navigate(Screen.MenuScanner.createRoute(it)) }
                        )
                    }

                    composable(
                        route     = Screen.ProfileEditor.route,
                        arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = -1L })
                    ) {
                        ProfileEditorScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable(
                        route     = Screen.Camera.route,
                        arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = -1L })
                    ) {
                        CameraScreen(
                            onNavigateBack       = { navController.popBackStack() },
                            onNavigateToProfiles = { scope.launch { drawerState.open() } }
                        )
                    }

                    composable(
                        route     = Screen.MenuScanner.route,
                        arguments = listOf(navArgument("profileId") { type = NavType.LongType })
                    ) {
                        MenuScannerScreen(
                            onNavigateBack        = { navController.popBackStack() },
                            allergens             = emptyList(),
                            restrictedIngredients = emptySet()
                        )
                    }

                    composable(
                        route     = Screen.SmartMenu.route,
                        arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = -1L })
                    ) {
                        SmartMenuScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable(Screen.Nearby.route)    { NearbyScreen() }
                    composable(Screen.MyPlaces.route)  { MyPlacesScreen() }

                    composable(Screen.Settings.route) {
                        SettingsScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable(Screen.AlertgiaScore.route) {
                        AlertgiaScoreScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
