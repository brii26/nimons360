package com.tit.nimonsapp.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
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
import com.tit.nimonsapp.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var binding: FragmentLoginBinding? = null

    private fun requireBinding(): FragmentLoginBinding = requireNotNull(binding)

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return requireBinding().root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().emailInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    viewModel.onEmailChanged(s?.toString().orEmpty())
                    viewModel.clearCountdown()
                }

                override fun afterTextChanged(s: Editable?) = Unit
            },
        )

        requireBinding().passwordInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    viewModel.onPasswordChanged(s?.toString().orEmpty())
                }

                override fun afterTextChanged(s: Editable?) = Unit
            },
        )

        requireBinding().loginButton.setOnClickListener { viewModel.login() }

        var isPasswordVisible = false
        requireBinding().passwordInput.setOnRightActionClick {
            isPasswordVisible = !isPasswordVisible

            val inputType =
                if (isPasswordVisible) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }

            requireBinding().passwordInput.setInputType(inputType)

            if (isPasswordVisible) {
                requireBinding().passwordInput.setRightActionText("Hide")
                requireBinding().passwordInput.setRightActionIcon(R.drawable.ic_hide)
            } else {
                requireBinding().passwordInput.setRightActionText("Show")
                requireBinding().passwordInput.setRightActionIcon(R.drawable.ic_show)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    requireBinding().loadingIndicator.visibility =
                        if (state.meta.isLoading) View.VISIBLE else View.GONE
                    requireBinding().loginButton.isEnabled = !state.meta.isLoading

                    val errorMessage = state.meta.errorMessage
                    if (errorMessage.isNullOrBlank()) {
                        requireBinding().emailInput.setError(null)
                        requireBinding().passwordInput.setError(null)
                    } else {
                        val error = errorMessage.lowercase()
                        when {
                            error.contains("empty") -> {
                                requireBinding().emailInput.setError("Required")
                                requireBinding().passwordInput.setError("Required")
                            }

                            error.contains("email") || error.contains("user") -> {
                                requireBinding().emailInput.setError(errorMessage)
                                requireBinding().passwordInput.setError(null)
                            }

                            error.contains("password") -> {
                                requireBinding().passwordInput.setError(errorMessage)
                                requireBinding().emailInput.setError(null)
                            }

                            else -> {
                                requireBinding().emailInput.setError(errorMessage)
                            }
                        }
                    }

                    // Show countdown if rate limited
                    if (state.remainingSeconds != null && state.remainingSeconds!! > 0) {
                        val remainingText = "Coba lagi dalam ${state.remainingSeconds!!} detik"
                        requireBinding().emailInput.setError(remainingText)
                    }

                    if (state.isLoggedIn) {
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        viewModel.consumeLoginSuccess()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun clearError() {
        viewModel.clearError()
    }
}
