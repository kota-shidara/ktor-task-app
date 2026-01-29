package com.example.com.example.user.application.event

interface EventPublisher {
    suspend fun publishUserDeleted(event: UserDeletedEvent)
}
