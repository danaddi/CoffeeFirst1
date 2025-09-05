package com.example.coffeefirst.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeefirst.data.CartRepository
import com.example.coffeefirst.data.db.CartItem
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val userId = auth.currentUser!!.uid

    val cartItems = cartRepository.getCartItems(userId).asLiveData()

    fun addToCart(newItem: CartItem) {
        viewModelScope.launch {
            val currentItems = cartRepository.getCartItemsOnce(userId)
            val existingItem = currentItems.find { it.menuItemId == newItem.menuItemId }
            if (existingItem != null) {
                val updatedItem = existingItem.copy(quantity = existingItem.quantity + newItem.quantity)
                cartRepository.updateCartItem(updatedItem)
            } else {
                cartRepository.addToCart(newItem)
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart(userId)
        }
    }

    fun updateCartItem(item: CartItem) {
        viewModelScope.launch {
            cartRepository.updateCartItem(item)
        }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch {
            cartRepository.removeFromCart(item)
        }
    }

}