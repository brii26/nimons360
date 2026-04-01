package com.tit.nimonsapp.ui.map

import com.tit.nimonsapp.ui.common.StatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class MapUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    val isSocketConnected: Boolean = false,
) : StatefulUi
