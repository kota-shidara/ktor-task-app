package com.example.com.example.user.pubsub

import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory

/**
 * ローカル開発用のPub/Subリソース自動セットアップユーティリティ。
 *
 * Pub/Subエミュレータは状態を永続化しないため、サービス起動のたびにトピックを再作成する必要がある。
 * PUBSUB_EMULATOR_HOST が未設定（=本番環境）の場合は何もしない。
 * 本番ではトピックはインフラ側（Terraform等）で事前に作成される。
 *
 * user-service はPublisher側のため、トピックの作成のみを行う。
 * 参考: https://docs.cloud.google.com/pubsub/docs/emulator?hl=ja
 */
object PubSubInitializer {

    private val logger = LoggerFactory.getLogger(PubSubInitializer::class.java)

    fun ensureTopicExists(projectId: String, topicId: String) {
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

        val topicAdminClient = TopicAdminClient.create(
            TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                // NoCredentialsProvider: エミュレータは認証不要のため、認証情報を一切送らない。
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
