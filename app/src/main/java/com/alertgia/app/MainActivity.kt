package com.alertgia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alertgia.app.ui.navigation.AlertgiaNavHost
import com.alertgia.app.ui.theme.AlertgiaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        // Keep splash visible for ~2.5 seconds
        val startTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            System.currentTimeMillis() - startTime < 2500L
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlertgiaTheme {
                AlertgiaNavHost()
            }
        }
    }
}
