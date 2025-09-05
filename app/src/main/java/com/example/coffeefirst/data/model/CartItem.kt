package com.example.coffeefirst.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val menuItemId: Int,
    val quantity: Int
)
