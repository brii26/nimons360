package com.tit.nimonsapp.data.network

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class WebSocketManager(
    private val client: OkHttpClient,
) {
    private var webSocket: WebSocket? = null
    private val json = Json { ignoreUnknownKeys = true }
    private val _messages = Channel<WebSocketMessage>(Channel.UNLIMITED)
    val messages: Flow<WebSocketMessage> = _messages.receiveAsFlow()

    private val onlineUsers = ConcurrentHashMap<Int, MemberPresencePayload>()

    fun connect(
        url: String,
        token: String? = null,
    ) {
        val requestBuilder = Request.Builder().url(url)
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()
        webSocket = client.newWebSocket(request, createWebSocketListener())
    }

    private fun createWebSocketListener(): WebSocketListener =
        object : WebSocketListener() {
            override fun onOpen(
                ws: WebSocket,
                response: okhttp3.Response,
            ) {
                _messages.trySend(
                    WebSocketMessage(
                        type = WebSocketMessageType.CONNECTED,
                        payload = null,
                        errorMessage = null,
                    ),
                )
            }

            override fun onMessage(
                ws: WebSocket,
                text: String,
            ) {
                try {
                    val jsonObject = org.json.JSONObject(text)
                    val type = jsonObject.optString("type", "")

                    when (type) {
                        "pong" -> {
                            _messages.trySend(
                                WebSocketMessage(
                                    type = WebSocketMessageType.PONG,
                                    payload = null,
                                    errorMessage = null,
                                ),
                            )
                        }

                        "member_presence_updated" -> {
                            val payload = jsonObject.optJSONObject("payload")
                            _messages.trySend(
                                WebSocketMessage(
                                    type = WebSocketMessageType.MEMBER_PRESENCE_UPDATED,
                                    payload = payload,
                                    errorMessage = null,
                                ),
                            )
                        }
                    }
                } catch (e: Exception) {
                    _messages.trySend(
                        WebSocketMessage(
                            type = WebSocketMessageType.ERROR,
                            payload = null,
                            errorMessage = e.message,
                        ),
                    )
                }
            }

            override fun onClosing(
                ws: WebSocket,
                code: Int,
                reason: String,
            ) {
                _messages.trySend(
                    WebSocketMessage(
                        type = WebSocketMessageType.DISCONNECTED,
                        payload = null,
                        errorMessage = null,
                    ),
                )
            }

            override fun onFailure(
                ws: WebSocket,
                t: Throwable,
                response: okhttp3.Response?,
            ) {
                _messages.trySend(
                    WebSocketMessage(
                        type = WebSocketMessageType.ERROR,
                        payload = null,
                        errorMessage = t.message,
                    ),
                )
            }
        }

    fun sendPresenceUpdate(payload: PresencePayload) {
        val updateDto =
            UpdatePresenceDto(
                payload = payload,
                timestamp = Instant.now().toString(),
            )
        webSocket?.send(json.encodeToString(updateDto))
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnected by user")
        webSocket = null
    }

    fun getOnlineUserIds(): List<Int> = onlineUsers.keys.toList()
}

enum class WebSocketMessageType {
    CONNECTED,
    DISCONNECTED,
    PONG,
    MEMBER_PRESENCE_UPDATED,
    ERROR,
}

data class WebSocketMessage(
    val type: WebSocketMessageType,
    val payload: JSONObject?,
    val errorMessage: String?,
)
