package com.tit.nimonsapp.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.time.Instant

class WebSocketManager(private val client: OkHttpClient) {
    private var webSocket: WebSocket? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {})
    }

    fun sendPresenceUpdate(payload: PresencePayload) {
        val updateDto = UpdatePresenceDto(
            payload = payload,
            timestamp = Instant.now().toString()
        )
        webSocket?.send(json.encodeToString(updateDto))
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnected by user")
        webSocket = null
    }

    fun getOnlineUserIds(): List<Int> {
        // This would normally be tracked via incoming messages
        return emptyList()
    }
}
