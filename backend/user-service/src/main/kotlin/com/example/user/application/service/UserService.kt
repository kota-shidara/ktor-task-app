package com.example.com.example.user.application.service

import com.example.com.example.user.application.dto.AuthResponse
import com.example.com.example.user.application.dto.LoginRequest
import com.example.com.example.user.application.dto.RegisterRequest
import com.example.com.example.user.domain.infrastracture.messaging.UserEventPublisher
import com.example.com.example.user.domain.model.User
import com.example.com.example.user.domain.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

class UserService(
    private val repository: UserRepository,
    private val eventPublisher: UserEventPublisher? = null
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    suspend fun register(request: RegisterRequest): AuthResponse {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = User(
            name = request.name,
            email = request.email,
            password = hashedPassword
        )
        val createdUser = repository.create(user)

        try {
            eventPublisher?.publishUserRegistered(createdUser.id!!, createdUser.name)
        } catch (e: Exception) {
            logger.error("User registered but failed to publish event for userId=${createdUser.id!!}.", e)
        }

        return AuthResponse("dummy-token-${createdUser.id}", createdUser.name)
    }

    suspend fun login(request: LoginRequest): AuthResponse? {
        val user = repository.findByEmail(request.email)
        if (user != null && BCrypt.checkpw(request.password, user.password)) {
            return AuthResponse("dummy-token-${user.id}", user.name)
        }
        return null
    }

    suspend fun deleteAccount(userId: Int): Boolean {
        val deleted = repository.deleteById(userId)
        if (deleted) {
            try {
                eventPublisher?.publishUserDeleted(userId)
            } catch (e: Exception) {
                logger.error("User deleted but failed to publish event for userId=$userId. Tasks may remain orphaned.", e)
            }
        }
        return deleted
    }
}