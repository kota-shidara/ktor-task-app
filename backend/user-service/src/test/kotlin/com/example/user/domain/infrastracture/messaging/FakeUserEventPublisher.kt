package com.example.com.example.user.domain.infrastracture.messaging

class FakeUserEventPublisher : UserEventPublisher {
    val publishedEvents = mutableListOf<Int>()
    val registeredEvents = mutableListOf<Pair<Int, String>>()

    override fun publishUserRegistered(userId: Int, name: String) {
        registeredEvents.add(userId to name)
    }

    override fun publishUserDeleted(userId: Int) {
        publishedEvents.add(userId)
    }
}
