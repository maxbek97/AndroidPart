package com.example.androidpart.ui.screens.MenuScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.androidpart.R
import com.example.androidpart.ui.components.MenuButton

@Composable
fun MenuScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111845)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            MenuButton(
                label = "Старт",
                iconRes = R.drawable.qr_code_svgrepo_com,
                onClick = {
                    // TODO: Действие при клике "Старт"
                }
            )

            MenuButton(
                label = "Настройки",
                iconRes = R.drawable.cogwheel_configuration_gear_svgrepo_com,
                onClick = {
                    // TODO: Действие при клике "Настройки"
                }
            )
            MenuButton(
                label = "Выйти из аккаунта",
                iconRes = R.drawable.exit_svgrepo_com,
                onClick = {
                    // TODO: Действие при клике "Настройки"
                }
            )
        }
    }
}