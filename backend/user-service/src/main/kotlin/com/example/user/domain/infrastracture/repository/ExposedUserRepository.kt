package com.example.com.example.user.domain.infrastracture.repository

import com.example.com.example.db.DatabaseFactory.dbQuery
import com.example.com.example.user.domain.infrastracture.persistence.UserEntity
import com.example.com.example.user.domain.infrastracture.persistence.UserTable
import com.example.com.example.user.domain.model.User
import com.example.com.example.user.domain.repository.UserRepository

class ExposedUserRepository : UserRepository {
    override suspend fun create(user: User): User = dbQuery {
        UserEntity.new {
            name = user.name
            email = user.email
            password = user.password
        }.toDomain()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UserEntity.find {
            UserTable.email eq email
        }.singleOrNull()?.toDomain()
    }

    private fun UserEntity.toDomain() =
        User(
            id = this.id.value,
            name = this.name,
            email = this.email,
            password = this.password
        )
}