package com.example.coffeefirst.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.coffeefirst.R
import com.example.coffeefirst.databinding.FragmentProfileBinding
import com.example.coffeefirst.ui.auth.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAuthState()
        setupListeners()
    }

    private fun checkAuthState() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            navigateToLogin()
            return
        }

        binding.tvEmail.text = currentUser.email ?: "Email не указан"
        binding.tvName.text = currentUser.displayName ?: "Имя не указано"

        currentUser.photoUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .into(binding.ivProfile)
        }
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Выйти") { _, _ ->
                viewModel.logout()
                navigateToLogin()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteAccountConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удаление аккаунта")
            .setMessage("Все ваши данные будут удалены. Вы уверены?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteAccount() {
        binding.progressBar.visibility = View.VISIBLE

        viewModel.deleteAccount().observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE

            if (result.isSuccess) {
                Toast.makeText(
                    requireContext(),
                    "Аккаунт успешно удален",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToLogin()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Ошибка: ${result.exceptionOrNull()?.message ?: "Неизвестная ошибка"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(
            R.id.action_profile_to_login,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
