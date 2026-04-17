package com.tit.nimonsapp.ui.map

import com.tit.nimonsapp.data.repository.DeviceLocation
import com.tit.nimonsapp.ui.common.StatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class MapUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    val isSocketConnected: Boolean = false,
    val currentLocation: DeviceLocation = DeviceLocation(),
    val otherUsers: Map<Int, UserOnMap> = emptyMap(),
    val selectedUserId: Int? = null,
) : StatefulUi

data class UserOnMap(
    val userId: Int,
    val fullName: String,
    val email: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Double,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String,
    val lastUpdateTimestamp: Long,
)
