package com.example.androidpart.data.remote

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
    }

    fun getToken(): String? = prefs.getString("access_token", null)

    fun clear() {
        prefs.edit().remove("access_token").apply()
    }

    fun isAuthorized(): Boolean = getToken() != null
}
