package com.tit.nimonsapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PinnedFamily(
    val id: Long?,
    val familyId: Int,
) : Parcelable {
    companion object {
        const val TABLE_PINNED = "table_pinned"
        const val ID = "id"
        const val FAMILY_ID = "familyId"
    }
}
