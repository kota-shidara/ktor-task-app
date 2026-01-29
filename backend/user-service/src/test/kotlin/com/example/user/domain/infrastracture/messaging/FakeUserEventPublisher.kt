package com.example.com.example.user.domain.infrastracture.messaging

class FakeUserEventPublisher : UserEventPublisher {
    val publishedEvents = mutableListOf<Int>()

    override fun publishUserDeleted(userId: Int) {
        publishedEvents.add(userId)
    }
}
