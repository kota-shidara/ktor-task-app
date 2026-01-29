package com.example.com.example.user.application.service

import com.example.com.example.user.application.dto.RegisterRequest
import com.example.com.example.user.domain.infrastracture.messaging.FakeUserEventPublisher
import com.example.com.example.user.domain.repository.FakeUserRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserServiceTest {

    private val fakeRepository = FakeUserRepository()
    private val fakeEventPublisher = FakeUserEventPublisher()
    private val userService = UserService(fakeRepository, fakeEventPublisher)

    @Test
    fun `register should return AuthResponse with token and name`() = runBlocking {
        val request = RegisterRequest(
            name = "テストユーザー",
            email = "test@example.com",
            password = "password123"
        )

        val result = userService.register(request)

        assertNotNull(result)
        assertEquals("テストユーザー", result.name)
        assertTrue(result.token.startsWith("dummy-token-"))
    }

    @Test
    fun `deleteAccount should delete user and publish event`() = runBlocking {
        val request = RegisterRequest(
            name = "削除ユーザー",
            email = "delete@example.com",
            password = "password123"
        )
        val registered = userService.register(request)
        val userId = registered.token.removePrefix("dummy-token-").toInt()

        val result = userService.deleteAccount(userId)

        assertTrue(result)
        assertEquals(1, fakeEventPublisher.publishedEvents.size)
        assertEquals(userId, fakeEventPublisher.publishedEvents.first())
        assertNull(fakeRepository.findById(userId))
    }

    @Test
    fun `deleteAccount should return false for non-existent user`() = runBlocking {
        val result = userService.deleteAccount(999)

        assertFalse(result)
        assertTrue(fakeEventPublisher.publishedEvents.isEmpty())
    }
}
