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
import com.example.androidpart.ui.screens.ErrorScreen.ErrorScreen
import com.example.androidpart.ui.screens.MenuScreen.MenuScreen
import com.example.androidpart.ui.screens.SettingsScreen.SettingsScreen
import com.example.androidpart.ui.screens.MainScreen.MainScreen
import com.example.androidpart.ui.screens.CalibrationScreen.CalibrationScreen

@OptIn(UnstableApi::class)
@Composable
fun MainNavGraph(navController: NavHostController) {

    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val startDestination = if (sessionManager.isAuthorized()) "menu" else "auth"
//    val startDestination = "error/camera"
    NavHost(
        navController = navController,
        startDestination = startDestination
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

        composable("error/server") {
            ErrorScreen(
                navController,
                title = "Ошибка подключения",
                message = "Не удалось подключиться к серверу. \nПопробуйте позже."
            )
        }

        composable("error/camera") {
            ErrorScreen(
                navController,
                title = "Ошибка разрешений",
                message = "Приложению нужен доступ к камере для работы VR режима."
            )
        }

        composable("calibration") {
            CalibrationScreen(navController)
        }

    }
}