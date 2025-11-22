package com.example.loginapp.domain.model

data class Category(
    val id: Int = 0,
    val name: String,
    val icon: String,
    val color: String,
    val type: String, // "EXPENSE" or "INCOME"
    val isDefault: Boolean = false
)
