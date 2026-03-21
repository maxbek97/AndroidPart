package com.example.androidpart.ui.screens.AuthScreen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidpart.ui.components.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.androidpart.data.remote.AuthRepository
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.androidpart.data.remote.SessionManager

@androidx.media3.common.util.UnstableApi
@Composable
fun AuthScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val repository = AuthRepository.create(sessionManager)

    val viewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(repository, sessionManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    var topMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isTopMessageVisible by remember { mutableStateOf(false) }

    var isLogin by remember { mutableStateOf(true) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.checkAuthorization()
    }

    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val loginFocus = remember { FocusRequester() }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111845))
    ) {

        VideoBackground()

        // затемнение
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        TopMessageBar(
            message = topMessage,
            isError = isError,
            visible = isTopMessageVisible,
            onAutoHide = { isTopMessageVisible = false },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                .zIndex(1f),
            verticalArrangement = Arrangement.Center
        ) {

            // ---------- АНИМАЦИЯ ПОКАЗА/СКРЫТИЯ ЛОГОТИПА ----------
            AnimatedVisibility(
                visible = isLogin,
                enter = fadeIn(tween(300)) + slideInVertically { -80 },
                exit = fadeOut(tween(300)) + slideOutVertically { -80 }
            ) {

            }
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
            // ---------- КАРТОЧКА ----------
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(0.9f)
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.75f),

                tonalElevation = 6.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {

                    // ---------- ЗАГОЛОВОК ----------
                    Text(
                        text = if (isLogin) "Авторизация" else "Регистрация",
                        color = Color(0xFF2139d1
                        ),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(20.dp))

                    SegmentedSwitch(
                        option1 = "Войти",
                        option2 = "Регистрация",
                        selectedFirst = isLogin,
                        onSelect = { isLogin = it }
                    )

                    Spacer(Modifier.height(20.dp))

                    AnimatedContent(
                        targetState = isLogin,
                        label = "auth_animation",
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) { loginMode ->

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),   // ← ← ← ВАЖНО
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            if (loginMode) {
                                AuthField(
                                    label = "email",
                                    value = email,
                                    onValueChange = { email = it },
                                    required = true,
                                    focusRequester = emailFocus,
                                    nextFocusRequester = passwordFocus
                                )

                                AuthField(
                                    label = "Пароль",
                                    value = password,
                                    onValueChange = { password = it },
                                    required = true,
                                    focusRequester = passwordFocus,
                                    isLast = true
                                )

                            } else {
                                AuthField(
                                    label = "Логин",
                                    value = login,
                                    onValueChange = { login = it },
                                    required = true,
                                    focusRequester = loginFocus,
                                    nextFocusRequester = emailFocus
                                )

                                AuthField(
                                    label = "email",
                                    value = email,
                                    onValueChange = { email = it },
                                    required = true,
                                    focusRequester = emailFocus,
                                    nextFocusRequester = passwordFocus
                                )

                                AuthField(
                                    label = "Пароль",
                                    value = password,
                                    onValueChange = { password = it },
                                    required = true,
                                    focusRequester = passwordFocus,
                                    isLast = true
                                )
                            }
                        }
                    }

                }
            }
            Spacer(Modifier.height(26.dp))

            Button(
                onClick = {
                    if (isLogin) {
                        viewModel.login(email, password)
                    } else {
                        viewModel.register(login, password, email)
                    }
                },
                enabled = uiState != AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5).copy(alpha = 0.75f))
            ) {
                if (uiState == AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (isLogin) "Войти" else "Зарегистрироваться",
                        fontSize = 18.sp
                    )
                }
            }
            // 3. **ОБРАБОТКА СОСТОЯНИЯ UI (ОШИБКИ/УСПЕХ)**
            LaunchedEffect(true) {
                viewModel.events.collect { event ->
                    when (event) {

                        is AuthEvent.NavigateToMenu -> {
                            navController.navigate("menu") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }

                        is AuthEvent.NavigateToError -> {
                            navController.navigate("error/server")
                        }
                    }
                }
            }
            LaunchedEffect(uiState) {
                when (val state = uiState) {

                    is AuthUiState.Error -> {
                        topMessage = state.message
                        isError = true
                        isTopMessageVisible = true
                    }

                    is AuthUiState.RegistrationSuccess -> {
                        topMessage = "Регистрация прошла успешно"
                        isError = false
                        isTopMessageVisible = true
                    }

                    else -> Unit
                }
            }

        }
    }
}