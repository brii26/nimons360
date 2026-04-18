package com.tit.nimonsapp.ui.families

import com.tit.nimonsapp.data.network.GetFamiliesResponseDto
import com.tit.nimonsapp.data.network.GetMyFamiliesResponseDto
import com.tit.nimonsapp.ui.common.RefreshableStatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

const val FAMILIES_PAGE_SIZE = 5

enum class FamiliesFilter {
    ALL, MY_FAMILIES
}

data class FamilyItem(
    val family: GetFamiliesResponseDto,
    val isPinned: Boolean,
)

data class FamiliesUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    override val isRefreshing: Boolean = false,
    val myFamilies: List<GetMyFamiliesResponseDto> = emptyList(),
    val allFamilies: List<GetFamiliesResponseDto> = emptyList(),
    val pinnedFamilyIds: List<Int> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: FamiliesFilter = FamiliesFilter.ALL,
    // precompute di viewmodel biar ui ga refilter
    val allFilteredItems: List<FamilyItem> = emptyList(),
    val displayedCount: Int = FAMILIES_PAGE_SIZE,
) : RefreshableStatefulUi {

    val pagedItems: List<FamilyItem>
        get() = allFilteredItems.take(displayedCount)

    val pinnedItems: List<FamilyItem>
        get() = allFilteredItems.filter { it.isPinned }

    val hasMoreItems: Boolean
        get() = displayedCount < allFilteredItems.size
}
