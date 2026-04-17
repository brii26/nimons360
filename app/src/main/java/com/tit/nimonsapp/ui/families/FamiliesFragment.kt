package com.tit.nimonsapp.ui.families

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.FragmentFamiliesBinding
import kotlinx.coroutines.launch

class FamiliesFragment : Fragment() {
    private var _binding: FragmentFamiliesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FamiliesViewModel by viewModels()

    private lateinit var pinnedAdapter: FamiliesAdapter
    private lateinit var allFamiliesAdapter: FamiliesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFamiliesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupListeners()
        observeUiState()

        viewModel.loadFamilies()
    }

    private fun setupAdapters() {
        pinnedAdapter = FamiliesAdapter(
            onPinClick = { id -> viewModel.togglePinned(id) },
            onItemClick = { id -> navigateToDetail(id) }
        )
        
        allFamiliesAdapter = FamiliesAdapter(
            onPinClick = { id -> viewModel.togglePinned(id) },
            onItemClick = { id -> navigateToDetail(id) }
        )

        binding.pinnedRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pinnedAdapter
        }

        binding.allFamiliesRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = allFamiliesAdapter
        }
    }

    private fun navigateToDetail(familyId: Int) {
        val bundle = bundleOf("familyId" to familyId)
        findNavController().navigate(R.id.action_familiesFragment_to_familyDetailFragment, bundle)
    }

    private fun setupListeners() {
        binding.searchInput.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text?.toString() ?: "")
        }

        binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chip_my_families -> FamiliesFilter.MY_FAMILIES
                else -> FamiliesFilter.ALL
            }
            viewModel.updateFilter(filter)
        }

        binding.fabAddFamily.setOnClickListener {
            findNavController().navigate(R.id.action_familiesFragment_to_createFamilyFragment)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    pinnedAdapter.setPinnedIds(state.pinnedFamilyIds)
                    pinnedAdapter.submitList(state.pinnedFamilies)
                    
                    allFamiliesAdapter.setPinnedIds(state.pinnedFamilyIds)
                    allFamiliesAdapter.submitList(state.filteredAllFamilies)
                    
                    binding.pinnedLabel.visibility = if (state.pinnedFamilies.isEmpty()) View.GONE else View.VISIBLE
                    binding.pinnedRecycler.visibility = if (state.pinnedFamilies.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
