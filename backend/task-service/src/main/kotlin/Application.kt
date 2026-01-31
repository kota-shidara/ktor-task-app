package com.example

import com.example.com.example.task.application.service.TaskService
import com.example.com.example.task.domain.infrastracture.messaging.UserDeletedSubscriber
import com.example.com.example.task.domain.infrastracture.repository.ExposedTaskRepository
import com.example.com.example.task.pubsub.PubSubInitializer
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val repository = ExposedTaskRepository()
    val taskService = TaskService(repository)

    configureSerialization()
    configureDatabases()
    configureRouting(taskService)
    configurePubSubSubscriber(taskService)
}

fun Application.configurePubSubSubscriber(taskService: TaskService) {
    val projectId = System.getenv("PUBSUB_PROJECTID")
        ?: environment.config.propertyOrNull("pubsub.projectId")?.getString()
        ?: error("pubsub.projectId is not configured. Set PUBSUB_PROJECTID env var or pubsub.projectId in application.yaml")
    val topicUserDeleted = System.getenv("PUBSUB_TOPIC_USER_DELETED")
        ?: environment.config.propertyOrNull("pubsub.topics.userDeleted")?.getString()
        ?: error("pubsub.topics.userDeleted is not configured. Set PUBSUB_TOPIC_USER_DELETED env var or pubsub.topics.userDeleted in application.yaml")
    val subscriptionUserDeleted = System.getenv("PUBSUB_SUBSCRIPTION_USER_DELETED")
        ?: environment.config.propertyOrNull("pubsub.subscriptions.userDeleted")?.getString()
        ?: error("pubsub.subscriptions.userDeleted is not configured. Set PUBSUB_SUBSCRIPTION_USER_DELETED env var or pubsub.subscriptions.userDeleted in application.yaml")

    PubSubInitializer.ensureTopicAndSubscription(projectId, topicUserDeleted, subscriptionUserDeleted)

    val subscriber = UserDeletedSubscriber(taskService, projectId, subscriptionUserDeleted)

    monitor.subscribe(ApplicationStarted) {
        subscriber.start()
    }

    monitor.subscribe(ApplicationStopped) {
        subscriber.stop()
    }
}
