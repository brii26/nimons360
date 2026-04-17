package com.tit.nimonsapp.ui.families

import com.tit.nimonsapp.data.network.GetFamiliesResponseDto
import com.tit.nimonsapp.data.network.GetMyFamiliesResponseDto
import com.tit.nimonsapp.ui.common.RefreshableStatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

enum class FamiliesFilter {
    ALL, MY_FAMILIES
}

data class FamiliesUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    override val isRefreshing: Boolean = false,
    val myFamilies: List<GetMyFamiliesResponseDto> = emptyList(),
    val allFamilies: List<GetFamiliesResponseDto> = emptyList(),
    val pinnedFamilyIds: List<Int> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: FamiliesFilter = FamiliesFilter.ALL
) : RefreshableStatefulUi {
    
    val filteredAllFamilies: List<GetFamiliesResponseDto>
        get() = allFamilies.filter { it.name.contains(searchQuery, ignoreCase = true) }
            .let { list ->
                if (selectedFilter == FamiliesFilter.MY_FAMILIES) {
                    val myIds = myFamilies.map { it.id }
                    list.filter { it.id in myIds }
                } else list
            }

    val pinnedFamilies: List<GetFamiliesResponseDto>
        get() = allFamilies.filter { it.id in pinnedFamilyIds }
}
