package com.example.loginapp.domain.model

data class User(
    val id: Int = 0,
    val username: String,
    val passwordHash: String
)
