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
                updateState {
                    copy(
                        meta = UiResourceMeta(),
                        password = "",
                        isLoggedIn = true,
                    )
                }
            }.onFailure { throwable ->
                updateState {
                    copy(
                        meta =
                            UiResourceMeta(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Login failed",
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
}
