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

/**
 * ローカル開発用のPub/Subリソース自動セットアップユーティリティ。
 *
 * Pub/Subエミュレータは状態を永続化しないため、サービス起動のたびにリソースを再作成する必要がある。
 * PUBSUB_EMULATOR_HOST が未設定（=本番環境）の場合は何もしない。
 * 本番ではトピック・サブスクリプションはインフラ側（Terraform等）で事前に作成される。
 *
 * task-service はSubscriber側のため、トピックに加えてサブスクリプションも作成する。
 */
object PubSubInitializer {

    private val logger = LoggerFactory.getLogger(PubSubInitializer::class.java)

    fun ensureTopicAndSubscription(projectId: String, topicId: String, subscriptionId: String) {
        // エミュレータ環境でのみ実行。本番環境ではスキップする。
        val emulatorHost = System.getenv("PUBSUB_EMULATOR_HOST") ?: return

        // エミュレータへのgRPC接続を手動構築する。
        // ManagedChannel: 特定サーバーへのTCP接続を抽象化したgRPCの通信パイプ。
        // 通常はpubsub.googleapis.com:443にTLS接続するが、エミュレータはローカルなので平文通信を使う。
        val channel = ManagedChannelBuilder
            .forTarget(emulatorHost)
            .usePlaintext()
            .build()

        // GrpcTransportChannel: 生のgRPC ManagedChannelをライブラリ共通のトランスポートインターフェースに適合させるアダプター。
        // FixedTransportChannelProvider: クライアントが自動決定する接続先を、手動作成したチャネルで上書き指定する。
        // この2つの組み合わせがエミュレータへの接続を可能にしている。
        val channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
        // NoCredentialsProvider: エミュレータは認証不要のため、認証情報を一切送らない。
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
