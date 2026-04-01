package com.tit.nimonsapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T,
)
