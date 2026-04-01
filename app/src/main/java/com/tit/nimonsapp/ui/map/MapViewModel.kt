package com.tit.nimonsapp.ui.map

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.tit.nimonsapp.ui.common.BaseStateViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
import kotlinx.coroutines.launch

class MapViewModel(
    application: Application,
) : BaseStateViewModel<MapUiState>(application, MapUiState()) {
    override fun MapUiState.withMeta(meta: UiResourceMeta): MapUiState = copy(meta = meta)

    fun setSocketConnected(isConnected: Boolean) {
        updateState {
            copy(
                isSocketConnected = isConnected,
                meta =
                    if (isConnected) {
                        UiResourceMeta()
                    } else {
                        meta.copy(errorMessage = null)
                    },
            )
        }
    }

    fun connectSocket() {
        viewModelScope.launch {
            updateState {
                withMeta(UiResourceMeta(isLoading = true, errorMessage = null))
            }

            updateState {
                copy(
                    meta = UiResourceMeta(),
                    isSocketConnected = true,
                )
            }
        }
    }

    fun disconnectSocket() {
        updateState {
            copy(
                meta = UiResourceMeta(),
                isSocketConnected = false,
            )
        }
    }
}
