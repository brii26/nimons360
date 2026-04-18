package com.tit.nimonsapp.ui.createfamily

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.FragmentCreateFamiliesBinding
import kotlinx.coroutines.launch

class CreateFamilyFragment : Fragment() {
    private var binding: FragmentCreateFamiliesBinding? = null

    private fun requireBinding(): FragmentCreateFamiliesBinding = requireNotNull(binding)

    private val viewModel: CreateFamilyViewModel by viewModels()
    private lateinit var iconPickerAdapter: IconPickerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateFamiliesBinding.inflate(inflater, container, false)
        return requireBinding().root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupIconPicker()
        setupListeners()
        observeUiState()
    }

    private fun setupIconPicker() {
        iconPickerAdapter =
            IconPickerAdapter(familyIcons) { selectedUrl ->
                viewModel.onIconUrlChanged(selectedUrl)
            }

        requireBinding().rvIconPicker.apply {
            layoutManager = GridLayoutManager(context, 5)
            adapter = iconPickerAdapter
        }
    }

    private fun setupListeners() {
        requireBinding().btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        requireBinding().btnCreate.setOnClickListener {
            viewModel.createFamily()
        }

        requireBinding().etFamilyName.addTextChangedListener { text ->
            viewModel.onNameChanged(text?.toString() ?: "")
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    requireBinding().ivPreviewIcon.load(state.iconUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_app)
                    }

                    requireBinding().btnCreate.isEnabled =
                        state.name.isNotBlank() && !state.meta.isLoading

                    requireBinding().btnCreate.setTextColor(
                        if (requireBinding().btnCreate.isEnabled) {
                            resources.getColor(R.color.nimons_green, null)
                        } else {
                            resources.getColor(R.color.section_label, null)
                        },
                    )

                    requireBinding().loadingIndicator.visibility =
                        if (state.meta.isLoading) View.VISIBLE else View.GONE

                    if (state.createdFamily != null) {
                        viewModel.consumeCreatedFamily()
                        findNavController().popBackStack()
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
