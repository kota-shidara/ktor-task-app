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

    @Test
    fun `register should return AuthResponse with token and name`() = runBlocking {
        // Arrange
        val mockRepository = object : UserRepository {
            override suspend fun create(user: User): User {
                return user.copy(id = 1)
            }

            override suspend fun findByEmail(email: String): User? {
                return null
            }
        }

        val userService = UserService(mockRepository)
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
