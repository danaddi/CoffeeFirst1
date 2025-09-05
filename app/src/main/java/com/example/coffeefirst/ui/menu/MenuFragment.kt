package com.example.coffeefirst.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeefirst.databinding.FragmentMenuBinding
import com.example.coffeefirst.ui.cart.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

    private lateinit var adapter: MenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MenuAdapter(
            onAddToCart = { menuItem ->
                val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                cartViewModel.addToCart(
                    com.example.coffeefirst.data.db.CartItem(
                        userId = userId,
                        menuItemId = menuItem.id,
                        name = menuItem.name,
                        quantity = 1,
                        price = menuItem.price
                    )
                )
            }
        )

        binding.menuRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.menuRecyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredItems.collectLatest { items ->
                Log.d("MenuFragment", "Filtered items count: ${items.size}")
                adapter.submitList(items)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
