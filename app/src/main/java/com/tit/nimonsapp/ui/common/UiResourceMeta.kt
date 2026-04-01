package com.tit.nimonsapp.ui.common

data class UiResourceMeta(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFromCache: Boolean = false,
    val isStale: Boolean = false,
    val lastUpdatedAt: String? = null,
)
