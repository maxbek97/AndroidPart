package com.example.androidpart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModelsLoadingOverlay(progress: Int) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1020).copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Загрузка моделей...",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {

                CircularProgressIndicator(
                    progress = { progress / 100f },
                    strokeWidth = 8.dp,
                    color = Color(0xFF4DA3FF),
                    trackColor = Color(0xFF1B2A4A),
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    text = "$progress%",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}