package com.example.androidpart.ui


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.androidpart.data.remote.SessionManager
import com.example.androidpart.ui.screens.AuthScreen.AuthScreen
import com.example.androidpart.ui.screens.MenuScreen.MenuScreen

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
//
//        composable("main_window") {
//            OrganiserHomeScreen(navController)
//        }
    }
}