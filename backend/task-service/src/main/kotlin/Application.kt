package com.example

import com.example.com.example.task.application.service.NotionExportService
import com.example.com.example.task.application.service.TaskService
import com.example.com.example.task.domain.infrastracture.cloudtasks.CloudTasksNotionExportEnqueuer
import com.example.com.example.task.domain.infrastracture.messaging.userDeletedSubscriber
import com.example.com.example.task.domain.infrastracture.messaging.userRegisteredSubscriber
import com.example.com.example.task.domain.infrastracture.notion.NotionApiClient
import com.example.com.example.task.domain.infrastracture.repository.ExposedTaskRepository
import com.example.com.example.task.pubsub.PubSubInitializer
import com.example.config.CloudRunSelfUrlResolver
import com.example.config.DotenvConfig
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val repository = ExposedTaskRepository()
    val taskService = TaskService(repository)

    val notionExportService = configureNotionExportService(repository)

    configureSerialization()
    configureDatabases()
    configureRouting(taskService, notionExportService)
    configurePubSubSubscriber(taskService)
}

fun Application.configureNotionExportService(repository: ExposedTaskRepository): NotionExportService {
    // シークレット: .env / 環境変数のみ（application.yaml には書かない）
    val notionApiKey = DotenvConfig.get("NOTION_API_KEY")
        ?: error("NOTION_API_KEY is not configured. Set NOTION_API_KEY env var or add it to .env file")

    val notionPageId = DotenvConfig.get("NOTION_PAGE_ID")
        ?: error("NOTION_PAGE_ID is not configured. Set NOTION_PAGE_ID env var or add it to .env file")

    // インフラ設定: 本番は環境変数、ローカルは application.yaml のデフォルト値
    val cloudTasksProjectId = System.getenv("CLOUD_TASKS_PROJECT_ID")
        ?: environment.config.propertyOrNull("cloudtasks.projectId")?.getString()
        ?: error("cloudtasks.projectId is not configured. Set CLOUD_TASKS_PROJECT_ID env var or cloudtasks.projectId in application.yaml")

    val cloudTasksLocation = System.getenv("CLOUD_TASKS_LOCATION")
        ?: environment.config.propertyOrNull("cloudtasks.location")?.getString()
        ?: error("cloudtasks.location is not configured. Set CLOUD_TASKS_LOCATION env var or cloudtasks.location in application.yaml")

    val notionExportQueueId = System.getenv("CLOUD_TASKS_NOTION_EXPORT_QUEUE_ID")
        ?: environment.config.propertyOrNull("cloudtasks.notionExport.queueId")?.getString()
        ?: error("cloudtasks.notionExport.queueId is not configured. Set CLOUD_TASKS_NOTION_EXPORT_QUEUE_ID env var or cloudtasks.notionExport.queueId in application.yaml")

    val cloudTasksCallbackBaseUrl = environment.config.propertyOrNull("cloudtasks.callbackBaseUrl")?.getString()
        ?: CloudRunSelfUrlResolver.resolve(cloudTasksProjectId, cloudTasksLocation)
        ?: error("cloudtasks.callbackBaseUrl is not configured. Set cloudtasks.callbackBaseUrl in application.yaml, or deploy on Cloud Run for auto-resolution")

    // 本番専用: ローカルでは不要。本番では Cloud Tasks が OIDC トークンを付与するために使用
    val cloudTasksServiceAccountEmail = System.getenv("CLOUD_TASKS_SERVICE_ACCOUNT_EMAIL")

    val notionExporter = NotionApiClient(notionApiKey, notionPageId)
    val notionExportEnqueuer = CloudTasksNotionExportEnqueuer(
        projectId = cloudTasksProjectId,
        location = cloudTasksLocation,
        queueId = notionExportQueueId,
        callbackBaseUrl = cloudTasksCallbackBaseUrl,
        serviceAccountEmail = cloudTasksServiceAccountEmail
    )

    monitor.subscribe(ApplicationStopped) {
        notionExporter.close()
        notionExportEnqueuer.close()
    }

    return NotionExportService(notionExportEnqueuer, notionExporter, repository)
}

fun Application.configurePubSubSubscriber(taskService: TaskService) {
    val projectId = System.getenv("PUBSUB_PROJECTID")
        ?: environment.config.propertyOrNull("pubsub.projectId")?.getString()
        ?: error("pubsub.projectId is not configured. Set PUBSUB_PROJECTID env var or pubsub.projectId in application.yaml")
    val topicUserRegistered = System.getenv("PUBSUB_TOPIC_USER_REGISTERED")
        ?: environment.config.propertyOrNull("pubsub.topics.userRegistered")?.getString()
        ?: error("pubsub.topics.userRegistered is not configured. Set PUBSUB_TOPIC_USER_REGISTERED env var or pubsub.topics.userRegistered in application.yaml")
    val subscriptionUserRegistered = System.getenv("PUBSUB_SUBSCRIPTION_USER_REGISTERED")
        ?: environment.config.propertyOrNull("pubsub.subscriptions.userRegistered")?.getString()
        ?: error("pubsub.subscriptions.userRegistered is not configured. Set PUBSUB_SUBSCRIPTION_USER_REGISTERED env var or pubsub.subscriptions.userRegistered in application.yaml")
    val topicUserDeleted = System.getenv("PUBSUB_TOPIC_USER_DELETED")
        ?: environment.config.propertyOrNull("pubsub.topics.userDeleted")?.getString()
        ?: error("pubsub.topics.userDeleted is not configured. Set PUBSUB_TOPIC_USER_DELETED env var or pubsub.topics.userDeleted in application.yaml")
    val subscriptionUserDeleted = System.getenv("PUBSUB_SUBSCRIPTION_USER_DELETED")
        ?: environment.config.propertyOrNull("pubsub.subscriptions.userDeleted")?.getString()
        ?: error("pubsub.subscriptions.userDeleted is not configured. Set PUBSUB_SUBSCRIPTION_USER_DELETED env var or pubsub.subscriptions.userDeleted in application.yaml")

    PubSubInitializer.ensureTopicAndSubscription(
        projectId,
        topicUserRegistered, subscriptionUserRegistered,
        topicUserDeleted, subscriptionUserDeleted)

    val registeredSubscriber = userRegisteredSubscriber(taskService, projectId, subscriptionUserRegistered)
    val deletedSubscriber = userDeletedSubscriber(taskService, projectId, subscriptionUserDeleted)

    monitor.subscribe(ApplicationStarted) {
        registeredSubscriber.start()
        deletedSubscriber.start()
    }

    monitor.subscribe(ApplicationStopped) {
        registeredSubscriber.stop()
        deletedSubscriber.stop()
    }
}
