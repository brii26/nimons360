package com.tit.nimonsapp.ui.createfamily

import com.tit.nimonsapp.data.network.CreateFamilyResponseDto
import com.tit.nimonsapp.ui.common.StatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class CreateFamilyUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    val name: String = "",
    val iconUrl: String = "",
    val createdFamily: CreateFamilyResponseDto? = null,
) : StatefulUi
