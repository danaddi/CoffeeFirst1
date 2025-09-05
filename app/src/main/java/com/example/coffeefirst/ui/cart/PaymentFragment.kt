package com.example.coffeefirst.ui.cart

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coffeefirst.databinding.FragmentPaymentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private val args: PaymentFragmentArgs by navArgs()

    private val cartViewModel: CartViewModel by viewModels({ requireActivity() })

    private var appliedBonus: Int = 0
    private var isUsingBonus: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var total = args.totalPrice

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val earnedBonus = args.bonus
        val currentBonus = prefs.getInt("bonus", 0)

        binding.paymentDetails.text = "Сумма: $total ₽\nБонусы за заказ: +$earnedBonus\nДоступно бонусов: $currentBonus"


        binding.buttonPay.setOnClickListener {
            val success = (1..10).random() <= 8

            if (success) {
                val updatedBonus = if (isUsingBonus) {
                    currentBonus - appliedBonus
                } else {
                    currentBonus + earnedBonus
                }
                prefs.edit().putInt("bonus", updatedBonus).apply()

                cartViewModel.clearCart()
                Toast.makeText(requireContext(),
                    if (isUsingBonus) "Оплачено с бонусами! Бонусы не начислены." else "Оплата прошла. Начислено $earnedBonus бонусов!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Оплата не прошла. Попробуйте снова.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            findNavController().popBackStack()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonApplyBonus.setOnClickListener {
            if (isUsingBonus) {
                Toast.makeText(
                    requireContext(),
                    "Бонусы уже были применены",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val input = binding.etUseBonus.text.toString().toIntOrNull() ?: 0

            if (input <= 0) {
                Toast.makeText(
                    requireContext(),
                    "Введите корректное количество бонусов",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (input > currentBonus) {
                Toast.makeText(
                    requireContext(),
                    "У вас только $currentBonus бонусов",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (input > total) {
                Toast.makeText(
                    requireContext(),
                    "Сумма заказа меньше введённых бонусов",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            appliedBonus = input
            isUsingBonus = true
            total -= appliedBonus

            binding.paymentDetails.text =
                "Сумма: $total ₽\nБонусы за заказ: +$earnedBonus\nСписано бонусов: $appliedBonus\nБонусы не будут начислены"
            Toast.makeText(requireContext(), "Списано $appliedBonus бонусов", Toast.LENGTH_SHORT)
                .show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

