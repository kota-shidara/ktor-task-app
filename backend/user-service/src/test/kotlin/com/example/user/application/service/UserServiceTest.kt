package com.example.com.example.user.application.service

import com.example.com.example.user.application.dto.RegisterRequest
import com.example.com.example.user.domain.model.User
import com.example.com.example.user.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserServiceTest {

    private val mockRepository = object : UserRepository {
        override suspend fun create(user: User): User {
            return user.copy(id = 1)
        }

        override suspend fun findByEmail(email: String): User? {
            return null
        }
    }

    private val userService = UserService(mockRepository)

    @Test
    fun `register should return AuthResponse with token and name`() = runBlocking {
        // Arrange
        val request = RegisterRequest(
            name = "テストユーザー",
            email = "test@example.com",
            password = "password123"
        )

        // Act
        val result = userService.register(request)

        // Assert
        assertNotNull(result)
        assertEquals("テストユーザー", result.name)
        assertTrue(result.token.startsWith("dummy-token-"))
    }
}
