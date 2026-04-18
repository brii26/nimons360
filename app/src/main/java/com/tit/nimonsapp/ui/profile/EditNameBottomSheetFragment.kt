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

    private var _binding: ContentEditNameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contentContainer = view.findViewById<android.widget.FrameLayout>(R.id.bottom_sheet_content)
        _binding = ContentEditNameBinding.bind(contentContainer.getChildAt(0))

        val currentName = viewModel.uiState.value.profile?.fullName
        if (!currentName.isNullOrEmpty()) {
            binding.inputDisplayName.text?.append(currentName)
        }

        var saveInProgress = false

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            val name = binding.inputDisplayName.text?.toString().orEmpty()
            saveInProgress = true
            viewModel.updateFullName(name)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val isLoading = state.meta.isLoading
                    binding.btnSave.isEnabled = !isLoading
                    if (saveInProgress && !isLoading && state.meta.errorMessage == null) {
                        dismiss()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
