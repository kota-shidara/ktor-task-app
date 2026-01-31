package com.example.com.example.user.domain.infrastracture.messaging

interface UserEventPublisher {
    fun publishUserRegistered(userId: Int)
    fun publishUserDeleted(userId: Int)
}
