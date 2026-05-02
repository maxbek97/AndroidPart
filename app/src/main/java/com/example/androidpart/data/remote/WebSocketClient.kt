package com.example.androidpart.data.remote


import android.util.Log
import com.example.androidpart.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WsClient {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(onMessage: (String) -> Unit) {
        val request = Request.Builder()
            .url(BuildConfig.WS_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }
        })
    }

    fun send(text: String) {
        webSocket?.send(text)
    }
    // Добавляем этот метод
    fun disconnect() {
        Log.d("WS_DEBUG", "Disconnecting WebSocket...")
        webSocket?.close(1000, "Screen closed") // 1000 - нормальное закрытие
        webSocket = null
    }
}