package com.tit.nimonsapp.ui.createfamily

import com.tit.nimonsapp.data.network.CreateFamilyResponseDto
import com.tit.nimonsapp.ui.common.StatefulUi
import com.tit.nimonsapp.ui.common.UiResourceMeta

data class CreateFamilyUiState(
    override val meta: UiResourceMeta = UiResourceMeta(),
    val name: String = "",
    val iconUrl: String = "https://mad.labpro.hmif.dev/assets/family_icon_1.png",
    val createdFamily: CreateFamilyResponseDto? = null,
) : StatefulUi

val familyIcons =
    listOf(
        "https://mad.labpro.hmif.dev/assets/family_icon_1.png",
        "https://mad.labpro.hmif.dev/assets/family_icon_2.png",
        "https://mad.labpro.hmif.dev/assets/family_icon_3.png",
        "https://mad.labpro.hmif.dev/assets/family_icon_4.png",
        "https://mad.labpro.hmif.dev/assets/family_icon_5.png",
        "https://mad.labpro.hmif.dev/assets/family_icon_6.png",
        "https://mad.labpro.hmif.dev/assets/family_icon_7.png",
        "https://mad.labpro.hmif.dev/assets/family_icon_8.png",
    )
