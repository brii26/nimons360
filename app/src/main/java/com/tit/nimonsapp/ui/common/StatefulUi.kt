package com.tit.nimonsapp.ui.common

interface StatefulUi {
    val meta: UiResourceMeta
}

interface RefreshableStatefulUi : StatefulUi {
    val isRefreshing: Boolean
}
