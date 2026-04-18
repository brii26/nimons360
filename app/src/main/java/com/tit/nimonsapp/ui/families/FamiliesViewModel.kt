package com.tit.nimonsapp.ui.families

import android.app.Application
import com.tit.nimonsapp.data.network.GetFamiliesResponseDto
import com.tit.nimonsapp.data.network.GetMyFamiliesResponseDto
import com.tit.nimonsapp.data.repository.FamilyRepository
import com.tit.nimonsapp.data.repository.PinnedFamilyRepository
import com.tit.nimonsapp.ui.common.AuthenticatedRefreshableViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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
                coroutineScope {
                    val myFamilies = async { familyRepository.getMyFamilies(token) }
                    val allFamilies = async { familyRepository.getFamilies(token) }
                    Triple(
                        myFamilies.await(),
                        allFamilies.await(),
                        pinnedFamilyRepository.getPinnedFamilyIds(),
                    )
                }
            },
            onSuccess = { (myFamilies, allFamilies, pinnedIds) ->
                copy(
                    myFamilies = myFamilies,
                    allFamilies = allFamilies,
                    pinnedFamilyIds = pinnedIds,
                ).recomputeFilter(resetPage = true)
            },
        )
    }

    fun updateSearchQuery(query: String) {
        updateState { copy(searchQuery = query).recomputeFilter(resetPage = true) }
    }

    fun updateFilter(filter: FamiliesFilter) {
        updateState { copy(selectedFilter = filter).recomputeFilter(resetPage = true) }
    }

    fun loadMore() {
        updateState {
            if (hasMoreItems) copy(displayedCount = displayedCount + FAMILIES_PAGE_SIZE) else this
        }
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
            copy(pinnedFamilyIds = currentPinned).recomputeFilter(resetPage = false)
        }
    }

    private fun FamiliesUiState.recomputeFilter(resetPage: Boolean): FamiliesUiState {
        val myIds: Set<Int>? =
            if (selectedFilter == FamiliesFilter.MY_FAMILIES) {
                myFamilies.map { it.id }.toSet()
            } else {
                null
            }
        val pinnedSet = pinnedFamilyIds.toSet()

        val sanitizedQuery = sanitizeSearchQuery(searchQuery)

        val filtered =
            allFamilies
                .filter { family ->
                    (sanitizedQuery.isEmpty() || family.name.contains(sanitizedQuery, ignoreCase = true)) &&
                        (myIds == null || family.id in myIds)
                }.map { FamilyItem(it, it.id in pinnedSet) }

        return copy(
            allFilteredItems = filtered,
            displayedCount = if (resetPage) FAMILIES_PAGE_SIZE else displayedCount,
        )
    }

    private fun sanitizeSearchQuery(query: String): String {
        // Escape regex special characters
        val specialChars = listOf("*", "+", "?", "[", "]", "{", "}", "(", ")")
        var result = query
        for (char in specialChars) {
            result = result.replace(char, "\\$char")
        }
        return result
    }
}
