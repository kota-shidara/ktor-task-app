package com.example.com.example.user.domain.infrastracture.messaging

interface UserEventPublisher {
    fun publishUserDeleted(userId: Int)
}
