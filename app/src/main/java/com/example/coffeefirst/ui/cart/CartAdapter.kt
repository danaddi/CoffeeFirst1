package com.example.coffeefirst.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeefirst.databinding.ItemCartBinding
import com.example.coffeefirst.data.db.CartItem

class CartAdapter(
    private val onRemoveClick: (CartItem) -> Unit,
    private val onQuantityChange: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback()) {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.itemName.text = item.name
            binding.itemPrice.text = item.price.toString() + " ₽"
            binding.itemQuantity.text = item.quantity.toString()

            binding.btnIncrease.setOnClickListener {
                val updated = item.copy(quantity = item.quantity + 1)
                onQuantityChange(updated)
            }

            binding.btnDecrease.setOnClickListener {
                val newQuantity = (item.quantity - 1).coerceAtLeast(1)
                val updated = item.copy(quantity = newQuantity)
                onQuantityChange(updated)
            }

            binding.btnRemove.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
