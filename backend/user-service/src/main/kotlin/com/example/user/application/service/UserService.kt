package com.example.com.example.user.application.service

import com.example.com.example.user.application.dto.AuthResponse
import com.example.com.example.user.application.dto.LoginRequest
import com.example.com.example.user.application.dto.RegisterRequest
import com.example.com.example.user.application.event.EventPublisher
import com.example.com.example.user.application.event.NoOpEventPublisher
import com.example.com.example.user.application.event.UserDeletedEvent
import com.example.com.example.user.domain.model.User
import com.example.com.example.user.domain.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt

class UserService(
    private val repository: UserRepository,
    private val eventPublisher: EventPublisher = NoOpEventPublisher()
) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = User(
            name = request.name,
            email = request.email,
            password = hashedPassword
        )
        val createdUser = repository.create(user)
        return AuthResponse("dummy-token-${createdUser.id}", createdUser.name)
    }

    suspend fun login(request: LoginRequest): AuthResponse? {
        val user = repository.findByEmail(request.email)
        if (user != null && BCrypt.checkpw(request.password, user.password)) {
            return AuthResponse("dummy-token-${user.id}", user.name)
        }
        return null
    }

    suspend fun deleteUser(userId: Int): Boolean {
        val user = repository.findById(userId) ?: return false
        val deleted = repository.delete(userId)
        if (deleted) {
            eventPublisher.publishUserDeleted(UserDeletedEvent(userId, user.email))
        }
        return deleted
    }
}