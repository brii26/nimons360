package com.tit.nimonsapp.ui.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.tit.nimonsapp.data.network.WebSocketMessageType
import com.tit.nimonsapp.data.network.WebSocketRepository
import com.tit.nimonsapp.data.repository.AuthRepository
import com.tit.nimonsapp.data.repository.FamilyRepository
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class MapViewModel(
    private val application: Application,
    private val webSocketRepository: WebSocketRepository,
) : AuthenticatedRefreshableViewModel<MapUiState>(application, MapUiState()) {
    override fun MapUiState.withMeta(meta: UiResourceMeta): MapUiState = copy(meta = meta)

    override fun MapUiState.withRefreshing(isRefreshing: Boolean): MapUiState = copy(isRefreshing = isRefreshing)

    private val authRepository = AuthRepository()
    private val familyRepository = FamilyRepository()

    private var hasGpsPermission = false
    private var isLocationSending = false
    private var isObservingSocket = false

    fun connectWebSocket(token: String) {
        Log.d("NIMONS_WS_RAW", "CALLING CONNECT")
        webSocketRepository.connect(token)
    }

    fun setGpsPermissionGranted(granted: Boolean) {
        hasGpsPermission = granted
        if (granted && uiState.value.isSocketConnected && !isLocationSending) {
            startLocationUpdates()
        }
    }

    fun loadCurrentUserProfile() {
        executeAuthenticatedLoad(
            isRefresh = false,
            errorMessageFallback = "Failed to load profile",
            loader = { token -> authRepository.getMe(token) },
            onSuccess = { profile ->
                Log.d(
                    "NIMONS_WS",
                    "profile loaded id=${profile.id} email=${profile.email} fullName=${profile.fullName}",
                )
                copy(currentUserProfile = profile)
            },
        )
    }

    fun loadFamilyMembers() {
        executeAuthenticatedLoad(
            isRefresh = false,
            errorMessageFallback = "Failed to load families",
            loader = { token -> familyRepository.getMyFamilies(token) },
            onSuccess = { families ->
                val memberIds =
                    families
                        .flatMap { family ->
                            family.members.mapNotNull { it.id }
                        }.toSet()
                Log.d("NIMONS_WS", "family whitelist=$memberIds")
                copy(myFamilyMemberIds = memberIds)
            },
        )
    }

    fun updateCurrentLocation(
        latitude: Double,
        longitude: Double,
        rotation: Double,
        batteryLevel: Int,
        isCharging: Boolean,
        internetStatus: String,
    ) {
        updateState {
            copy(
                currentLocation =
                    currentLocation.copy(
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

    fun startObservingWebSocket() {
        if (isObservingSocket) return
        isObservingSocket = true

        viewModelScope.launch {
            webSocketRepository.observeMessages().collect { message ->
                when (message.type) {
                    WebSocketMessageType.CONNECTED -> {
                        Log.d("NIMONS_WS", "socket connected")
                        updateState { copy(isSocketConnected = true) }
                        if (hasGpsPermission) startLocationUpdates()
                    }

                    WebSocketMessageType.MEMBER_PRESENCE_UPDATED -> {
                        Log.d("NIMONS_WS", "presence received: ${message.payload}")
                        handleMemberPresenceUpdated(message.payload)
                    }

                    WebSocketMessageType.DISCONNECTED -> {
                        Log.d("NIMONS_WS", "socket disconnected")
                        updateState { copy(isSocketConnected = false) }
                        isLocationSending = false
                    }

                    else -> {
                        Unit
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (isLocationSending) return
        if (!hasGpsPermission) return
        isLocationSending = true

        viewModelScope.launch {
            while (isLocationSending) {
                val state = uiState.value
                val location = state.currentLocation

                if (location.latitude == 0.0 || location.longitude == 0.0) {
                    Log.d("NIMONS_WS", "skip send: invalid coordinate ${location.latitude}, ${location.longitude}")
                    delay(1000)
                    continue
                }

                val normalizedInternetStatus =
                    if (location.internetStatus == "wifi") "wifi" else "mobile"

                val payload =
                    JSONObject().apply {
                        put("name", state.currentUserProfile?.fullName ?: "Me")
                        put("latitude", location.latitude)
                        put("longitude", location.longitude)
                        put("rotation", location.rotation)
                        put("batteryLevel", location.batteryLevel)
                        put("isCharging", location.isCharging)
                        put("internetStatus", normalizedInternetStatus)
                        put("metadata", JSONObject())
                    }

                Log.d("NIMONS_WS", "sending presence: $payload")
                webSocketRepository.sendLocationUpdate(payload)

                delay(1000)
            }
        }
    }

    private fun handleMemberPresenceUpdated(payload: JSONObject?) {
        payload ?: return

        try {
            val userId = payload.optInt("userId", -1)
            Log.d("NIMONS_WS", "presence received userId=$userId payload=$payload")
            if (userId == -1) return

            if (userId !in uiState.value.myFamilyMemberIds) {
            val latitude = payload.optDouble("latitude", 0.0)
            val longitude = payload.optDouble("longitude", 0.0)
            if (latitude == 0.0 || longitude == 0.0) {
                Log.d("NIMONS_WS", "drop received presence with invalid coordinate for userId=$userId")
                return
            }

            val userOnMap =
                UserOnMap(
                    userId = userId,
                    fullName = payload.optString("fullName", "Unknown"),
                    email = payload.optString("email", ""),
                    latitude = latitude,
                    longitude = longitude,
                    rotation = payload.optDouble("rotation", 0.0),
                    batteryLevel = payload.optInt("batteryLevel", 0),
                    isCharging = payload.optBoolean("isCharging", false),
                    internetStatus = payload.optString("internetStatus", "mobile"),
                    lastUpdateTimestamp = System.currentTimeMillis(),
                )

            updateState {
                copy(otherUsers = otherUsers + (userId to userOnMap))
            }
        } catch (e: Exception) {
            Log.e("NIMONS_WS", "failed to parse presence", e)
        }
    }

    fun selectUser(userId: Int?) {
        updateState { copy(selectedUserId = userId) }
    }

    fun removeOfflineUsers() {
        val now = System.currentTimeMillis()
        val offlineTimeout = 5000L

        updateState {
            copy(
                otherUsers =
                    otherUsers.filter { (_, user) ->
                        now - user.lastUpdateTimestamp < offlineTimeout
                    },
            )
        }
    }

    fun updateSearchQuery(query: String) {
        updateState { copy(searchQuery = query) }
    }

    fun getFilteredUsers(): Map<Int, UserOnMap> {
        val query = uiState.value.searchQuery.trim()
        val currentUsers = uiState.value.otherUsers

        if (query.isEmpty()) return currentUsers

        return currentUsers.filter { (_, user) ->
            user.fullName.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true)
        }
    }

    override fun refresh() {
        loadCurrentUserProfile()
        loadFamilyMembers()
    }

    override fun onCleared() {
        super.onCleared()
        isLocationSending = false
    }
}
