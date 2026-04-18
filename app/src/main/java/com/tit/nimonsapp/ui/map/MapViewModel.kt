package com.tit.nimonsapp.ui.map

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.tit.nimonsapp.data.network.GetMeResponseDto
import com.tit.nimonsapp.data.network.WebSocketMessageType
import com.tit.nimonsapp.data.network.WebSocketRepository
import com.tit.nimonsapp.data.repository.AuthRepository
import com.tit.nimonsapp.data.repository.FamilyRepository
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
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
    private val familyRepository = FamilyRepository()

    // Track GPS permission state - only send location updates when granted
    private var hasGpsPermission = false
    private var isLocationSending = false

    // Load current user profile for "My Info" and to get user ID for filtering.
    // Set GPS permission status from Fragment.
    // Dipanggil stlh permission granted to start location updates.
    fun setGpsPermissionGranted(granted: Boolean) {
        hasGpsPermission = granted
        if (granted && !isLocationSending) {
            startLocationUpdates()
        }
    }

    /**
     * Load current user profile for "My Info" and to get user ID for filtering.
     */
    fun loadCurrentUserProfile() {
        executeAuthenticatedLoad(
            isRefresh = false,
            errorMessageFallback = "Failed to load profile",
            loader = { token -> authRepository.getMe(token) },
            onSuccess = { profile ->
                copy(currentUserProfile = profile)
            },
        )
    }

    /**
     * Load all family members from all joined families to whitelist who to show on map.
     */
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
                copy(myFamilyMemberIds = memberIds)
            },
        )
    }

    /**
     * Update current device location in local UI state.
     */
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

    /**
     * Start observing WebSocket messages. Connection management is handled at Activity/Global level.
     */
    fun startObservingWebSocket() {
        viewModelScope.launch {
            // Wait for profile and families to be loaded for filtering
            loadCurrentUserProfile()
            loadFamilyMembers()

            webSocketRepository.observeMessages().collect { message ->
                when (message.type) {
                    WebSocketMessageType.CONNECTED -> {
                        updateState { copy(isSocketConnected = true) }
                        startLocationUpdates()
                    }

                    WebSocketMessageType.MEMBER_PRESENCE_UPDATED -> {
                        handleMemberPresenceUpdated(message.payload)
                    }

                    WebSocketMessageType.DISCONNECTED -> {
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

    /**
     * Periodically send my location to the server via WebSocket.
     * Only sends updates when GPS permission is granted.
     */
    private fun startLocationUpdates() {
        if (isLocationSending) return
        if (!hasGpsPermission) return // Skip if permission denied
        isLocationSending = true

        viewModelScope.launch {
            while (isLocationSending) {
                // Send location every 1 second until stopped or permission revoked
                delay(1000)
                val state = uiState.value
                val location = state.currentLocation
                val userName = state.currentUserProfile?.fullName ?: "Me"

                val payload =
                    JSONObject().apply {
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

    // Process incoming presence data. Filter by family members.
    private fun handleMemberPresenceUpdated(payload: JSONObject?) {
        payload ?: return

        try {
            val userId = payload.optInt("userId", -1)
            if (userId == -1) return

            // Filter: Only show users in my family whitelist
            val currentUserProfile = uiState.value.currentUserProfile
            val isMe = userId == currentUserProfile?.id
            val isFamily = userId in uiState.value.myFamilyMemberIds

            if (!isMe && !isFamily) return

            val userOnMap =
                UserOnMap(
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

    /**
     * Remove users who haven't updated their location in 5 seconds.
     */
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
        updateState {
            copy(searchQuery = query)
        }
    }

    fun getFilteredUsers(): Map<Int, UserOnMap> {
        val query = uiState.value.searchQuery.trim()
        val currentUsers = uiState.value.otherUsers

        if (query.isEmpty()) {
            return currentUsers
        }

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
