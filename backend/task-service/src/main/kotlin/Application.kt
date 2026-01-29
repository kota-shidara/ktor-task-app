package com.example

import com.example.com.example.task.application.event.UserEventSubscriber
import com.example.com.example.task.domain.infrastracture.repository.ExposedTaskRepository
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureRouting()
    configureEventSubscriber()
}

fun Application.configureEventSubscriber() {
    val projectId = System.getenv("GCP_PROJECT_ID")
        ?: environment.config.propertyOrNull("pubsub.projectId")?.getString()
    val subscriptionId = System.getenv("PUBSUB_SUBSCRIPTION_USER_EVENTS")
        ?: environment.config.propertyOrNull("pubsub.subscriptionId")?.getString()

    if (projectId == null || subscriptionId == null) {
        log.info("Pub/Sub subscriber disabled: missing projectId or subscriptionId")
        return
    }

    val taskRepository = ExposedTaskRepository()
    val subscriber = UserEventSubscriber(taskRepository, projectId, subscriptionId)

    monitor.subscribe(ApplicationStarted) {
        subscriber.start()
    }

    monitor.subscribe(ApplicationStopping) {
        subscriber.stop()
    }

    log.info("Pub/Sub subscriber configured: project={}, subscription={}", projectId, subscriptionId)
}
