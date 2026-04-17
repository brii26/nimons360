// Cara penggunaan:
// class MyModal : AppFloatingModalFragment(R.layout.my_content_layout) { ... }
// MyModal().show(childFragmentManager, null)

package com.tit.nimonsapp.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import com.tit.nimonsapp.R

abstract class AppFloatingModalFragment(
    @LayoutRes private val contentLayoutId: Int,
) : DialogFragment() {

    override fun getTheme(): Int = R.style.AppFloatingModal

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_app_floating_modal, container, false)
        val contentContainer = root.findViewById<FrameLayout>(R.id.modal_content)
        inflater.inflate(contentLayoutId, contentContainer, true)
        return root
    }
}
