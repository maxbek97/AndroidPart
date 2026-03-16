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
import com.example.androidpart.data.remote.RetrofitClient
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController


@Composable
fun AuthScreen(navController: NavHostController) {

    // 1. **ПОДГОТОВКА ViewModel**

    // **Создаем необходимые зависимости (Репозиторий)**
    val repository = remember { AuthRepository(RetrofitClient.apiService) }

    // **Создаем ViewModel, используя фабрику для передачи repository**
    val viewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    var topMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isTopMessageVisible by remember { mutableStateOf(false) }

    // **Собираем состояние из ViewModel (AuthState)**
    val authState by viewModel.uiState.collectAsState()

    var isLogin by remember { mutableStateOf(true) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }


    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val loginFocus = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111845))
    ) {


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
            verticalArrangement = Arrangement.Top
        ) {

            // ---------- АНИМАЦИЯ ПОКАЗА/СКРЫТИЯ ЛОГОТИПА ----------
            AnimatedVisibility(
                visible = isLogin,
                enter = fadeIn(tween(300)) + slideInVertically { -80 },
                exit = fadeOut(tween(300)) + slideOutVertically { -80 }
            ) {
//                Image(
//                    painter = painterResource(R.drawable.logo),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(140.dp)
//                        .padding(bottom = 20.dp)
//                )
            }

            // ---------- КАРТОЧКА ----------
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(0.9f)
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
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

                    // ---------- АНИМАЦИЯ МЕЖДУ ФОРМАМИ ----------
                    // ---------- АНИМАЦИЯ СМЕНЫ ФОРМ ----------
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
                enabled = authState != AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                if (authState == AuthUiState.Loading) {
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
            LaunchedEffect(authState) {
                when (val state = authState) {
                    is AuthUiState.Error -> {
                        topMessage = state.message
                        isError = true
                        isTopMessageVisible = true
                    }

                    is AuthUiState.LoginSuccess -> {
                        topMessage = "С возвращением!"
                        isError = false
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