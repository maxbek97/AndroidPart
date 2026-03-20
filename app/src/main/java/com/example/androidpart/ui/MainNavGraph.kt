package com.example.androidpart.ui


import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.androidpart.data.remote.SessionManager
import com.example.androidpart.ui.screens.AuthScreen.AuthScreen
import com.example.androidpart.ui.screens.MenuScreen.MenuScreen
import com.example.androidpart.ui.screens.SettingsScreen.SettingsScreen
import com.example.androidpart.ui.screens.MainScreen.MainScreen

@OptIn(UnstableApi::class)
@Composable
fun MainNavGraph(navController: NavHostController) {

    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val startDestionation = if (sessionManager.isAuthorized()) "menu" else "auth"
    NavHost(
        navController = navController,
        startDestination = startDestionation
    ) {
        composable("auth") {
            AuthScreen(navController = navController)
        }

        composable("menu") {
            MenuScreen(navController)
        }

        composable("settings") {
            SettingsScreen(navController)
        }

        composable("main") {
            MainScreen(navController)
        }

    }
}