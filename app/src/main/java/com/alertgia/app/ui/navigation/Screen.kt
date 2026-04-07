package com.alertgia.app.ui.navigation

sealed class Screen(val route: String) {
    data object ProfileList : Screen("profiles")
    data object ProfileEditor : Screen("profile_editor/{profileId}") {
        fun createRoute(profileId: Long = -1L) = "profile_editor/$profileId"
    }
    data object Camera : Screen("camera/{profileId}") {
        fun createRoute(profileId: Long) = "camera/$profileId"
    }
    data object MenuScanner : Screen("menu_scanner/{profileId}") {
        fun createRoute(profileId: Long) = "menu_scanner/$profileId"
    }
    data object Nearby : Screen("nearby")
    data object Settings : Screen("settings")
}
