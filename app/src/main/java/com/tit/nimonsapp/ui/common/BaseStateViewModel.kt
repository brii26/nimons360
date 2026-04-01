package com.tit.nimonsapp.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseStateViewModel<S : StatefulUi>(
    application: Application,
    initialState: S,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    protected fun updateState(transform: S.() -> S) {
        _uiState.value = _uiState.value.transform()
    }

    protected fun currentState(): S = _uiState.value

    protected abstract fun S.withMeta(meta: UiResourceMeta): S

    open fun clearError() {
        updateState {
            withMeta(meta.copy(errorMessage = null))
        }
    }
}
