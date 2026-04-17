package com.tit.nimonsapp.ui.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tit.nimonsapp.data.network.WebSocketRepository

class MapViewModelFactory(
    private val application: Application,
    private val webSocketRepository: WebSocketRepository,
    private val token: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(application, webSocketRepository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
