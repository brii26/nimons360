package com.tit.nimonsapp.ui.families

import com.tit.nimonsapp.data.network.GetFamiliesResponseDto
import com.tit.nimonsapp.data.network.GetMyFamiliesResponseDto
import com.tit.nimonsapp.ui.common.RefreshableStatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class FamiliesUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    override val isRefreshing: Boolean = false,
    val myFamilies: List<GetMyFamiliesResponseDto> = emptyList(),
    val allFamilies: List<GetFamiliesResponseDto> = emptyList(),
    val pinnedFamilyIds: List<Int> = emptyList(),
) : RefreshableStatefulUi
