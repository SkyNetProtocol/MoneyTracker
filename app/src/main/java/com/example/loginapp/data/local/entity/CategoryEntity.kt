package com.example.loginapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String, // Emoji or icon identifier
    val color: String, // Hex color code (e.g., "#FF5722")
    val type: String, // "EXPENSE" or "INCOME"
    val isDefault: Boolean = false // System categories vs user-created
)
