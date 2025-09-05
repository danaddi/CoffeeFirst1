package com.example.coffeefirst.ui.menu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeefirst.R
import com.example.coffeefirst.data.model.MenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
) : ViewModel() {

    val menuItems = listOf(
        MenuItem("coffee1", "Капучино", R.drawable.latte, "Кофе", 150.0f),
        MenuItem("coffee2", "Латте", R.drawable.latte, "Кофе", 200.0f),
        MenuItem("coffee3", "Раф", R.drawable.latte, "Кофе", 184.0f),
        MenuItem("coffee4", "Эспрессо", R.drawable.latte, "Кофе", 130.0f),
        MenuItem("coffee5", "Макиато", R.drawable.latte, "Кофе", 150.0f),
        MenuItem("coffee6", "Фраппе", R.drawable.latte, "Кофе", 250.0f),
        MenuItem("coffee7", "Американо", R.drawable.latte, "Кофе", 200.0f),
        MenuItem("tea1", "Зеленый чай", R.drawable.water, "Чай", 100.0f),
        MenuItem("food1", "Сэндвич", R.drawable.hamburger, "Еда", 120.0f),
        MenuItem("dessert1", "Чизкейк", R.drawable.cheesecake, "Десерты", 180.0f),
    )

    private val _selectedCategory = MutableStateFlow("Кофе")
    val selectedCategory = _selectedCategory.asStateFlow()

    val filteredItems = selectedCategory.map { category ->
        menuItems.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.Lazily, menuItems.filter { it.category == "Кофе" })

    fun selectCategory(category: String) {
        Log.d("MenuViewModel", "Category selected: $category")
        _selectedCategory.value = category
    }
}
