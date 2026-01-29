package com.example.com.example.user.domain.infrastracture.messaging

import com.example.com.example.user.application.event.EventPublisher
import com.example.com.example.user.application.event.UserDeletedEvent
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class PubSubEventPublisher(
    projectId: String,
    topicId: String
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(PubSubEventPublisher::class.java)
    private val publisher: Publisher = Publisher.newBuilder(TopicName.of(projectId, topicId)).build()

    override suspend fun publishUserDeleted(event: UserDeletedEvent) {
        val json = Json.encodeToString(event)
        val message = PubsubMessage.newBuilder()
            .setData(ByteString.copyFromUtf8(json))
            .putAttributes("eventType", "user.deleted")
            .build()

        val future = publisher.publish(message)
        val messageId = future.get()
        logger.info("Published user.deleted event: messageId={}, userId={}", messageId, event.userId)
    }

    fun shutdown() {
        publisher.shutdown()
    }
}
