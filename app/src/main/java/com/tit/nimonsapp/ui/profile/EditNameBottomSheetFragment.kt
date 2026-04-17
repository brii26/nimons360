package com.tit.nimonsapp.ui.profile

import android.os.Bundle
import android.view.View
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.ContentEditNameBinding
import com.tit.nimonsapp.ui.common.AppBottomSheetFragment

class EditNameBottomSheetFragment : AppBottomSheetFragment(R.layout.content_edit_name) {

    private var _binding: ContentEditNameBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contentContainer = view.findViewById<android.widget.FrameLayout>(R.id.bottom_sheet_content)
        _binding = ContentEditNameBinding.bind(contentContainer.getChildAt(0))

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            // TODO: wire up save logic
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
