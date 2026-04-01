package com.tit.nimonsapp.ui.startup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R
import com.tit.nimonsapp.data.repository.SessionRepository
import kotlinx.coroutines.launch

class StartupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FrameLayout(requireContext())

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val sessionRepository = SessionRepository(requireContext())
            val hasToken = sessionRepository.hasToken()

            val navController = findNavController()
            if (hasToken) {
                navController.navigate(R.id.action_startupFragment_to_homeFragment)
            } else {
                navController.navigate(R.id.action_startupFragment_to_loginFragment)
            }
        }
    }
}
