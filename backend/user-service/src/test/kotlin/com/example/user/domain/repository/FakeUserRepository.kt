package com.example.com.example.user.domain.repository

import com.example.com.example.user.domain.model.User

class FakeUserRepository : UserRepository {
    private val users = mutableListOf<User>()
    private var nextId = 1

    override suspend fun create(user: User): User {
        val newUser = user.copy(id = nextId++)
        users.add(newUser)
        return newUser
    }

    override suspend fun findByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    override suspend fun findById(id: Int): User? {
        return users.find { it.id == id }
    }

    override suspend fun deleteById(id: Int): Boolean {
        return users.removeIf { it.id == id }
    }
}
