package com.example.androidpart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SegmentedSwitch(
    option1: String,
    option2: String,
    selectedFirst: Boolean,
    onSelect: (Boolean) -> Unit
) {
    val bg = Color(0xFF1F1F1F)
    val selectedColor = Color(0xFF3F51B5)
    val textColor = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(color = Color(0xFF111845), RoundedCornerShape(50))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    if (selectedFirst) selectedColor else Color.Transparent,
                    RoundedCornerShape(50)
                )
                .padding(vertical = 10.dp)
                .clickable { onSelect(true) },
            contentAlignment = Alignment.Center
        ) {
            Text(option1, color = textColor, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    if (!selectedFirst) selectedColor else Color.Transparent,
                    RoundedCornerShape(40)
                )
                .padding(vertical = 10.dp)
                .clickable { onSelect(false) },
            contentAlignment = Alignment.Center
        ) {
            Text(option2, color = textColor, fontSize = 12.sp)
        }
    }
}
