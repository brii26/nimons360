package com.tit.nimonsapp.ui.common

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.tit.nimonsapp.data.repository.SessionRepository
import kotlinx.coroutines.launch

abstract class AuthenticatedRefreshableViewModel<S : RefreshableStatefulUi>(
    application: Application,
    initialState: S,
) : BaseStateViewModel<S>(application, initialState),
    Refreshable {
    protected val sessionRepository = SessionRepository(application)

    protected abstract fun S.withRefreshing(isRefreshing: Boolean): S

    protected open fun S.onMissingSession(): S = withMeta(UiResourceMeta(errorMessage = "No active session")).withRefreshing(false)

    protected fun <T> executeAuthenticatedLoad(
        isRefresh: Boolean,
        errorMessageFallback: String,
        loader: suspend (String) -> T,
        onSuccess: S.(T) -> S,
    ) {
        viewModelScope.launch {
            updateState {
                if (isRefresh) {
                    withMeta(meta.copy(errorMessage = null)).withRefreshing(true)
                } else {
                    withMeta(UiResourceMeta(isLoading = true, errorMessage = null)).withRefreshing(false)
                }
            }

            val token = sessionRepository.getToken()
            if (token == null) {
                updateState { onMissingSession() }
                return@launch
            }

            runCatching {
                loader(token)
            }.onSuccess { data ->
                updateState {
                    onSuccess(data)
                        .withMeta(UiResourceMeta())
                        .withRefreshing(false)
                }
            }.onFailure { throwable ->
                updateState {
                    withMeta(
                        UiResourceMeta(
                            errorMessage = throwable.message ?: errorMessageFallback,
                        ),
                    ).withRefreshing(false)
                }
            }
        }
    }

    protected fun <T> executeAuthenticatedAction(
        errorMessageFallback: String,
        onStart: S.() -> S = { this },
        action: suspend (String) -> T,
        onSuccess: S.(T) -> S,
        afterSuccess: suspend (T) -> Unit = {},
    ) {
        viewModelScope.launch {
            updateState { onStart() }

            val token = sessionRepository.getToken()
            if (token == null) {
                updateState { onMissingSession() }
                return@launch
            }

            runCatching {
                action(token)
            }.onSuccess { data ->
                updateState {
                    onSuccess(data).withMeta(UiResourceMeta())
                }
                afterSuccess(data)
            }.onFailure { throwable ->
                updateState {
                    withMeta(
                        UiResourceMeta(
                            errorMessage = throwable.message ?: errorMessageFallback,
                        ),
                    )
                }
            }
        }
    }
}
