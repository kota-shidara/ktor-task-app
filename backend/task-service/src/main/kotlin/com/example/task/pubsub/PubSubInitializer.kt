package com.example.com.example.task.pubsub

import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.Subscription
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory

object PubSubInitializer {

    private val logger = LoggerFactory.getLogger(PubSubInitializer::class.java)

    fun ensureTopicAndSubscription(projectId: String, topicId: String, subscriptionId: String) {
        val emulatorHost = System.getenv("PUBSUB_EMULATOR_HOST") ?: return

        val channel = ManagedChannelBuilder
            .forTarget(emulatorHost)
            .usePlaintext()
            .build()

        val channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
        val credentialsProvider = NoCredentialsProvider.create()

        val topicAdminClient = TopicAdminClient.create(
            TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
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
        }

        val subscriptionAdminClient = SubscriptionAdminClient.create(
            SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build()
        )

        try {
            val topicName = TopicName.of(projectId, topicId)
            val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)
            val subscription = Subscription.newBuilder()
                .setName(subscriptionName.toString())
                .setTopic(topicName.toString())
                .setPushConfig(PushConfig.getDefaultInstance())
                .setAckDeadlineSeconds(30)
                .build()
            subscriptionAdminClient.createSubscription(subscription)
            logger.info("Created Pub/Sub subscription: $subscriptionName")
        } catch (e: com.google.api.gax.rpc.AlreadyExistsException) {
            logger.info("Pub/Sub subscription already exists: $subscriptionId")
        } finally {
            try {
                subscriptionAdminClient.close()
            } finally {
                channel.shutdown()
            }
        }
    }
}
