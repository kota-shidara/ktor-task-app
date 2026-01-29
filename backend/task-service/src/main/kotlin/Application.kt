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
    val projectId = environment.config.propertyOrNull("pubsub.projectId")?.getString() ?: "local-project"
    val topicId = environment.config.propertyOrNull("pubsub.topicId")?.getString() ?: "user-deleted"
    val subscriptionId = environment.config.propertyOrNull("pubsub.subscriptionId")?.getString() ?: "task-service-user-deleted-sub"

    PubSubInitializer.ensureTopicAndSubscription(projectId, topicId, subscriptionId)

    val subscriber = UserDeletedSubscriber(taskService, projectId, subscriptionId)

    monitor.subscribe(ApplicationStarted) {
        subscriber.start()
    }

    monitor.subscribe(ApplicationStopped) {
        subscriber.stop()
    }
}
