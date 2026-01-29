package com.example.com.example.user.application.service

import com.example.com.example.user.application.dto.RegisterRequest
import com.example.com.example.user.application.event.SpyEventPublisher
import com.example.com.example.user.domain.repository.FakeUserRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserServiceTest {

    private val fakeRepository = FakeUserRepository()
    private val spyEventPublisher = SpyEventPublisher()
    private val userService = UserService(fakeRepository, spyEventPublisher)

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

    @Test
    fun `deleteUser should delete user and publish event`() = runBlocking {
        // Arrange
        val request = RegisterRequest(
            name = "削除ユーザー",
            email = "delete@example.com",
            password = "password123"
        )
        val registered = userService.register(request)
        val userId = registered.token.removePrefix("dummy-token-").toInt()

        // Act
        val result = userService.deleteUser(userId)

        // Assert
        assertTrue(result)
        assertEquals(1, spyEventPublisher.publishedEvents.size)
        assertEquals(userId, spyEventPublisher.publishedEvents[0].userId)
        assertEquals("delete@example.com", spyEventPublisher.publishedEvents[0].email)
    }

    @Test
    fun `deleteUser should return false for non-existent user`() = runBlocking {
        // Act
        val result = userService.deleteUser(999)

        // Assert
        assertFalse(result)
        assertEquals(0, spyEventPublisher.publishedEvents.size)
    }
}
