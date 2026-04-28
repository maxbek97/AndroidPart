package com.example.androidpart.ui.screens.SettingsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import com.example.androidpart.R
import com.example.androidpart.data.local.SettingsDataStore
import com.example.androidpart.ui.components.DropdownSelector
import com.example.androidpart.ui.components.MarkerSizeInput
import com.example.androidpart.ui.components.SettingDescription
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val metrics = context.resources.displayMetrics
    val settingsDataStore = remember { SettingsDataStore(context) }
    val scope = rememberCoroutineScope()
    val fpsOptions = listOf(24, 30, 60)


    val resolutions = remember {
        getAvailableRes(
            metrics.widthPixels,
            metrics.heightPixels
        )
    }

    var markerInput by remember { mutableStateOf("0.05") }
    var selectedMarkerSize by remember { mutableStateOf(0.05f) } // 5 см по умолчанию
    var selectedResolution by remember { mutableStateOf(resolutions.first()) }
    var selectedFps by remember { mutableStateOf(fpsOptions.first()) }
    var saveJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        settingsDataStore.settingsFlow.collect { (res, fps, markerSize) ->
            val resObj = resolutions.find { it.toString() == res}
            if (resObj != null ) selectedResolution = resObj
            if (fpsOptions.contains(fps)) selectedFps = fps
            if (markerSize != selectedMarkerSize) {
                selectedMarkerSize = markerSize
                markerInput = markerSize.toString()
            }
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111845)),
    ) {
            // ====== ВЕРХ ======
            Text(
                text = "Настройки",
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(36.dp)
            )

        // ====== Центр ======
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // ---------- РАЗРЕШЕНИЕ ----------
            SettingDescription(
                "Разрешение изображения для одного глаза (VR)"
            )

            DropdownSelector(
                label = "Разрешение",
                options = resolutions,
                selected = selectedResolution,
                onSelected = { newRes ->
                    selectedResolution = newRes
                    // Сохраняем через coroutineScope
                    scope.launch{
                        settingsDataStore.saveSettings(
                            selectedResolution.toString(),
                            selectedFps,
                            selectedMarkerSize)
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            // ---------- FPS ----------
            SettingDescription(
                "Частота кадров отображения изображения"
            )

            DropdownSelector(
                label = "FPS",
                options = fpsOptions,
                selected = selectedFps,
                onSelected = { newFps ->
                    selectedFps = newFps
                    scope.launch{
                        settingsDataStore.saveSettings(
                            selectedResolution.toString(),
                            selectedFps,
                            selectedMarkerSize)
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            SettingDescription(
                text = "Размер реального маркера"
            )
            MarkerSizeInput(
                value = markerInput,
                onValueChange = { value ->
                    markerInput = value

                    val parsed = value.toFloatOrNull()
                    if (parsed != null) {
                        selectedMarkerSize = parsed
                    }
                },
                onFocusLost = { value ->
                    saveJob?.cancel()
                    saveJob = scope.launch {
                        delay(300) // даём UI стабилизироваться

                        val parsed = value.toFloatOrNull()
                        if (parsed != null) {
                            settingsDataStore.saveSettings(
                                selectedResolution.toString(),
                                selectedFps,
                                parsed
                            )
                        }
                    }
                }
            )

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        OutlinedButton(
            onClick = {
                navController.navigate("calibration")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color.White)
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.calibrate_svgrepo_com),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Откалибровать камеру",
                fontSize = 18.sp
            )
        }

        // ====== КНОПКА ВЫХОДА (НИЗ) ======
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .fillMaxWidth()
                .height(56.dp)
            ,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Red
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color.Red)
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.exit_svgrepo_com),
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Выйти из аккаунта",
                fontSize = 20.sp)
            }
        }

        // ====== ДИАЛОГ ======
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },

                containerColor = Color(0xFF1E1E2E),

                title = {
                    Text("Выход", color = Color.White)
                },

                text = {
                    Text(
                        "Вы точно хотите выйти из аккаунта?",
                        color = Color.LightGray
                    )
                },

                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                sessionManager.clearSession()

                                navController.navigate("auth") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            },
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color.Red)
                            )
                        ) {
                            Text("Да")
                        }

                        Spacer(Modifier.width(16.dp))

                        OutlinedButton(
                            onClick = { showDialog = false },
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color.White)
                            )
                        ) {
                            Text("Нет")
                        }
                    }
                },
            )
        }
    }
}
