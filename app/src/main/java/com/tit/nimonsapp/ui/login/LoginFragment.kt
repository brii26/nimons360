package com.tit.nimonsapp.ui.login

import android.os.Bundle
import android.text.Editable
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
import android.text.InputType

class LoginFragment : Fragment() {
	private var _binding: FragmentLoginBinding? = null
	private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onEmailChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onPasswordChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.loginButton.setOnClickListener { viewModel.login() }

		//toggle password visibility
		var isPasswordVisible = false
		binding.passwordInput.setOnRightActionClick {
			isPasswordVisible = !isPasswordVisible

			val inputType = if (isPasswordVisible) {
				InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
			} else {
				InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
			}
			binding.passwordInput.setInputType(inputType)
			if (isPasswordVisible) {
				binding.passwordInput.setRightActionText("Hide")
				binding.passwordInput.setRightActionIcon(R.drawable.ic_hide)
			} else {
				binding.passwordInput.setRightActionText("Show")
				binding.passwordInput.setRightActionIcon(R.drawable.ic_show)
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.uiState.collect { state ->
					binding.loadingIndicator.visibility = if (state.meta.isLoading) View.VISIBLE else View.GONE
					binding.loginButton.isEnabled = !state.meta.isLoading

					val errorMessage = state.meta.errorMessage
					if (errorMessage.isNullOrBlank()) {
						binding.emailInput.setError(null)
						binding.passwordInput.setError(null)
					} else {
						val error = errorMessage.lowercase()
						when {
							error.contains("empty") -> {
								binding.emailInput.setError("Required")
								binding.passwordInput.setError("Required")
							}
							error.contains("email") || error.contains("user") -> {
								binding.emailInput.setError(errorMessage)
								binding.passwordInput.setError(null)
							}
							error.contains("password") -> {
								binding.passwordInput.setError(errorMessage)
								binding.emailInput.setError(null)
							}
							else -> {
								binding.emailInput.setError(errorMessage)
							}
						}
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
        _binding = null
    }
}
