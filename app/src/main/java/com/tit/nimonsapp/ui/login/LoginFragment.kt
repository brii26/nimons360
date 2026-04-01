package com.tit.nimonsapp.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput: EditText = view.findViewById(R.id.email_input)
        val passwordInput: EditText = view.findViewById(R.id.password_input)
        val loginButton: Button = view.findViewById(R.id.login_button)
        val errorText: TextView = view.findViewById(R.id.error_text)
        val loadingIndicator: ProgressBar = view.findViewById(R.id.loading_indicator)

        emailInput.addTextChangedListener(
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
                }

                override fun afterTextChanged(s: Editable?) = Unit
            },
        )

        passwordInput.addTextChangedListener(
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

        loginButton.setOnClickListener {
            viewModel.login()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    loadingIndicator.visibility =
                        if (state.meta.isLoading) View.VISIBLE else View.GONE

                    loginButton.isEnabled = !state.meta.isLoading

                    if (state.meta.errorMessage.isNullOrBlank()) {
                        errorText.visibility = View.GONE
                    } else {
                        errorText.visibility = View.VISIBLE
                        errorText.text = state.meta.errorMessage
                    }

                    if (state.isLoggedIn) {
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        viewModel.consumeLoginSuccess()
                    }
                }
            }
        }
    }
}
