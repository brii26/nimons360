package com.tit.nimonsapp.ui.home

import android.os.Bundle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): android.view.View =
        ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) { viewModel.loadHome() }

                homeScreen(
                    state = uiState,
                    onRefresh = viewModel::refresh,
                    onGoToProfile = {
                        findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    },
                    onGoToCreateFamily = {
                        findNavController().navigate(R.id.action_homeFragment_to_createFamilyFragment)
                    },
                    onGoToFamilyDetail = { familyId ->
                        findNavController().navigate(
                            R.id.action_homeFragment_to_familyDetailFragment,
                            bundleOf("familyId" to familyId),
                        )
                    },
                    onJoinFamily = { familyId ->
                        findNavController().navigate(
                            R.id.action_homeFragment_to_familyDetailFragment,
                            bundleOf("familyId" to familyId),
                        )
                    },
                )
            }
        }
}
