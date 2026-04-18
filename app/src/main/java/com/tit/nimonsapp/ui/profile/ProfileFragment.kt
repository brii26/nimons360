package com.tit.nimonsapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

    private fun requireBinding(): FragmentProfileBinding = requireNotNull(binding)

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return requireBinding().root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadProfile()

        requireBinding().editAvatarButton.setOnClickListener {
            EditNameBottomSheetFragment().show(childFragmentManager, null)
        }

        requireBinding().backHomeButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }

        requireBinding().logoutButton.setOnClickListener {
            SignOutModalFragment(onConfirm = { viewModel.logout() })
                .show(childFragmentManager, null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.profile?.let { profile ->
                        requireBinding().profileName.text = profile.fullName
                        requireBinding().profileEmail.text = profile.email
                        requireBinding().profileAvatar.setLetter(
                            profile.fullName.firstOrNull()?.toString() ?: "?",
                            requireContext().getColor(android.R.color.black),
                        )
                    }

                    if (state.isLoggedOut) {
                        viewModel.consumeLogout()
                        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
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
