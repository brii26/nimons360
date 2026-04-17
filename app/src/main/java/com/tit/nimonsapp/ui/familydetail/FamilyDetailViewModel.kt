package com.tit.nimonsapp.ui.familydetail

import android.app.Application
import com.tit.nimonsapp.data.repository.AuthRepository
import com.tit.nimonsapp.data.repository.FamilyRepository
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta

class FamilyDetailViewModel(
    application: Application,
) : AuthenticatedRefreshableViewModel<FamilyDetailUiState>(application, FamilyDetailUiState()) {
    private val familyRepository = FamilyRepository()
    private val authRepository = AuthRepository()
    private var currentFamilyId: Int? = null

    override fun FamilyDetailUiState.withMeta(meta: UiResourceMeta): FamilyDetailUiState = copy(meta = meta)

    override fun FamilyDetailUiState.withRefreshing(isRefreshing: Boolean): FamilyDetailUiState = copy(isRefreshing = isRefreshing)

    fun loadFamilyDetail(familyId: Int) {
        currentFamilyId = familyId
        loadFamilyDetail(familyId = familyId, isRefresh = false)
    }

    override fun refresh() {
        val familyId = currentFamilyId ?: return
        loadFamilyDetail(familyId = familyId, isRefresh = true)
    }

    private fun loadFamilyDetail(
        familyId: Int,
        isRefresh: Boolean,
    ) {
        executeAuthenticatedLoad(
            isRefresh = isRefresh,
            errorMessageFallback = "Failed to load family detail",
            loader = { token ->
                Pair(
                    familyRepository.getFamilyDetail(token, familyId),
                    authRepository.getMe(token)
                )
            },
            onSuccess = { (familyDetail, me) ->
                copy(familyDetail = familyDetail, me = me)
            },
        )
    }

    fun leaveFamily(onSuccess: () -> Unit) {
        val familyId = currentFamilyId ?: uiState.value.familyDetail?.id ?: return

        executeAuthenticatedAction(
            errorMessageFallback = "Failed to leave family",
            onStart = {
                copy(
                    isSubmittingAction = true,
                    meta = UiResourceMeta(isLoading = false, errorMessage = null),
                )
            },
            action = { token ->
                familyRepository.leaveFamily(token, familyId)
            },
            onSuccess = {
                copy(isSubmittingAction = false)
            },
            afterSuccess = {
                onSuccess()
            },
        )
    }
}
