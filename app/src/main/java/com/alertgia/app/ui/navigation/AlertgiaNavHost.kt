package com.alertgia.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alertgia.app.data.preferences.AppPreferences
import com.alertgia.app.domain.model.DietaryRestriction
import com.alertgia.app.ui.camera.CameraScreen
import com.alertgia.app.ui.nearby.NearbyScreen
import com.alertgia.app.ui.profile.ProfileListScreen
import com.alertgia.app.ui.profileeditor.ProfileEditorScreen
import com.alertgia.app.ui.scan.MenuScannerScreen
import com.alertgia.app.ui.settings.SettingsScreen

@Composable
fun AlertgiaNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val language by prefs.language.collectAsState(initial = "en")

    NavHost(
        navController = navController,
        startDestination = Screen.ProfileList.route
    ) {
        composable(Screen.ProfileList.route) {
            ProfileListScreen(
                onNavigateToEditor = { profileId ->
                    navController.navigate(Screen.ProfileEditor.createRoute(profileId))
                },
                onNavigateToCamera = { profileId ->
                    navController.navigate(Screen.Camera.createRoute(profileId))
                },
                onNavigateToMenuScanner = { profileId ->
                    navController.navigate(Screen.MenuScanner.createRoute(profileId))
                },
                onNavigateToNearby = {
                    navController.navigate(Screen.Nearby.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.ProfileEditor.route,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = -1L })
        ) {
            ProfileEditorScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Camera.route,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) {
            CameraScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.MenuScanner.route,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L
            // For simplicity, pass allergens directly. In a full app, load from ViewModel.
            MenuScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                allergens = emptyList(), // Will be loaded by ViewModel in future iteration
                restrictedIngredients = emptySet(),
                isSpanish = language == "es"
            )
        }

        composable(Screen.Nearby.route) {
            NearbyScreen(
                onNavigateBack = { navController.popBackStack() },
                isSpanish = language == "es"
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
