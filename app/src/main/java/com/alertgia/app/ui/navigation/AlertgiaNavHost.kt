package com.alertgia.app.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alertgia.app.data.preferences.AppPreferences
import com.alertgia.app.ui.camera.CameraScreen
import com.alertgia.app.ui.profile.ProfileListUiState
import com.alertgia.app.ui.profile.ProfileListViewModel
import com.alertgia.app.ui.nearby.NearbyScreen
import com.alertgia.app.ui.onboarding.OnboardingScreen
import com.alertgia.app.ui.splash.SplashScreen
import com.alertgia.app.ui.places.MyPlacesScreen
import com.alertgia.app.ui.profile.ProfileListScreen
import com.alertgia.app.ui.profileeditor.ProfileEditorScreen
import com.alertgia.app.ui.scan.MenuScannerScreen
import com.alertgia.app.ui.scanmode.ScanModeScreen
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

private val bottomNavRoutes = setOf(
    Screen.ScanMode.route,
    Screen.Nearby.route,
    Screen.MyPlaces.route,
    Screen.AlertgiaScore.route,
    Screen.Settings.route,
    Screen.ProfileList.route
)

private val navItemColors @Composable get() = NavigationBarItemDefaults.colors(
    selectedIconColor   = AlertgiaGreen,
    selectedTextColor   = AlertgiaGreen,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
    indicatorColor      = BrandGreenLight
)

private val pinItemColors @Composable get() = NavigationBarItemDefaults.colors(
    selectedIconColor   = AlertgiaGreen,
    selectedTextColor   = AlertgiaGreen,
    unselectedIconColor = AlertgiaGreen.copy(alpha = 0.45f),
    unselectedTextColor = TextSecondary,
    indicatorColor      = BrandGreenLight
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertgiaNavHost(
    profileViewModel: ProfileListViewModel = hiltViewModel()
) {
    val navController       = rememberNavController()
    val context             = LocalContext.current
    val prefs               = remember { AppPreferences(context) }
    val language            by prefs.language.collectAsState(initial = "es")
    val onboardingComplete  by prefs.onboardingComplete.collectAsState(initial = false)
    val profileUiState      by profileViewModel.uiState.collectAsStateWithLifecycle()

    val firstProfileName = when (val s = profileUiState) {
        is ProfileListUiState.Success -> s.profiles.firstOrNull()?.name ?: ""
        else -> ""
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showChrome   = currentRoute in bottomNavRoutes
    val isSpanish    = language == "es"

    val greeting = if (firstProfileName.isNotBlank()) "Hola, $firstProfileName" else "Hola"

    fun navigate(route: String) {
        navController.navigate(route) {
            popUpTo(Screen.ScanMode.route) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides language) {
        Scaffold(
            topBar = {
                if (showChrome) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = greeting,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Screen.ProfileList.route) }) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = if (isSpanish) "Perfiles" else "Profiles",
                                    tint = AlertgiaGreen,
                                    modifier = Modifier.size(96.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor    = androidx.compose.ui.graphics.Color.White,
                            titleContentColor = TextPrimary,
                            actionIconContentColor = AlertgiaGreen
                        )
                    )
                }
            },
            bottomBar = {
                if (showChrome) {
                    NavigationBar(containerColor = NavyMid, contentColor = TextPrimary) {

                        NavigationBarItem(
                            selected = currentRoute == Screen.ScanMode.route,
                            onClick  = { navigate(Screen.ScanMode.route) },
                            icon     = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                            label    = { Text(if (isSpanish) "Escanear" else "Scan") },
                            colors   = navItemColors
                        )

                        NavigationBarItem(
                            selected = currentRoute == Screen.Nearby.route,
                            onClick  = { navigate(Screen.Nearby.route) },
                            icon     = { Icon(Icons.Filled.LocalHospital, contentDescription = null) },
                            label    = { Text(if (isSpanish) "Urgencia" else "Emergency") },
                            colors   = navItemColors
                        )

                        NavigationBarItem(
                            selected = currentRoute == Screen.MyPlaces.route,
                            onClick  = { navigate(Screen.MyPlaces.route) },
                            icon     = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                            label    = { Text(if (isSpanish) "Mis Sitios" else "My Places") },
                            colors   = pinItemColors
                        )

                        NavigationBarItem(
                            selected = currentRoute == Screen.AlertgiaScore.route,
                            onClick  = { navigate(Screen.AlertgiaScore.route) },
                            icon     = { Icon(Icons.Filled.Restaurant, contentDescription = null) },
                            label    = { Text("Score") },
                            colors   = navItemColors
                        )

                        NavigationBarItem(
                            selected = currentRoute == Screen.Settings.route,
                            onClick  = { navigate(Screen.Settings.route) },
                            icon     = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            label    = { Text(if (isSpanish) "Ajustes" else "Settings") },
                            colors   = navItemColors
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController    = navController,
                startDestination = Screen.Splash.route,
                modifier         = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashComplete = {
                            val destination = if (onboardingComplete) Screen.ScanMode.route else Screen.Onboarding.route
                            navController.navigate(destination) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

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

                composable(Screen.ScanMode.route) {
                    ScanModeScreen(
                        onNavigateToCamera    = { navController.navigate(Screen.Camera.createRoute(-1L)) },
                        onNavigateToSmartMenu = { navController.navigate(Screen.SmartMenu.createRoute(-1L)) }
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
                    CameraScreen(onNavigateBack = { navController.popBackStack() })
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

                composable(Screen.Nearby.route)       { NearbyScreen() }
                composable(Screen.MyPlaces.route)     { MyPlacesScreen() }
                composable(Screen.AlertgiaScore.route) { AlertgiaScoreScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Screen.Settings.route)     { SettingsScreen(onNavigateBack = { navController.popBackStack() }) }
            }
        }
    }
}
