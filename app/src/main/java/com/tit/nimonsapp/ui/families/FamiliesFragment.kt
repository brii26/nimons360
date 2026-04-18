package com.tit.nimonsapp.ui.families

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.FragmentFamiliesBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FamiliesFragment : Fragment() {
    private var binding: FragmentFamiliesBinding? = null

    private fun requireBinding(): FragmentFamiliesBinding = requireNotNull(binding)

    private val viewModel: FamiliesViewModel by viewModels()

    private lateinit var pinnedAdapter: FamiliesAdapter
    private lateinit var allFamiliesAdapter: FamiliesAdapter

    private var searchDebounceJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFamiliesBinding.inflate(inflater, container, false)
        return requireBinding().root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupListeners()
        observeUiState()

        viewModel.loadFamilies()
    }

    private fun setupAdapters() {
        val binding = requireBinding()
        val sharedPool = RecyclerView.RecycledViewPool()

        pinnedAdapter =
            FamiliesAdapter(
                onPinClick = { id -> viewModel.togglePinned(id) },
                onItemClick = { id -> navigateToDetail(id) },
            )
        allFamiliesAdapter =
            FamiliesAdapter(
                onPinClick = { id -> viewModel.togglePinned(id) },
                onItemClick = { id -> navigateToDetail(id) },
            )

        binding.pinnedRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pinnedAdapter
            setRecycledViewPool(sharedPool)
            setHasFixedSize(false)
        }

        binding.allFamiliesRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = allFamiliesAdapter
            setRecycledViewPool(sharedPool)
            setHasFixedSize(false)
        }
    }

    private fun navigateToDetail(familyId: Int) {
        findNavController().navigate(
            R.id.action_familiesFragment_to_familyDetailFragment,
            bundleOf("familyId" to familyId),
        )
    }

    private fun setupListeners() {
        val binding = requireBinding()

        binding.searchInput.addTextChangedListener { text ->
            searchDebounceJob?.cancel()
            searchDebounceJob =
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(200)
                    viewModel.updateSearchQuery(text?.toString() ?: "")
                }
        }

        binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter =
                when (checkedIds.firstOrNull()) {
                    R.id.chip_my_families -> FamiliesFilter.MY_FAMILIES
                    else -> FamiliesFilter.ALL
                }
            viewModel.updateFilter(filter)
        }

        binding.fabAddFamily.setOnClickListener {
            findNavController().navigate(R.id.action_familiesFragment_to_createFamilyFragment)
        }

        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                val contentHeight = v.getChildAt(0).measuredHeight
                val viewHeight = v.measuredHeight
                if (scrollY + viewHeight >= contentHeight - 200) {
                    viewModel.loadMore()
                }
            },
        )
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val binding = requireBinding()
                    val pinned = state.pinnedItems

                    pinnedAdapter.submitList(pinned)
                    allFamiliesAdapter.submitList(state.pagedItems)

                    val hasPinned = pinned.isNotEmpty()
                    binding.pinnedLabel.visibility = if (hasPinned) View.VISIBLE else View.GONE
                    binding.pinnedRecycler.visibility = if (hasPinned) View.VISIBLE else View.GONE

                    binding.loadingFooter.visibility =
                        if (state.hasMoreItems) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchDebounceJob?.cancel()
        binding = null
    }
}
