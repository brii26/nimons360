package com.tit.nimonsapp.ui.login

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.tit.nimonsapp.data.repository.AuthRepository
import com.tit.nimonsapp.data.repository.SessionRepository
import com.tit.nimonsapp.ui.common.BaseStateViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
) : BaseStateViewModel<LoginUiState>(application, LoginUiState()) {
    private val authRepository = AuthRepository()
    private val sessionRepository = SessionRepository(application)

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    private var lastAttemptTime = 0L
    private var attemptCount = 0
    private val RATE_LIMIT_WINDOW_MS = 60_000L // 1 minute
    private val MAX_ATTEMPTS_PER_WINDOW = 10
    private var countdownJob: kotlinx.coroutines.Job? = null

    override fun LoginUiState.withMeta(meta: UiResourceMeta): LoginUiState = copy(meta = meta)

    fun onEmailChanged(email: String) {
        updateState {
            copy(email = email)
        }
    }

    fun onPasswordChanged(password: String) {
        updateState {
            copy(password = password)
        }
    }

    fun login() {
        val now = System.currentTimeMillis()

        if (now - lastAttemptTime > RATE_LIMIT_WINDOW_MS) {
            attemptCount = 0
        }

        // Rate limit check
        if (attemptCount >= MAX_ATTEMPTS_PER_WINDOW && now - lastAttemptTime < RATE_LIMIT_WINDOW_MS) {
            updateState {
                withMeta(meta.copy(errorMessage = "Too many login attempts. Please try again later."))
            }
            // Start countdown
            startCountdown(RATE_LIMIT_WINDOW_MS - (now - lastAttemptTime))
            return
        }

        lastAttemptTime = now
        attemptCount++

        val email = uiState.value.email.trim()
        val password = uiState.value.password

        if (email.isBlank()) {
            updateState {
                withMeta(meta.copy(errorMessage = "Email is required"))
            }
            return
        }

        if (!emailRegex.matches(email)) {
            updateState {
                withMeta(meta.copy(errorMessage = "Invalid email format"))
            }
            return
        }

        if (password.isBlank()) {
            updateState {
                withMeta(meta.copy(errorMessage = "Password is required"))
            }
            return
        }

        viewModelScope.launch {
            updateState {
                withMeta(UiResourceMeta(isLoading = true, errorMessage = null))
            }

            runCatching {
                authRepository.login(email, password)
            }.onSuccess { response ->
                sessionRepository.saveToken(response.token)
                attemptCount = 0 // Reset counter on success
                updateState {
                    copy(
                        meta = UiResourceMeta(),
                        password = "",
                        isLoggedIn = true,
                    )
                }
            }.onFailure { throwable ->
                val errorMessage = when (throwable) {
                    is retrofit2.HttpException -> {
                        when (throwable.code()) {
                            401 -> "Email atau password salah"
                            409 -> "Sesi telah berakhir, silakan login ulang"
                            403 -> "Akses ditolak"
                            404 -> "Account not found"
                            500 -> "Server error. Please try again."
                            else -> throwable.message ?: "Login failed"
                        }
                    }
                    is java.net.UnknownHostException, is java.net.ConnectException -> "No internet connection"
                    is java.net.SocketTimeoutException -> "Connection timeout. Please try again."
                    else -> throwable.message ?: "Login failed"
                }
                updateState {
                    copy(
                        meta =
                            UiResourceMeta(
                                isLoading = false,
                                errorMessage = errorMessage,
                            ),
                        isLoggedIn = false,
                    )
                }
            }
        }
    }

    fun consumeLoginSuccess() {
        updateState {
            copy(isLoggedIn = false)
        }
    }

    fun clearCountdown() {
        countdownJob?.cancel()
        updateState { copy(remainingSeconds = null, meta = meta.copy(errorMessage = null)) }
    }

    private fun startCountdown(remainingMs: Long) {
        countdownJob?.cancel()

        countdownJob = viewModelScope.launch {
            updateState { copy(remainingSeconds = (remainingMs / 1000).toInt()) }

            var remaining = remainingMs
            while (remaining > 0) {
                kotlinx.coroutines.delay(1000)
                remaining -= 1000
                updateState { copy(remainingSeconds = (remaining / 1000).toInt()) }
            }

            updateState { copy(remainingSeconds = null, meta = meta.copy(errorMessage = null)) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
