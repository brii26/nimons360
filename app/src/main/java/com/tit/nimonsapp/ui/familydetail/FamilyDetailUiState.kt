package com.tit.nimonsapp.ui.familydetail

import com.tit.nimonsapp.data.network.GetFamilyDetailResponseDto
import com.tit.nimonsapp.data.network.GetMeResponseDto
import com.tit.nimonsapp.ui.common.RefreshableStatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class FamilyDetailUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    override val isRefreshing: Boolean = false,
    val familyDetail: GetFamilyDetailResponseDto? = null,
    val me: GetMeResponseDto? = null,
    val isSubmittingAction: Boolean = false,
) : RefreshableStatefulUi
