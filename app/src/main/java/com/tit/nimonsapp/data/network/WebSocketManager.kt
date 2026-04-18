package com.tit.nimonsapp.data.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class WebSocketManager(
    private val client: OkHttpClient,
) {
    private var webSocket: WebSocket? = null
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    private val _messages = Channel<WebSocketMessage>(Channel.UNLIMITED)
    val messages: Flow<WebSocketMessage> = _messages.receiveAsFlow()

    private val onlineUsers = ConcurrentHashMap<Int, MemberPresencePayload>()

    private var reconnectAttempts = 0
    private var reconnectDelay = 1000L
    private var shouldReconnect = false
    private var currentUrl: String? = null
    private var currentToken: String? = null
    private val connectScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun connect(
        url: String,
        token: String? = null,
    ) {
        currentUrl = url
        currentToken = token
        shouldReconnect = true
        reconnectAttempts = 0
        reconnectDelay = 1000L

        Log.d("NIMONS_WS_RAW", "connect() called url=$url hasToken=${!token.isNullOrBlank()}")

        val requestBuilder = Request.Builder().url(url)
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()
        webSocket = client.newWebSocket(request, createWebSocketListener())
    }

    private fun attemptReconnect() {
        if (!shouldReconnect || reconnectAttempts >= 5) {
            Log.d("NIMONS_WS_RAW", "Reconnect stopped: attempts=$reconnectAttempts, shouldReconnect=$shouldReconnect")
            return
        }

        Log.d("NIMONS_WS_RAW", "Attempting reconnect #$reconnectAttempts after ${reconnectDelay}ms")
        connectScope.launch {
            delay(reconnectDelay)
            currentUrl?.let { url ->
                currentToken?.let { token ->
                    connect(url, token)
                }
            }
            reconnectAttempts++
            reconnectDelay = (reconnectDelay * 2).coerceAtMost(30000L) // Max 30 seconds
        }
    }

    private fun createWebSocketListener(): WebSocketListener =
        object : WebSocketListener() {
            override fun onOpen(
                ws: WebSocket,
                response: Response,
            ) {
                Log.d("NIMONS_WS_RAW", "onOpen code=${response.code}")
                reconnectAttempts = 0
                reconnectDelay = 1000L
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
                Log.d("NIMONS_WS_RAW", "onMessage: $text")

                try {
                    val jsonObject = JSONObject(text)
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

                            payload?.let {
                                val userId = it.optInt("userId", -1)
                                if (userId != -1) {
                                    onlineUsers[userId] =
                                        MemberPresencePayload(
                                            userId = userId,
                                            email = it.optString("email", ""),
                                            fullName = it.optString("fullName", ""),
                                            latitude = it.optDouble("latitude", 0.0),
                                            longitude = it.optDouble("longitude", 0.0),
                                            rotation = it.optDouble("rotation", 0.0),
                                            batteryLevel = it.optInt("batteryLevel", 0),
                                            isCharging = it.optBoolean("isCharging", false),
                                            internetStatus = it.optString("internetStatus", "mobile"),
                                            metadata = emptyMap(),
                                        )
                                }
                            }

                            _messages.trySend(
                                WebSocketMessage(
                                    type = WebSocketMessageType.MEMBER_PRESENCE_UPDATED,
                                    payload = payload,
                                    errorMessage = null,
                                ),
                            )
                        }

                        "error" -> {
                            _messages.trySend(
                                WebSocketMessage(
                                    type = WebSocketMessageType.ERROR,
                                    payload = jsonObject.optJSONObject("payload"),
                                    errorMessage = jsonObject.optJSONObject("payload")?.optString("message"),
                                ),
                            )
                        }

                        else -> {
                            Log.d("NIMONS_WS_RAW", "ignored message type=$type")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NIMONS_WS_RAW", "onMessage parse failed", e)
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
                Log.d("NIMONS_WS_RAW", "onClosing code=$code reason=$reason")
                _messages.trySend(
                    WebSocketMessage(
                        type = WebSocketMessageType.DISCONNECTED,
                        payload = null,
                        errorMessage = null,
                    ),
                )
            }

            override fun onClosed(
                ws: WebSocket,
                code: Int,
                reason: String,
            ) {
                Log.d("NIMONS_WS_RAW", "onClosed code=$code reason=$reason")
            }

            override fun onFailure(
                ws: WebSocket,
                t: Throwable,
                response: Response?,
            ) {
                Log.e(
                    "NIMONS_WS_RAW",
                    "onFailure code=${response?.code} message=${t.message}",
                    t,
                )
                _messages.trySend(
                    WebSocketMessage(
                        type = WebSocketMessageType.ERROR,
                        payload = null,
                        errorMessage = t.message,
                    ),
                )
                attemptReconnect()
            }
        }

    fun sendPresenceUpdate(payload: PresencePayload) {
        val updateDto =
            UpdatePresenceDto(
                payload = payload,
                timestamp = Instant.now().toString(),
            )
        val jsonText = json.encodeToString(updateDto)
        Log.d("NIMONS_WS_RAW", "sendPresenceUpdate: $jsonText")
        webSocket?.send(jsonText)
    }

    fun disconnect() {
        Log.d("NIMONS_WS_RAW", "disconnect() called")
        shouldReconnect = false
        reconnectAttempts = 0
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
