package com.tit.nimonsapp.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnlineUser(
    val id: Int,
    val location: UserLocation,
)

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val rotation: Double,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String,
)

class WebSocketRepository(
    private val manager: WebSocketManager,
) {
    private val _onlineUsers = MutableStateFlow<Map<Int, UserLocation>>(emptyMap())

    val onlineUsers: StateFlow<Map<Int, UserLocation>> = _onlineUsers.asStateFlow()

    fun connect(token: String) {
        val url = "wss://mad.labpro.hmif.dev/ws/live"
        manager.connect(url, token)
    }

    fun observeMessages(): Flow<WebSocketMessage> = manager.messages

    fun sendMyLocation(location: UserLocation) {
        val payload =
            PresencePayload(
                name = "",
                latitude = location.latitude,
                longitude = location.longitude,
                rotation = location.rotation,
                batteryLevel = location.batteryLevel,
                isCharging = location.isCharging,
                internetStatus = if (location.internetStatus == "wifi") "wifi" else "mobile",
                metadata = emptyMap(),
            )
        manager.sendPresenceUpdate(payload)
    }

    fun sendLocationUpdate(payload: org.json.JSONObject) {
        val presencePayload =
            PresencePayload(
                name = payload.optString("name", ""),
                latitude = payload.optDouble("latitude", 0.0),
                longitude = payload.optDouble("longitude", 0.0),
                rotation = payload.optDouble("rotation", 0.0),
                batteryLevel = payload.optInt("batteryLevel", 0),
                isCharging = payload.optBoolean("isCharging", false),
                internetStatus = if (payload.optString("internetStatus", "mobile") == "wifi") "wifi" else "mobile",
                metadata = emptyMap(),
            )
        manager.sendPresenceUpdate(presencePayload)
    }

    fun disconnect() {
        manager.disconnect()
    }

    fun getOnlineUserIds(): List<Int> = manager.getOnlineUserIds()
}
