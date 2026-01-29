package com.example.com.example.user.application.event

class SpyEventPublisher : EventPublisher {
    val publishedEvents = mutableListOf<UserDeletedEvent>()

    override suspend fun publishUserDeleted(event: UserDeletedEvent) {
        publishedEvents.add(event)
    }
}
