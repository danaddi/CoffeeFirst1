package com.example.coffeefirst.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeefirst.data.db.CartItem
import com.example.coffeefirst.databinding.FragmentCartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels()
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CartAdapter(
            onRemoveClick = { viewModel.removeItem(it) },
            onQuantityChange = { viewModel.updateCartItem(it) }
        )

        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = adapter

        viewModel.cartItems.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.buttonPlaceOrder.setOnClickListener {
            val cartItems = viewModel.cartItems.value ?: emptyList()
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Корзина пуста", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val totalPrice = calculateTotalPrice(cartItems)
            val bonus = (totalPrice * 0.2).toInt()

            val action = CartFragmentDirections.actionCartToPayment(totalPrice.toFloat(), bonus)
            findNavController().navigate(action)
        }


    }

    private fun calculateTotalPrice(items: List<CartItem>): Double {
        return items.sumOf { it.quantity * it.price.toDouble() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

