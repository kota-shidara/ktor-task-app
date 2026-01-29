package com.example.com.example.user.pubsub

import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory

object PubSubInitializer {

    private val logger = LoggerFactory.getLogger(PubSubInitializer::class.java)

    fun ensureTopicExists(projectId: String, topicId: String) {
        val emulatorHost = System.getenv("PUBSUB_EMULATOR_HOST") ?: return

        val channel = ManagedChannelBuilder
            .forTarget(emulatorHost)
            .usePlaintext()
            .build()

        val channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))

        val topicAdminClient = TopicAdminClient.create(
            TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .build()
        )

        try {
            val topicName = TopicName.of(projectId, topicId)
            topicAdminClient.createTopic(topicName)
            logger.info("Created Pub/Sub topic: $topicName")
        } catch (e: com.google.api.gax.rpc.AlreadyExistsException) {
            logger.info("Pub/Sub topic already exists: $topicId")
        } finally {
            topicAdminClient.close()
            channel.shutdown()
        }
    }
}
