package com.tit.nimonsapp.ui.families

import android.app.Application
import com.tit.nimonsapp.data.repository.FamilyRepository
import com.tit.nimonsapp.data.repository.PinnedFamilyRepository
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta

class FamiliesViewModel(
    application: Application,
) : AuthenticatedRefreshableViewModel<FamiliesUiState>(application, FamiliesUiState()) {
    private val familyRepository = FamilyRepository()
    private val pinnedFamilyRepository = PinnedFamilyRepository(application)

    override fun FamiliesUiState.withMeta(meta: UiResourceMeta): FamiliesUiState = copy(meta = meta)

    override fun FamiliesUiState.withRefreshing(isRefreshing: Boolean): FamiliesUiState = copy(isRefreshing = isRefreshing)

    fun loadFamilies() {
        loadFamilies(isRefresh = false)
    }

    override fun refresh() {
        loadFamilies(isRefresh = true)
    }

    private fun loadFamilies(isRefresh: Boolean) {
        executeAuthenticatedLoad(
            isRefresh = isRefresh,
            errorMessageFallback = "Failed to load families",
            loader = { token ->
                Triple(
                    familyRepository.getMyFamilies(token),
                    familyRepository.getFamilies(token),
                    pinnedFamilyRepository.getPinnedFamilyIds(),
                )
            },
            onSuccess = { (myFamilies, allFamilies, pinnedFamilyIds) ->
                copy(
                    myFamilies = myFamilies,
                    allFamilies = allFamilies,
                    pinnedFamilyIds = pinnedFamilyIds,
                )
            },
        )
    }

    fun togglePinned(familyId: Int) {
        val currentPinned = uiState.value.pinnedFamilyIds.toMutableList()

        if (currentPinned.contains(familyId)) {
            pinnedFamilyRepository.unpinFamily(familyId)
            currentPinned.remove(familyId)
        } else {
            pinnedFamilyRepository.pinFamily(familyId)
            currentPinned.add(familyId)
        }

        updateState {
            copy(pinnedFamilyIds = currentPinned.sorted())
        }
    }

    fun refreshPinnedOnly() {
        updateState {
            copy(
                pinnedFamilyIds = pinnedFamilyRepository.getPinnedFamilyIds().sorted(),
            )
        }
    }
}
