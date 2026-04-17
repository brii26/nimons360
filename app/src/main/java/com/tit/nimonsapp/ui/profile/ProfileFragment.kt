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
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadProfile()

        binding.editAvatarButton.setOnClickListener {
            EditNameBottomSheetFragment().show(childFragmentManager, null)
        }

        binding.backHomeButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }

        binding.logoutButton.setOnClickListener {
            SignOutModalFragment(onConfirm = { viewModel.logout() })
                .show(childFragmentManager, null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.profile?.let { profile ->
                        binding.profileName.text = profile.fullName
                        binding.profileEmail.text = profile.email
                        binding.profileAvatar.setLetter(
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
        _binding = null
    }
}
