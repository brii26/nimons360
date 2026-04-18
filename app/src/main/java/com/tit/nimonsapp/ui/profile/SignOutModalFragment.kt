package com.tit.nimonsapp.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.ContentSignOutBinding
import com.tit.nimonsapp.ui.common.AppFloatingModalFragment

class SignOutModalFragment(
    private val onConfirm: () -> Unit,
) : AppFloatingModalFragment(R.layout.content_sign_out) {
    private var binding: ContentSignOutBinding? = null

    private fun requireBinding(): ContentSignOutBinding = requireNotNull(binding)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val contentContainer = view.findViewById<FrameLayout>(R.id.modal_content)
        binding = ContentSignOutBinding.bind(contentContainer.getChildAt(0))

        requireBinding().btnCancel.setOnClickListener { dismiss() }
        requireBinding().btnSignOut.setOnClickListener {
            dismiss()
            onConfirm()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
