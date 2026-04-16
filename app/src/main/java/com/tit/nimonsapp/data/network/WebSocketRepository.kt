package com.tit.nimonsapp.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnlineUser(val id: Int, val location: UserLocation)

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val rotation: Double,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String
)

class WebSocketRepository(
    private val manager: WebSocketManager
) {
    private val _onlineUsers = MutableStateFlow<Map<Int, UserLocation>>(emptyMap())

    val onlineUsers: StateFlow<Map<Int, UserLocation>> = _onlineUsers.asStateFlow()

    fun connect(url: String) {
        manager.connect(url)
    }

    fun sendMyLocation(location: UserLocation) {
        val payload = PresencePayload(
            name = "",
            latitude = location.latitude,
            longitude = location.longitude,
            rotation = location.rotation,
            batteryLevel = location.batteryLevel,
            isCharging = location.isCharging,
            internetStatus = location.internetStatus,
            metadata = emptyMap()
        )
        manager.sendPresenceUpdate(payload)
    }

    fun disconnect() {
        manager.disconnect()
    }

    fun getOnlineUserIds(): List<Int> {
        return manager.getOnlineUserIds()
    }
}
