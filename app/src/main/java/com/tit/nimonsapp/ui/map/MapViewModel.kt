package com.tit.nimonsapp.ui.map

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.tit.nimonsapp.data.network.WebSocketRepository
import com.tit.nimonsapp.data.network.WebSocketMessageType
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
import com.tit.nimonsapp.data.network.GetMeResponseDto
import com.tit.nimonsapp.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject

class MapViewModel(
    private val application: Application,
    private val webSocketRepository: WebSocketRepository,
) : AuthenticatedRefreshableViewModel<MapUiState>(application, MapUiState()) {
    override fun MapUiState.withMeta(meta: UiResourceMeta): MapUiState = copy(meta = meta)
    override fun MapUiState.withRefreshing(isRefreshing: Boolean): MapUiState = copy(isRefreshing = isRefreshing)

    private val authRepository = AuthRepository()

    private var isLocationSending = false

    fun loadCurrentUserProfile() {
        executeAuthenticatedLoad(
            isRefresh = false,
            errorMessageFallback = "Failed to load profile",
            loader = { token -> authRepository.getMe(token) },
            onSuccess = { profile -> 
                copy(currentUserProfile = profile)
            }
        )
    }

    fun updateCurrentLocation(latitude: Double, longitude: Double, rotation: Double, batteryLevel: Int, isCharging: Boolean, internetStatus: String) {
        updateState {
            copy(
                currentLocation = currentLocation.copy(
                    latitude = latitude,
                    longitude = longitude,
                    rotation = rotation,
                    batteryLevel = batteryLevel,
                    isCharging = isCharging,
                    internetStatus = internetStatus,
                ),
            )
        }
    }

    fun connectWebSocket() {
        viewModelScope.launch {
            val token = sessionRepository.getToken()
            if (token == null) {
                updateState { onMissingSession() }
                return@launch
            }

            updateState {
                withMeta(UiResourceMeta(isLoading = true, errorMessage = null))
            }

            webSocketRepository.connect(token)
            loadCurrentUserProfile()

            webSocketRepository.observeMessages().collect { message ->
                when (message.type) {
                    WebSocketMessageType.CONNECTED -> {
                        updateState {
                            copy(
                                meta = UiResourceMeta(),
                                isSocketConnected = true,
                            )
                        }
                        startLocationUpdates()
                    }
                    WebSocketMessageType.MEMBER_PRESENCE_UPDATED -> {
                        handleMemberPresenceUpdated(message.payload)
                    }
                    WebSocketMessageType.DISCONNECTED -> {
                        updateState {
                            copy(
                                meta = UiResourceMeta(errorMessage = "WebSocket disconnected"),
                                isSocketConnected = false,
                            )
                        }
                        isLocationSending = false
                    }
                    WebSocketMessageType.ERROR -> {
                        updateState {
                            copy(
                                meta = UiResourceMeta(errorMessage = message.errorMessage),
                                isSocketConnected = false,
                            )
                        }
                        isLocationSending = false
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (isLocationSending) return
        isLocationSending = true

        viewModelScope.launch {
            while (uiState.value.isSocketConnected && isLocationSending) {
                val location = uiState.value.currentLocation
                val userName = uiState.value.currentUserProfile?.fullName ?: "Me"

                val payload = JSONObject().apply {
                    put("name", userName)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("rotation", location.rotation)
                    put("batteryLevel", location.batteryLevel)
                    put("isCharging", location.isCharging)
                    put("internetStatus", location.internetStatus)
                    put("metadata", JSONObject())
                }

                webSocketRepository.sendLocationUpdate(payload)
                delay(1000)
            }
        }
    }

    private fun handleMemberPresenceUpdated(payload: JSONObject?) {
        payload ?: return

        try {
            val userId = payload.optInt("userId", -1)
            if (userId == -1) return

            val userOnMap = UserOnMap(
                userId = userId,
                fullName = payload.optString("fullName", "Unknown"),
                email = payload.optString("email", ""),
                latitude = payload.optDouble("latitude", 0.0),
                longitude = payload.optDouble("longitude", 0.0),
                rotation = payload.optDouble("rotation", 0.0),
                batteryLevel = payload.optInt("batteryLevel", 0),
                isCharging = payload.optBoolean("isCharging", false),
                internetStatus = payload.optString("internetStatus", "unknown"),
                lastUpdateTimestamp = System.currentTimeMillis(),
            )

            updateState {
                copy(otherUsers = otherUsers + (userId to userOnMap))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectUser(userId: Int?) {
        updateState {
            copy(selectedUserId = userId)
        }
    }

    fun removeOfflineUsers() {
        val now = System.currentTimeMillis()
        val offlineTimeout = 5000L

        updateState {
            copy(
                otherUsers = otherUsers.filter { (_, user) ->
                    now - user.lastUpdateTimestamp < offlineTimeout
                },
            )
        }
    }

    fun disconnectWebSocket() {
        isLocationSending = false
        webSocketRepository.disconnect()
        updateState {
            copy(
                meta = UiResourceMeta(),
                isSocketConnected = false,
                otherUsers = emptyMap(),
            )
        }
    }

    fun updateSearchQuery(query: String) {
        updateState {
            copy(searchQuery = query)
        }
    }

    fun getFilteredUsers(): Map<Int, UserOnMap> {
        val query = uiState.value.searchQuery.trim()
        if (query.isEmpty()) {
            return uiState.value.otherUsers
        }

        return uiState.value.otherUsers.filter { (_, user) ->
            user.fullName.contains(query, ignoreCase = true) ||
            user.email.contains(query, ignoreCase = true)
        }
    }

    override fun refresh() {
        loadCurrentUserProfile()
    }
}
