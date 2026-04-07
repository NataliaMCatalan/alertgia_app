package com.alertgia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alertgia.app.ui.navigation.AlertgiaNavHost
import com.alertgia.app.ui.theme.AlertgiaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlertgiaTheme {
                AlertgiaNavHost()
            }
        }
    }
}
