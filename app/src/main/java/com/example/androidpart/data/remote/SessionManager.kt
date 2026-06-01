package com.example.androidpart.data.remote

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences(
            "auth_prefs",
            Context.MODE_PRIVATE
        )

    fun saveAccessToken(token: String) {
        prefs.edit()
            .putString("access_token", token)
            .apply()
    }

    fun getAccessToken(): String? =
        prefs.getString("access_token", null)

    fun saveRefreshToken(token: String) {
        prefs.edit()
            .putString("refresh_token", token)
            .apply()
    }

    fun getRefreshToken(): String? =
        prefs.getString("refresh_token", null)

    fun clearSession() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .apply()
    }

    fun isAuthorized(): Boolean =
        getAccessToken() != null
}