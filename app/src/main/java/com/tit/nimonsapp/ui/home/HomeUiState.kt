package com.tit.nimonsapp.ui.home

import com.tit.nimonsapp.data.network.GetDiscoverFamiliesResponseDto
import com.tit.nimonsapp.data.network.GetMeResponseDto
import com.tit.nimonsapp.data.network.GetMyFamiliesResponseDto
import com.tit.nimonsapp.ui.common.RefreshableStatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class HomeUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    override val isRefreshing: Boolean = false,
    val me: GetMeResponseDto? = null,
    val discoverFamilies: List<GetDiscoverFamiliesResponseDto> = emptyList(),
    val myFamilies: List<GetMyFamiliesResponseDto> = emptyList(),
) : RefreshableStatefulUi
