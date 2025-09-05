package com.example.coffeefirst.ui.menu

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.coffeefirst.databinding.ItemMenuBinding
import com.example.coffeefirst.data.model.MenuItem

class MenuAdapter(
    private val onAddToCart: (MenuItem) -> Unit
) : ListAdapter<MenuItem, MenuAdapter.MenuViewHolder>(DiffCallback()) {

    inner class MenuViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuItem) {
            binding.title.text = item.name
            binding.image.setImageResource(item.imageResId)
            binding.price.text = "Цена: ${item.price.toInt()} ₽"
            binding.root.setOnClickListener { onAddToCart(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMenuBinding.inflate(inflater, parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        Log.d("MenuAdapter", "Binding item: ${getItem(position).name}")
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(old: MenuItem, newItem: MenuItem) = old.id == newItem.id
        override fun areContentsTheSame(old: MenuItem, newItem: MenuItem) = old == newItem
    }
}
