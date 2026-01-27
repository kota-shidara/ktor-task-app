package com.example.com.example.user.domain.repository

import com.example.com.example.user.domain.model.User

class FakeUserRepository : UserRepository {
    override suspend fun create(user: User): User {
        return user.copy(id = 1)
    }

    override suspend fun findByEmail(email: String): User? {
        return null
    }
}
