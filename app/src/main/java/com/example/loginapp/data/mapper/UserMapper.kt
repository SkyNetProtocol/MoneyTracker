package com.example.loginapp.data.mapper

import com.example.loginapp.data.local.entity.UserEntity
import com.example.loginapp.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        passwordHash = passwordHash
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        passwordHash = passwordHash
    )
}
