package com.alertgia.app.ui.navigation

import android.app.Activity
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
import com.alertgia.app.ui.camera.CameraScreen
import com.alertgia.app.ui.nearby.NearbyScreen
import com.alertgia.app.ui.onboarding.OnboardingScreen
import com.alertgia.app.ui.profile.ProfileListScreen
import com.alertgia.app.ui.profileeditor.ProfileEditorScreen
import com.alertgia.app.ui.scan.MenuScannerScreen
import com.alertgia.app.ui.score.AlertgiaScoreScreen
import com.alertgia.app.ui.settings.SettingsScreen
import com.alertgia.app.ui.splash.SplashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AlertgiaNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val language by prefs.language.collectAsState(initial = "en")
    val onboardingComplete by prefs.onboardingComplete.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    val destination = if (onboardingComplete) {
                        Screen.ProfileList.route
                    } else {
                        Screen.Onboarding.route
                    }
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
                    navController.navigate(Screen.ProfileList.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onDecline = {
                    (context as? Activity)?.finish()
                }
            )
        }

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
                },
                onNavigateToScore = {
                    navController.navigate(Screen.AlertgiaScore.route)
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
        ) {
            MenuScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                allergens = emptyList(),
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

        composable(Screen.AlertgiaScore.route) {
            AlertgiaScoreScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
