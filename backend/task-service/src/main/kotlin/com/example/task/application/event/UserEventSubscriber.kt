package com.example.com.example.task.application.event

import com.example.com.example.task.domain.repository.TaskRepository
import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class UserEventSubscriber(
    private val taskRepository: TaskRepository,
    projectId: String,
    subscriptionId: String
) {
    private val logger = LoggerFactory.getLogger(UserEventSubscriber::class.java)
    private val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)
    private var subscriber: Subscriber? = null

    fun start() {
        val receiver = MessageReceiver { message: PubsubMessage, consumer: AckReplyConsumer ->
            try {
                handleMessage(message)
                consumer.ack()
            } catch (e: Exception) {
                logger.error("Failed to process message: {}", e.message, e)
                consumer.nack()
            }
        }

        subscriber = Subscriber.newBuilder(subscriptionName, receiver).build()
        subscriber?.startAsync()?.awaitRunning()
        logger.info("UserEventSubscriber started: subscription={}", subscriptionName)
    }

    fun stop() {
        subscriber?.stopAsync()?.awaitTerminated()
        logger.info("UserEventSubscriber stopped")
    }

    private fun handleMessage(message: PubsubMessage) {
        val eventType = message.attributesMap["eventType"]
        if (eventType != "user.deleted") {
            logger.warn("Unknown event type: {}", eventType)
            return
        }

        val json = message.data.toStringUtf8()
        val event = Json.decodeFromString<UserDeletedEvent>(json)
        logger.info("Received user.deleted event: userId={}, email={}", event.userId, event.email)

        runBlocking {
            val deletedCount = taskRepository.deleteAllByUserId(event.userId)
            logger.info("Deleted {} tasks for userId={}", deletedCount, event.userId)
        }
    }
}
