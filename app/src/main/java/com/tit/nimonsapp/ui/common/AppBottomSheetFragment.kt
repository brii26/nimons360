package com.tit.nimonsapp.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tit.nimonsapp.R

abstract class AppBottomSheetFragment(
    @LayoutRes private val contentLayoutId: Int,
) : BottomSheetDialogFragment() {
    override fun getTheme(): Int = R.style.AppBottomSheet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_app_bottom_sheet, container, false)
        val contentContainer = root.findViewById<FrameLayout>(R.id.bottom_sheet_content)
        inflater.inflate(contentLayoutId, contentContainer, true)
        return root
    }
}
