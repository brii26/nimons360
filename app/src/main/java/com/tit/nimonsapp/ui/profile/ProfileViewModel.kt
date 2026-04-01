package com.tit.nimonsapp.ui.profile

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.tit.nimonsapp.data.repository.AuthRepository
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application,
) : AuthenticatedRefreshableViewModel<ProfileUiState>(application, ProfileUiState()) {
    private val authRepository = AuthRepository()

    override fun ProfileUiState.withMeta(meta: UiResourceMeta): ProfileUiState = copy(meta = meta)

    override fun ProfileUiState.withRefreshing(isRefreshing: Boolean): ProfileUiState = copy(isRefreshing = isRefreshing)

    override fun ProfileUiState.onMissingSession(): ProfileUiState =
        copy(
            meta = UiResourceMeta(errorMessage = "No active session"),
            isLoggedOut = true,
            isRefreshing = false,
        )

    fun loadProfile() {
        loadProfile(isRefresh = false)
    }

    override fun refresh() {
        loadProfile(isRefresh = true)
    }

    private fun loadProfile(isRefresh: Boolean) {
        executeAuthenticatedLoad(
            isRefresh = isRefresh,
            errorMessageFallback = "Failed to load profile",
            loader = { token -> authRepository.getMe(token) },
            onSuccess = { profile -> copy(profile = profile) },
        )
    }

    fun updateFullName(fullName: String) {
        if (fullName.isBlank()) {
            updateState {
                withMeta(meta.copy(errorMessage = "Full name must not be empty"))
            }
            return
        }

        executeAuthenticatedAction(
            errorMessageFallback = "Failed to update profile",
            onStart = {
                withMeta(UiResourceMeta(isLoading = true, errorMessage = null))
            },
            action = { token -> authRepository.updateMe(token, fullName.trim()) },
            onSuccess = { updated ->
                copy(
                    profile =
                        profile?.copy(
                            fullName = updated.fullName,
                            updatedAt = updated.updatedAt,
                        ),
                )
            },
        )
    }

    fun logout() {
        viewModelScope.launch {
            sessionRepository.clearToken()
            updateState {
                copy(isLoggedOut = true)
            }
        }
    }

    fun consumeLogout() {
        updateState {
            copy(isLoggedOut = false)
        }
    }
}
