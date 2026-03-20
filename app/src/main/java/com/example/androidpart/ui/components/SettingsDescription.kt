package com.example.androidpart.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.androidpart.data.remote.SessionManager
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.androidpart.R


@Composable
fun SettingDescription(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.info_circle_svgrepo_com),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp),
            colorFilter = ColorFilter.tint(Color.LightGray)

        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.LightGray
        )
    }
}