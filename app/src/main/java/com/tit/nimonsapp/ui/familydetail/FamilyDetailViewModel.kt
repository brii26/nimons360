package com.tit.nimonsapp.ui.familydetail

import android.app.Application
import com.tit.nimonsapp.data.repository.AuthRepository
import com.tit.nimonsapp.data.repository.FamilyRepository
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
import retrofit2.HttpException

class FamilyDetailViewModel(
    application: Application,
) : AuthenticatedRefreshableViewModel<FamilyDetailUiState>(application, FamilyDetailUiState()) {
    private val familyRepository = FamilyRepository()
    private val authRepository = AuthRepository()
    private var currentFamilyId: Int? = null

    override fun FamilyDetailUiState.withMeta(meta: UiResourceMeta): FamilyDetailUiState =
        copy(
            meta = meta,
            isSubmittingAction = if (meta.errorMessage != null) false else isSubmittingAction,
        )

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
                    authRepository.getMe(token),
                )
            },
            onSuccess = { (familyDetail, me) ->
                copy(familyDetail = familyDetail, me = me)
            },
        )
    }

    fun joinFamily(
        familyCode: String,
        onJoinSuccess: () -> Unit,
    ) {
        val familyId = currentFamilyId ?: uiState.value.familyDetail?.id ?: return

        if (familyCode.isBlank()) {
            updateState {
                copy(meta = meta.copy(errorMessage = "Family code must not be empty"))
            }
            return
        }

        executeAuthenticatedAction(
            errorMessageFallback = "Wrong Family Code!",
            onStart = {
                copy(
                    isSubmittingAction = true,
                    meta = meta.copy(errorMessage = null),
                )
            },
            action = { token ->
                try {
                    familyRepository.joinFamily(token, familyId, familyCode.trim())
                } catch (e: HttpException) {
                    if (e.code() == 403) {
                        throw Exception("Wrong Family Code!")
                    }
                    throw e
                }
            },
            onSuccess = {
                copy(isSubmittingAction = false)
            },
            afterSuccess = {
                onJoinSuccess()
                refresh()
            },
        )
    }

    override fun clearError() {
        super.clearError()
    }

    fun leaveFamily(onSuccess: () -> Unit) {
        val familyId = currentFamilyId ?: uiState.value.familyDetail?.id ?: return

        executeAuthenticatedAction(
            errorMessageFallback = "Failed to leave family",
            onStart = {
                copy(
                    isSubmittingAction = true,
                    meta = meta.copy(errorMessage = null),
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
