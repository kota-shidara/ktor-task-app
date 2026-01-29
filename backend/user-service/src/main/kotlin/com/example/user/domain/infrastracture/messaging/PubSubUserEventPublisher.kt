package com.example.com.example.user.domain.infrastracture.messaging

import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

@Serializable
private data class UserDeletedEvent(val userId: Int)

class PubSubUserEventPublisher(
    projectId: String,
    topicId: String
) : UserEventPublisher {

    private val logger = LoggerFactory.getLogger(PubSubUserEventPublisher::class.java)
    private val publisher: Publisher = Publisher.newBuilder(TopicName.of(projectId, topicId)).build()

    override fun publishUserDeleted(userId: Int) {
        try {
            val json = Json.encodeToString(UserDeletedEvent.serializer(), UserDeletedEvent(userId))
            val data = ByteString.copyFromUtf8(json)
            val message = PubsubMessage.newBuilder()
                .setData(data)
                .putAttributes("eventType", "user-deleted")
                .build()
            val future = publisher.publish(message)
            val messageId = future.get(10, TimeUnit.SECONDS)
            logger.info("Published user-deleted event for userId=$userId, messageId=$messageId")
        } catch (e: Exception) {
            logger.error("Failed to publish user-deleted event for userId=$userId: ${e.message}", e)
            throw e
        }
    }

    fun shutdown() {
        publisher.shutdown()
        publisher.awaitTermination(30, TimeUnit.SECONDS)
    }
}
