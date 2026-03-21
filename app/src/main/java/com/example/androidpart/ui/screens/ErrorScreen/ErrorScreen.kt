package com.example.androidpart.ui.screens.ErrorScreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.androidpart.R

@Composable
fun ErrorScreen(
    navController: NavHostController,
    title: String = "Ошибка",
    message: String
) {
    // --- Анимация появления контента ---
    val appearanceProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appearanceProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        )
    }

    // --- Анимация "левитации" робота ---
    val infiniteTransition = rememberInfiniteTransition(label = "robotFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111845))
    ) {
        // Фоновый круг
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF1B225A),
                radius = 600.dp.toPx(),
                center = center.copy(
                    x = center.x + 300.dp.toPx(),
                    y = center.y + 400.dp.toPx()
                )
            )
        }

        // Заголовок с анимацией появления
        Text(
            text = title,
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .alpha(appearanceProgress.value) // Проявление
                .offset(y = (20 * (1 - appearanceProgress.value)).dp) // Небольшой вылет снизу
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .alpha(appearanceProgress.value)
        ) {
            Spacer(modifier = Modifier.height(120.dp))
            Text(
                text = message,
                fontSize = 24.sp,
                color = Color.LightGray,
                lineHeight = 36.sp,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }

        // Картинка робота с двойной анимацией
        Image(
            painter = painterResource(id = R.drawable.chill_bot),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(1f)
                .scale(1.4f)
                // Совмещаем базовое смещение и анимацию левитации
                .offset(y = (-45).dp + floatOffset.dp)
                .alpha(appearanceProgress.value),
            contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
        )
    }
}