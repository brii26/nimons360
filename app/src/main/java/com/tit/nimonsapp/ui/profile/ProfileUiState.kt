package com.tit.nimonsapp.ui.profile

import com.tit.nimonsapp.data.network.GetMeResponseDto
import com.tit.nimonsapp.ui.common.RefreshableStatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class ProfileUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    override val isRefreshing: Boolean = false,
    val profile: GetMeResponseDto? = null,
    val isLoggedOut: Boolean = false,
) : RefreshableStatefulUi
