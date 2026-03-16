package com.example.androidpart.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    isLast: Boolean = false
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Row {
                Text(label, color = Color.Black)
                if (required) Text("*", color = Color.Red)
            }
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth(0.85f)
            .let { mod ->
                if (focusRequester != null) mod.focusRequester(focusRequester)
                else mod
            },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            cursorColor = Color.Black
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                nextFocusRequester?.requestFocus()
            },
            onDone = {
                focusManager.clearFocus()      // 🔥 снимаем фокус
                keyboardController?.hide()     // 🔥 скрываем клавиатуру
            }
        )
    )
}

