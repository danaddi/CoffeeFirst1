package com.example.coffeefirst.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeefirst.R
import com.example.coffeefirst.databinding.FragmentHomeBinding
import com.example.coffeefirst.ui.cart.CartViewModel
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.activityViewModels
import com.example.coffeefirst.ui.menu.MenuViewModel



@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val cartViewModel: CartViewModel by viewModels()
    private val menuViewModel: MenuViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<MapFragment.CoffeeShop>(
            "selected_coffee_shop"
        )?.observe(viewLifecycleOwner) { coffeeShop ->
            binding.tvAddress.text = coffeeShop.name
        }
        binding.addressContainer.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_map)
        }
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        generateQrCode(userId)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val bonus = prefs.getInt("bonus", 0)
        binding.bonusTextView.text = "Бонусы: $bonus"

        binding.qrImageView.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val dialog = QrDialogFragment.newInstance(userId)
            dialog.show(parentFragmentManager, "QrCodeDialog")
        }

        binding.btnCart.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_cart)
        }


        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            val count = items.sumOf { it.quantity }
            showBadge(binding.btnCart, count)
        }

        setupCategoryButtons()


    }
    @OptIn(ExperimentalBadgeUtils::class)
    private fun showBadge(view: View, count: Int) {
        val badge = BadgeDrawable.create(requireContext()).apply {
            number = count
            isVisible = count > 0
        }
        BadgeUtils.attachBadgeDrawable(badge, view)
    }

    private fun setupCategoryButtons() {
        val categories = listOf("Кофе", "Чай", "Еда", "Десерты")

        binding.categoryContainer.removeAllViews()

        for (category in categories) {
            val button = Button(requireContext()).apply {
                text = category
                setOnClickListener {
                    menuViewModel.selectCategory(category)
                }
            }
            binding.categoryContainer.addView(button)
        }

        menuViewModel.selectCategory(categories.first())
    }


    private fun generateQrCode(data: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 200, 200)
            binding.qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
