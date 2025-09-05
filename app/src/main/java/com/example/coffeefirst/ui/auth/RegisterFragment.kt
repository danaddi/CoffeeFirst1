package com.example.coffeefirst.ui.auth

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.coffeefirst.R
import com.example.coffeefirst.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.checkBox1.setOnCheckedChangeListener { _, isChecked ->
            val method = if (isChecked) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
            binding.passwordEditText.transformationMethod = method
            binding.passwordEditText.setSelection(binding.passwordEditText.text?.length ?: 0)
        }

        binding.checkBox2.setOnCheckedChangeListener { _, isChecked ->
            val method = if (isChecked) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
            binding.confirmPasswordEditText.transformationMethod = method
            binding.confirmPasswordEditText.setSelection(binding.confirmPasswordEditText.text?.length ?: 0)
        }


        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (password == confirmPassword) {
                viewModel.register(email, password)
            } else {
                binding.errorText.text = "Passwords don't match"
            }
        }

        binding.loginText.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_home)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Loading -> showProgressBar()
                    is AuthState.Success -> {
                        hideProgressBar()
                        findNavController().navigate(
                            R.id.action_register_to_home,
                            null,
                            androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.loginFragment, true)
                                .build()
                        )
                    }
                    is AuthState.Error -> {
                        hideProgressBar()
                        showError(state.message)
                    }
                    else -> hideProgressBar()
                }
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.registerButton.isEnabled = false
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.registerButton.isEnabled = true
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}
