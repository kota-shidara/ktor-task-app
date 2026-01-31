package com.example.com.example.user.domain.infrastracture.messaging

import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannelBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

@Serializable
private data class UserRegisteredEvent(val userId: Int, val name: String)

@Serializable
private data class UserDeletedEvent(val userId: Int)

class PubSubUserEventPublisher(
    projectId: String,
    registeredTopicId: String,
    deletedTopicId: String
) : UserEventPublisher {

    private val logger = LoggerFactory.getLogger(PubSubUserEventPublisher::class.java)
    private val userRegisteredPublisher: Publisher = createPublisher(projectId, registeredTopicId)
    private val userDeletedPublisher: Publisher = createPublisher(projectId, deletedTopicId)

    override fun publishUserRegistered(userId: Int, name: String) {
        try {
            val json = Json.encodeToString(UserRegisteredEvent.serializer(), UserRegisteredEvent(userId, name))
            publish(userRegisteredPublisher, "user-registered", json, userId)
        } catch (e: Exception) {
            logger.error("Failed to publish user-registered event for userId=$userId: ${e.message}", e)
            throw e
        }
    }

    override fun publishUserDeleted(userId: Int) {
        try {
            val json = Json.encodeToString(UserDeletedEvent.serializer(), UserDeletedEvent(userId))
            publish(userDeletedPublisher, "user-deleted", json, userId)
        } catch (e: Exception) {
            logger.error("Failed to publish user-deleted event for userId=$userId: ${e.message}", e)
            throw e
        }
    }

    fun shutdown() {
        userRegisteredPublisher.shutdown()
        userRegisteredPublisher.awaitTermination(30, TimeUnit.SECONDS)
        userDeletedPublisher.shutdown()
        userDeletedPublisher.awaitTermination(30, TimeUnit.SECONDS)
    }

    private fun createPublisher(projectId: String, topicId: String): Publisher {
        val topicName = TopicName.of(projectId, topicId)
        val builder = Publisher.newBuilder(topicName)
        val emulatorHost = System.getenv("PUBSUB_EMULATOR_HOST")
        // ローカル環境でのみ必要な処理。本番ではライブラリが自動でgoogle cloud上のpub/subにつなげくれる模様（詳細はまだ理解していない）
        if (emulatorHost != null) {
            val channel = ManagedChannelBuilder.forTarget(emulatorHost).usePlaintext().build()
            val channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
            builder.setChannelProvider(channelProvider)
            builder.setCredentialsProvider(NoCredentialsProvider.create())
        }
        return builder.build()
    }

    private fun publish(publisher: Publisher, eventType: String, json: String, userId: Int) {
        val data = ByteString.copyFromUtf8(json)
        val message = PubsubMessage.newBuilder()
            .setData(data)
            .putAttributes("eventType", eventType)
            .build()
        val future = publisher.publish(message)
        val messageId = future.get(10, TimeUnit.SECONDS)
        logger.info("Published $eventType event for $userId, messageId=$messageId")
    }
}
