package com.example.com.example.task.domain.infrastracture.cloudtasks

import com.example.config.DotenvConfig
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.tasks.v2.CloudTasksClient
import com.google.cloud.tasks.v2.CloudTasksSettings
import com.google.cloud.tasks.v2.HttpMethod
import com.google.cloud.tasks.v2.HttpRequest
import com.google.cloud.tasks.v2.OidcToken
import com.google.cloud.tasks.v2.QueueName
import com.google.cloud.tasks.v2.Task
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.Closeable

@Serializable
data class NotionExportPayload(val userId: Int)

class CloudTasksNotionExportEnqueuer(
    private val projectId: String,
    private val location: String,
    private val queueId: String,
    private val callbackBaseUrl: String,
    private val serviceAccountEmail: String? = null
) : NotionExportEnqueuer, Closeable {

    private val logger = LoggerFactory.getLogger(CloudTasksNotionExportEnqueuer::class.java)
    private val emulatorHost: String? = DotenvConfig.get("CLOUD_TASKS_EMULATOR_HOST")

    private val client: CloudTasksClient by lazy {
        if (emulatorHost != null) {
            val channel = ManagedChannelBuilder
                .forTarget(emulatorHost)
                .usePlaintext()
                .build()
            val channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
            CloudTasksClient.create(
                CloudTasksSettings.newBuilder()
                    .setTransportChannelProvider(channelProvider)
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .build()
            )
        } else {
            CloudTasksClient.create()
        }
    }

    override fun enqueue(userId: Int) {
        val payload = Json.encodeToString(NotionExportPayload.serializer(), NotionExportPayload(userId))
        val queueName = QueueName.of(projectId, location, queueId)
        val callbackUrl = "$callbackBaseUrl/internal/notion-export"

        val httpRequestBuilder = HttpRequest.newBuilder()
            .setUrl(callbackUrl)
            .setHttpMethod(HttpMethod.POST)
            .putHeaders("Content-Type", "application/json")
            .setBody(ByteString.copyFromUtf8(payload))

        if (emulatorHost == null && serviceAccountEmail != null) {
            httpRequestBuilder.setOidcToken(
                OidcToken.newBuilder()
                    .setServiceAccountEmail(serviceAccountEmail)
                    .build()
            )
        }

        val task = Task.newBuilder()
            .setHttpRequest(httpRequestBuilder.build())
            .build()

        val createdTask = client.createTask(queueName, task)
        logger.info("Enqueued notion-export task for userId=$userId: ${createdTask.name}")
    }

    override fun close() {
        client.close()
    }
}
