package com.example.com.example.task.domain.infrastracture.messaging

import com.example.com.example.task.application.service.TaskService
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Serializable
private data class UserRegisteredEvent(val userId: Int, val name: String)

@Serializable
private data class UserDeletedEvent(val userId: Int)

class UserSubscriber(
    private val logger: Logger,
    private val projectId: String,
    private val subscriptionId: String,
    private val receiver: MessageReceiver
) {
    private var subscriber: Subscriber? = null

    fun start() {
        val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)
        val builder = Subscriber.newBuilder(subscriptionName, receiver)

        val emulatorHost = System.getenv("PUBSUB_EMULATOR_HOST")
        if (emulatorHost != null) {
            val channel = ManagedChannelBuilder
                .forTarget(emulatorHost)
                .usePlaintext()
                .build()
            val channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
            builder.setChannelProvider(channelProvider)
            builder.setCredentialsProvider(NoCredentialsProvider.create())
        }

        subscriber = builder.build()
        subscriber?.startAsync()?.awaitRunning()
        logger.info("subscriber started on subscription: $subscriptionId")
    }

    fun stop() {
        subscriber?.stopAsync()?.awaitTerminated()
        logger.info("subscriber stopped")
    }
}

fun userRegisteredSubscriber(
    taskService: TaskService,
    projectId: String,
    subscriptionId: String
): UserSubscriber {
    val logger = LoggerFactory.getLogger("UserRegisteredSubscriber")

    return UserSubscriber(logger, projectId, subscriptionId) { message: PubsubMessage, consumer: AckReplyConsumer ->
        try {
            val json = message.data.toStringUtf8()
            val event = Json.decodeFromString<UserRegisteredEvent>(json)
            logger.info("Received user-registered event for userId=${event.userId}")

            runBlocking {
                taskService.createDefaultTask(event.userId, event.name)
            }
            logger.info("Created default tasks for userId=${event.userId}")
            consumer.ack()
        } catch (e: Exception) {
            logger.error("Failed to process user-registered event: ${e.message}", e)
            consumer.nack()
        }
    }
}

fun userDeletedSubscriber(
    taskService: TaskService,
    projectId: String,
    subscriptionId: String
): UserSubscriber {
    val logger = LoggerFactory.getLogger("UserDeletedSubscriber")

    return UserSubscriber(logger, projectId, subscriptionId) { message: PubsubMessage, consumer: AckReplyConsumer ->
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
}
