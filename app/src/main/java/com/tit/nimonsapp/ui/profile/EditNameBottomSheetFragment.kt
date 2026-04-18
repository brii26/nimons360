package com.tit.nimonsapp.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.ContentEditNameBinding
import com.tit.nimonsapp.ui.common.AppBottomSheetFragment
import kotlinx.coroutines.launch

class EditNameBottomSheetFragment : AppBottomSheetFragment(R.layout.content_edit_name) {
    private var binding: ContentEditNameBinding? = null

    private fun requireBinding(): ContentEditNameBinding = requireNotNull(binding)

    private val viewModel: ProfileViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val contentContainer = view.findViewById<android.widget.FrameLayout>(R.id.bottom_sheet_content)
        binding = ContentEditNameBinding.bind(contentContainer.getChildAt(0))

        val currentName =
            viewModel.uiState.value.profile
                ?.fullName
        if (!currentName.isNullOrEmpty()) {
            requireBinding().inputDisplayName.text?.append(currentName)
        }

        var saveInProgress = false

        requireBinding().btnCancel.setOnClickListener { dismiss() }
        requireBinding().btnSave.setOnClickListener {
            val name =
                requireBinding()
                    .inputDisplayName.text
                    ?.toString()
                    .orEmpty()
            saveInProgress = true
            viewModel.updateFullName(name)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val isLoading = state.meta.isLoading
                    requireBinding().btnSave.isEnabled = !isLoading
                    if (saveInProgress && !isLoading && state.meta.errorMessage == null) {
                        dismiss()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
