package com.example.androidpart.core.math

import android.util.Log

fun parseStringMatrix(input: String): List<List<Double>> {
    return try {
        input.split(';')
            .filter { it.isNotBlank() }
            .map { row ->
                row.split(',')
                    .filter { it.isNotBlank() }
                    .map { it.trim().toDouble() }
            }
    } catch (e: Exception) {
        Log.e("VM_DEBUG", "Matrix parse error"); emptyList()
    }
}

fun parseStringList(str: String): List<Double> {
    return try {
        // Разделение по запятой, по точке с запятой и по пробелу
        str.replace("[", "").replace("]", "")
            .split(Regex("[;,\\s]+"))
            .filter { it.isNotBlank() }
            .map { it.trim().toDouble() }
    } catch (e: Exception) {
        Log.e("VM_DEBUG", "List parse error in: $str"); emptyList()
    }
}