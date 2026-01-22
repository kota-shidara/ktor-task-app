package com.example.com.example.user.domain.repository

import com.example.com.example.user.domain.model.User

interface UserRepository {
    suspend fun create(user: User): User
    suspend fun findByEmail(email: String): User?
}