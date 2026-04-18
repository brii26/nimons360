package com.tit.nimonsapp.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R
import com.tit.nimonsapp.data.repository.SessionRepository
import com.tit.nimonsapp.ui.theme.NimonsTheme
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                NimonsTheme {
                splashScreen(
                    onSplashFinished = {
                        lifecycleScope.launch {
                            val hasSession = SessionRepository(requireContext()).hasToken()
                            val action =
                                if (hasSession) {
                                    R.id.action_splashFragment_to_homeFragment
                                } else {
                                    R.id.action_splashFragment_to_loginFragment
                                }
                            findNavController().navigate(action)
                        }
                    },
                )
                }
            }
        }
}
