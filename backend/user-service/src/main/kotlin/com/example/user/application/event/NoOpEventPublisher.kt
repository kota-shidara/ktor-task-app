package com.example.com.example.user.application.event

class NoOpEventPublisher : EventPublisher {
    override suspend fun publishUserDeleted(event: UserDeletedEvent) {
        // No-op: used when Pub/Sub is not configured
    }
}
