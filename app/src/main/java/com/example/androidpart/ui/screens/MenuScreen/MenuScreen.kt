package com.example.androidpart.ui.screens.MenuScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.androidpart.R
import com.example.androidpart.data.remote.SessionManager
import com.example.androidpart.ui.components.MenuButton

@Composable
fun MenuScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

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
            Text(
                text = "IVANVISION",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                lineHeight = 52.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp),
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(4f, 4f),
                        blurRadius = 48f
                    )
                )
            )
            MenuButton(
                label = "Старт",
                iconRes = R.drawable.qr_code_svgrepo_com,
                onClick = {
                    navController.navigate("main")
                }
            )

            MenuButton(
                label = "Настройки",
                iconRes = R.drawable.cogwheel_configuration_gear_svgrepo_com,
                onClick = {
                    navController.navigate("settings")
                }
            )

        }
    }
}