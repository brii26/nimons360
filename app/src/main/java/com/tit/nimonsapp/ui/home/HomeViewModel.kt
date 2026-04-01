package com.tit.nimonsapp.ui.home

import android.app.Application
import com.tit.nimonsapp.data.repository.AuthRepository
import com.tit.nimonsapp.data.repository.FamilyRepository
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta

class HomeViewModel(
    application: Application,
) : AuthenticatedRefreshableViewModel<HomeUiState>(application, HomeUiState()) {
    private val familyRepository = FamilyRepository()
    private val authRepository = AuthRepository()

    override fun HomeUiState.withMeta(meta: UiResourceMeta): HomeUiState = copy(meta = meta)

    override fun HomeUiState.withRefreshing(isRefreshing: Boolean): HomeUiState = copy(isRefreshing = isRefreshing)

    fun loadHome() {
        loadHome(isRefresh = false)
    }

    override fun refresh() {
        loadHome(isRefresh = true)
    }

    private fun loadHome(isRefresh: Boolean) {
        executeAuthenticatedLoad(
            isRefresh = isRefresh,
            errorMessageFallback = "Failed to load home data",
            loader = { token ->
                Triple(
                    authRepository.getMe(token),
                    familyRepository.getDiscoverFamilies(token),
                    familyRepository.getMyFamilies(token),
                )
            },
            onSuccess = { (me, discoverFamilies, myFamilies) ->
                copy(
                    me = me,
                    discoverFamilies = discoverFamilies,
                    myFamilies = myFamilies,
                )
            },
        )
    }
}
