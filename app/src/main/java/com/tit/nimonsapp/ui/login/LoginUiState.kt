package com.tit.nimonsapp.ui.login

import com.tit.nimonsapp.ui.common.StatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class LoginUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    val email: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false,
) : StatefulUi
