package com.example.com.example.task.domain.infrastracture.messaging

import com.example.com.example.task.application.service.TaskService
import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
private data class UserDeletedEvent(val userId: Int)

class UserDeletedSubscriber(
    private val taskService: TaskService,
    private val projectId: String,
    private val subscriptionId: String
) {
    private val logger = LoggerFactory.getLogger(UserDeletedSubscriber::class.java)
    private var subscriber: Subscriber? = null

    fun start() {
        val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)
        val receiver = MessageReceiver { message: PubsubMessage, consumer: AckReplyConsumer ->
            try {
                val json = message.data.toStringUtf8()
                val event = Json.decodeFromString<UserDeletedEvent>(json)
                logger.info("Received user-deleted event for userId=${event.userId}")

                val deletedCount = runBlocking {
                    taskService.deleteAllTasksForUser(event.userId)
                }
                logger.info("Deleted $deletedCount tasks for userId=${event.userId}")
                consumer.ack()
            } catch (e: Exception) {
                logger.error("Failed to process user-deleted event: ${e.message}", e)
                consumer.nack()
            }
        }

        subscriber = Subscriber.newBuilder(subscriptionName, receiver).build()
        subscriber?.startAsync()?.awaitRunning()
        logger.info("UserDeletedSubscriber started on subscription: $subscriptionId")
    }

    fun stop() {
        subscriber?.stopAsync()?.awaitTerminated()
        logger.info("UserDeletedSubscriber stopped")
    }
}
