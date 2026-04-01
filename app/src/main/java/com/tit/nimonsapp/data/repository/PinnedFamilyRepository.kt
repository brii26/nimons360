package com.tit.nimonsapp.data.repository

import android.content.Context
import com.tit.nimonsapp.model.PinnedFamily
import com.tit.nimonsapp.model.database

class PinnedFamilyRepository(
    private val context: Context,
) {
    fun pinFamily(familyId: Int) {
        context.database.insertPinnedIfNotExists(familyId)
    }

    fun unpinFamily(familyId: Int) {
        context.database.deletePinned(familyId)
    }

    fun isPinned(familyId: Int): Boolean = context.database.isPinned(familyId)

    fun getPinnedFamilies(): List<PinnedFamily> = context.database.getAllPinned()

    fun getPinnedFamilyIds(): List<Int> = context.database.getPinnedFamilyIds()

    fun clearPinnedFamilies() {
        context.database.deleteAllPinned()
    }
}
